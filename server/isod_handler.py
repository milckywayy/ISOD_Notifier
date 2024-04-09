import asyncio
import logging
import aiohttp
from firebase_admin import messaging
from datetime import datetime

from async_http_request import async_get_request_with_session
from notifications.notify import notify
from database.sql_queries import *
from utils.decode_filter import decode_filter

TIME_INTERVALS = {
    (0, 7): 1800,   # 00:00 - 06:59 | 30 min
    (7, 8): 600,    # 07:00 - 07:59 | 10 min
    (8, 16): 60,    # 08:00 - 15:59 | 1 min
    (16, 22): 120,  # 16:00 - 21:59 | 2 min
    (22, 24): 600,  # 22:00 - 23:59 | 10 min
}


def get_sleep_duration():
    hour = datetime.now().hour

    for time_range, interval in TIME_INTERVALS.items():
        if time_range[0] <= hour < time_range[1]:
            return interval


def remove_device(username, token, db):
    device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]
    if device_exists:
        db.execute(DELETE_DEVICE_QUERY, (token,))
        logging.info(f"Removed device: {username}: {token}.")

    device_count = db.execute(DEVICE_COUNT_QUERY, (username,))[0][0]
    if device_count < 1:
        logging.info(f"Removed last {username}'s device.")
        remove_user(username, db)


def remove_user(username, db):
    db.execute(DELETE_CLIENT_QUERY, (username,))
    db.execute(DELETE_NEWS_QUERY, (username,))
    db.execute(DELETE_ALL_DEVICES_QUERY, (username,))
    logging.info(f"Removed user: {username}.")


async def check_for_new_notifications(db, loc):
    async with aiohttp.ClientSession() as session:
        while True:
            interval = get_sleep_duration()
            logging.info('Checking for ISOD news. Interval: ' + str(interval) + 'sec')

            clients = db.execute(GET_CLIENTS_QUERY)
            tasks = [process_client(client, db, loc, session) for client in clients]

            try:
                await asyncio.gather(*tasks, return_exceptions=True)
            except Exception as e:
                logging.error(f"Error in processing clients: {e}")

            db.commit()
            await asyncio.sleep(interval)


async def process_client(client, db, loc, session):
    username, api_key, news_fingerprint = client

    try:
        response = await async_get_request_with_session(session,
                                                        f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}')
        new_news_fingerprint = response['fingerprint']

        if news_fingerprint == new_news_fingerprint:
            return

        db.execute(UPDATE_FINGERPRINT_QUERY, (new_news_fingerprint, username))

        response = await async_get_request_with_session(session,
                                                        f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=3')
        new_news_hashes = [(item['hash'], item['type']) for item in response['items']]
        existing_news_hashes = [(item[0], int(item[1])) for item in db.execute(GET_LAST_NEWS_QUERY, (username,))]
        new_hashes = set(new_news_hashes) - set(existing_news_hashes)

        if not new_hashes:
            return

        logging.info(f'New notifications for: {username}')
        devices = [{'token': item[0], 'lang': item[1], 'filter': item[2]} for item in
                   db.execute(GET_DEVICES_QUERY, (username,))]

        for news_hash, news_type in new_hashes:
            news_item = next(item for item in response['items'] if item['hash'] == news_hash)

            db.execute(INSERT_NEWS_QUERY, (username, news_hash, news_item['type'], datetime.now()))
            db.execute(DELETE_OLDEST_NEWS_QUERY, (username,))

            for device in devices:
                token = device['token']
                lang = device['lang']
                news_filter = device['filter']

                filter_classes, filter_announcements, wrs_news, filter_other = decode_filter(news_filter)

                if (news_type == 1001 or news_type == 1002) and not filter_classes:
                    logging.info(f'Skipping class type news: {news_hash}')
                    continue
                elif news_type == 1000 and not filter_announcements:
                    logging.info(f'Skipping announcement: {news_hash}')
                    continue
                elif news_type == 2414 and not wrs_news:
                    logging.info(f'Skipping announcement: {news_hash}')
                    continue
                elif not filter_other:
                    logging.info(f'Skipping other news: {news_hash}')
                    continue

                try:
                    notify(device['token'], loc.get('new_isod_notification_title', lang), news_item["subject"])

                except messaging.exceptions.NotFoundError:
                    logging.info(f'Device token inactive, removing from db: {token}')
                    remove_device(username, token, db)

    except aiohttp.ClientResponseError as e:
        if e.status == 400:
            logging.error(f"{username}'s ISOD API key got changed. Removing user.")
            remove_user(username, db)
        else:
            raise Exception(f'Error processing client {username}: {e}') from e

    except Exception as e:
        raise Exception(f'Error processing client {username}: {e}') from e


async def start_isod_handler(app):
    loc = app['localization_manager']
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db, loc))
