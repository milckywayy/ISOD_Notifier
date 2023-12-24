import asyncio

from server.http_request import get_request
from server.notify import notify

REQUESTS_INTERVAL_TIME_SECONDS = 60

GET_CLIENTS_QUERY = '''SELECT * FROM clients'''
UPDATE_FINGERPRINT_QUERY = '''UPDATE clients SET news_fingerprint = ? WHERE token = ?'''


async def check_for_new_notifications(db):
    while True:
        print("Handling ISOD news")

        clients = db.execute(GET_CLIENTS_QUERY)

        for client in clients:
            token = client[0]
            username = client[1]
            api_key = client[2]
            fingerprint = client[4]

            new_fingerprint = get_request(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}')

            if fingerprint != new_fingerprint:
                news = get_request(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=1')
                notify(token, "New ISOD notification", news["subject"])
                db.execute(UPDATE_FINGERPRINT_QUERY, new_fingerprint, token)

        db.commit()

        await asyncio.sleep(REQUESTS_INTERVAL_TIME_SECONDS)


async def start_isod_handler(app):
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db))
