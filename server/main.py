import logging
import firebase_admin
from aiohttp import web, ClientSession
from firebase_admin import credentials
from firebase_admin import firestore_async
import ssl

from constants import SERVICE_PORT
from endpoints.isod_endpoints import link_isod_account, unlink_isod_account, get_isod_link_status
from endpoints.usos_endpoints import get_usos_link_url
from localization.localizationManager import LocalizationManager
from usosapi import usosapi
from utils.rate_limiter import rate_limiter

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


async def create_session(app):
    app['localization_manager'] = loc
    app['database_manager'] = firestore_async.client()
    app['session'] = ClientSession()


async def close_session(app):
    await app['session'].close()


if __name__ == '__main__':
    cred = credentials.Certificate('credentials/isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)

    loc = LocalizationManager('localization/strings')

    app = web.Application(middlewares=[rate_limiter(max_requests=5, period=60)])
    app.add_routes([web.post('/link_isod_account', link_isod_account),
                    web.post('/unlink_isod_account', unlink_isod_account),
                    web.post('/get_isod_link_status', get_isod_link_status),
                    web.post('/get_usos_link_url', get_usos_link_url)])
    # app.on_startup.append(start_isod_handler)
    app.on_startup.append(create_session)
    app.on_cleanup.append(close_session)

    ssl_context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    ssl_context.load_cert_chain('credentials/certificate.crt', 'credentials/private.key')

    logging.info(f'Starting service on port {SERVICE_PORT}')
    web.run_app(app, port=SERVICE_PORT, ssl_context=ssl_context)
