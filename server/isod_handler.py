import asyncio
import datetime
import logging
import aiohttp
from firebase_admin import messaging

from async_http_request import async_get_request_with_session
from notify import notify
from sql_queries import *

REQUESTS_INTERVAL_TIME_SECONDS = 60

TIME_INTERVALS = {
    (0, 7): 1800,       # 00:00 - 06:59 | 30 min
    (7, 8): 600,        # 07:00 - 07:59 | 10 min
    (8, 16): 60,        # 08:00 - 15:59 | 1 min
    (16, 22): 120,       # 16:00 - 21:59 | 2 min
    (22, 24): 600,      # 22:00 - 23:59 | 10 min
}


def get_sleep_duration():
    hour = datetime.datetime.now().hour

    for time_range, interval in TIME_INTERVALS.items():
        if time_range[0] <= hour < time_range[1]:
            return interval
    return 300  # default value in case something goes wrong


async def check_for_new_notifications(db):
    async with aiohttp.ClientSession() as session:
        while True:
            interval = get_sleep_duration()
            logging.info('Checking for ISOD news. Interval: ' + str(interval) + 'sec')

            clients = db.execute(GET_CLIENTS_QUERY)
            tasks = [process_client(client, db, session) for client in clients]

            try:
                await asyncio.gather(*tasks, return_exceptions=True)
            except Exception as e:
                logging.error(f"Error in processing clients: {e}")

            db.commit()
            await asyncio.sleep(interval)


async def process_client(client, db, session):
    username, api_key = client

    try:
        response = await async_get_request_with_session(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=3')
        new_news_hashes = [item['hash'] for item in response['items']]

        existing_news_hashes = db.execute(GET_NEWS_QUERY, (username,))
        existing_news_hashes = [item[0] for item in existing_news_hashes]

        new_hashes = set(new_news_hashes) - set(existing_news_hashes)
        old_hashes = set(existing_news_hashes) - set(new_news_hashes)

        for old_hash in old_hashes:
            db.execute(DELETE_ONE_NEWS_QUERY, (username, old_hash))

        if new_hashes:
            logging.info(f'New notifications for: {username}')
            tokens = [item[0] for item in db.execute(GET_DEVICES_QUERY, (username,))]

            for news_hash in new_hashes:
                news_item = next(item for item in response['items'] if item['hash'] == news_hash)

                for token in tokens:
                    try:
                        notify(token, "New ISOD notification", news_item["subject"])
                    except messaging.exceptions.NotFoundError:
                        logging.info(f'Token inactive, removing from db: {token}')

                        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]
                        if device_exists:
                            db.execute(DELETE_DEVICE_QUERY, (token,))

                        device_count = db.execute(DEVICE_COUNT_QUERY, (username,))[0][0]
                        if device_count < 1:
                            db.execute(DELETE_CLIENT_QUERY, (username,))
                            db.execute(DELETE_NEWS_QUERY, (username,))
                            logging.info(f"Removed last {username} device, removed client.")

                db.execute(INSERT_NEWS_QUERY, (username, news_hash, news_item['type']))

    except Exception as e:
        raise Exception(f'Error processing client {username}: {e}') from e


async def start_isod_handler(app):
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db))
