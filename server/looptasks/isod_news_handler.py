import asyncio
import logging
from datetime import datetime
import aiohttp

from asynchttp.async_http_request import async_get_request
from constants import ISOD_NEWS_CHECK_INTERVALS, DEFAULT_NOTIFICATION_URL
from notifications.notify import notify
from utils.decode_filter import decode_filter
from firebase_admin import exceptions


def get_sleep_duration():
    hour = datetime.now().hour
    for time_range, interval in ISOD_NEWS_CHECK_INTERVALS.items():
        if time_range[0] <= hour < time_range[1]:
            return interval


async def isod_news_handler(db, loc, session):
    while True:
        try:
            interval = get_sleep_duration()

            tasks = [process_isod_account(account, db, loc, session) async for account in db.collection_group('isod_account').stream()]
            logging.info(f"Processing {len(tasks)} ISOD accounts. Interval: {interval} sec")
            await asyncio.gather(*tasks, return_exceptions=True)

            await asyncio.sleep(interval)

        except Exception as e:
            logging.error(f"Unexpected error while checking ISOD news: {e}")


async def process_isod_account(account, db, loc, session):
    account_data = account.to_dict()
    username, api_key, current_fingerprint = account.id, account_data['isod_api_key'], account_data['news_fingerprint']

    try:
        new_fingerprint = await check_for_news_update(session, username, api_key, current_fingerprint)
        if not new_fingerprint:
            return

        await account.reference.update({'news_fingerprint': new_fingerprint})
        new_news = await fetch_recent_news(session, username, api_key)

    except aiohttp.ClientResponseError as e:
        if e.status == 400:
            logging.error(f"Expired ISOD API key. Removing account: {account.id}")

            # Delete ISOD news
            isod_news_collection = await account.reference.collection('isod_news').get()
            for doc in isod_news_collection:
                await doc.reference.delete()

            # Delete ISOD Account
            await account.reference.delete()
        else:
            logging.error(f"ISOD server error: {e}")
        return

    new_hashes_not_in_db = [item for item in new_news if not (await account.reference.collection('isod_news').document(item[0]).get()).exists]
    if not new_hashes_not_in_db:
        return

    logging.info(f"New news for account: {username}")
    await process_new_news(account, new_hashes_not_in_db, loc)


async def check_for_news_update(session, username, api_key, current_fingerprint):
    response = await async_get_request(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}')
    return response['fingerprint'] if response['fingerprint'] != current_fingerprint else None


async def fetch_recent_news(session, username, api_key):
    response = await async_get_request(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=3')
    return [(item['hash'], item['subject'], item['type']) for item in response['items']]


async def process_new_news(account, new_hashes_not_in_db, loc):
    for news_hash, _, news_type in new_hashes_not_in_db:
        await account.reference.collection('isod_news').document(news_hash).set({
            'type': news_type, 'date': datetime.now()
        })

    tasks = [process_device(device, new_hashes_not_in_db, loc) async for device in account.reference.parent.parent.collection('devices').stream()]
    await asyncio.gather(*tasks, return_exceptions=True)


async def process_device(device, news, loc):
    device_data = device.to_dict()
    device_language, news_filter = device_data['language'], int(device_data['news_filter'])

    for news_hash, news_title, news_type in news:
        if should_notify(news_type, news_filter):
            try:
                notify(
                    device.id,
                    loc.get('new_isod_notification_title',device_language),
                    news_title,
                    url=DEFAULT_NOTIFICATION_URL,
                    news_hash=news_hash
                )

            except exceptions.FirebaseError:
                logging.error(f"Expired FCM token. Removing device: {device.id}")
                await device.reference.delete()


def should_notify(news_type, news_filter):
    filter_classes, filter_announcements, _, filter_other = decode_filter(news_filter)

    if news_type in (1001, 1002) and not filter_classes:
        logging.info(f"Skipping class type news due to filter settings")
        return False
    elif news_type == 1000 and not filter_announcements:
        logging.info(f"Skipping announcement due to filter settings")
        return False
    elif news_type in (1003, 1004, 1005) and not filter_other:
        logging.info(f"Skipping other news due to filter settings")
        return False

    return True
