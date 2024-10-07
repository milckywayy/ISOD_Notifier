import asyncio
import logging
from datetime import datetime
import aiohttp

from asynchttp.async_http_request import async_get_request
from constants import ISOD_NEWS_CHECK_INTERVALS, DEFAULT_NOTIFICATION_URL
from notifications.notify import notify
from firebase_admin import exceptions

from utils.firestore import delete_isod_account


def get_sleep_duration():
    hour = datetime.now().hour
    for time_range, interval in ISOD_NEWS_CHECK_INTERVALS.items():
        if time_range[0] <= hour < time_range[1]:
            return interval


async def isod_news_handler(db, loc, cache_manager, session):
    while True:
        try:
            interval = get_sleep_duration()

            tasks = [process_isod_account(account, cache_manager, loc, session) async for account in db.collection_group('isod_account').stream()]
            logging.info(f"Processing {len(tasks)} ISOD accounts. Interval: {interval} sec")
            await asyncio.gather(*tasks, return_exceptions=True)

            await asyncio.sleep(interval)

        except Exception as e:
            logging.error(f"Unexpected error while checking ISOD news: {e}")


async def process_isod_account(account, cache_manager, loc, session):
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
            await delete_isod_account(account.reference)
        else:
            logging.error(f"ISOD server error: {e}")
        return

    new_hashes_not_in_db = [item for item in new_news if not (await account.reference.collection('isod_news').document(item[0]).get()).exists]
    if not new_hashes_not_in_db:
        return

    logging.info(f"New news for account: {username}")
    await process_new_news(account, new_hashes_not_in_db, loc, cache_manager)


async def check_for_news_update(session, username, api_key, current_fingerprint):
    response = await async_get_request(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}')
    return response['fingerprint'] if response['fingerprint'] != current_fingerprint else None


async def fetch_recent_news(session, username, api_key):
    response = await async_get_request(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=3')
    return [(item['hash'], item['subject'], item['type']) for item in response['items']]


async def process_new_news(account, new_hashes_not_in_db, loc, cache_manager):
    for news_hash, _, news_type in new_hashes_not_in_db:
        await account.reference.collection('isod_news').document(news_hash).set({
            'type': news_type, 'date': datetime.now()
        })

    user = await account.reference.parent.parent.get()
    await cache_manager.delete('get_student_grades', user.get('token'))
    await cache_manager.delete('get_student_news', user.get('token'))

    tasks = [process_device(device, new_hashes_not_in_db, loc) async for device in user.reference.collection('devices').stream()]
    await asyncio.gather(*tasks, return_exceptions=True)


async def process_device(device, new_news, loc):
    device_data = device.to_dict()
    device_language = device_data['language']

    for news_hash, news_title, news_type in new_news:
        try:
            notify(
                device.id,
                loc.get('new_isod_notification_title', device_language),
                news_title,
                url=DEFAULT_NOTIFICATION_URL,
                service='ISOD',
                news_hash=news_hash,
                news_type=str(news_type)
            )

        except exceptions.FirebaseError:
            logging.error(f"Expired FCM token. Removing device: {device.id}")
            await device.reference.delete()
            return

        except Exception as e:
            logging.error(f"Unknown error:", e)
            return
