import asyncio
import logging
import aiohttp

from async_http_request import async_get_request_with_session
from notify import notify

REQUESTS_INTERVAL_TIME_SECONDS = 60

GET_CLIENTS_QUERY = '''SELECT * FROM clients'''
UPDATE_FINGERPRINT_QUERY = '''UPDATE clients SET news_fingerprint = ? WHERE token = ?'''


async def check_for_new_notifications(db):
    async with aiohttp.ClientSession() as session:
        while True:
            logging.info('Checking for ISOD news')

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
            await asyncio.sleep(REQUESTS_INTERVAL_TIME_SECONDS)


async def start_isod_handler(app):
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db))
