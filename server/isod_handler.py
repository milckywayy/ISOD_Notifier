import asyncio
import datetime
import logging
import aiohttp

from async_http_request import async_get_request_with_session
from notify import notify

REQUESTS_INTERVAL_TIME_SECONDS = 60

GET_CLIENTS_QUERY = '''SELECT * FROM clients'''
UPDATE_FINGERPRINT_QUERY = '''UPDATE clients SET news_fingerprint = ? WHERE token = ?'''

TIME_INTERVALS = {
    (0, 7): 1800,       # 00:00 - 06:59 | 30 min
    (7, 8): 600,        # 07:00 - 07:59 | 10 min
    (8, 16): 60,        # 08:00 - 15:59 | 1 min
    (16, 22): 90,      # 16:00 - 21:59 | 2 min
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
            for client in clients:
                token, username, api_key, _, fingerprint = client

                try:
                    new_fingerprint = await async_get_request_with_session(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}')

                    if fingerprint != new_fingerprint['fingerprint']:
                        news = (await async_get_request_with_session(session, f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=1'))['items'][0]
                        notify(token, "New ISOD notification", news["subject"])
                        db.execute(UPDATE_FINGERPRINT_QUERY, (new_fingerprint['fingerprint'], token))

                except Exception as e:
                    logging.error(f"Error processing client {username}: {e}")

            db.commit()
            await asyncio.sleep(interval)


async def start_isod_handler(app):
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db))
