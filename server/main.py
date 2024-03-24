import warnings

import firebase_admin
from aiohttp import ClientSession
from firebase_admin import credentials
from firebase_admin import firestore_async
import json
import ssl

from constants import SERVICE_PORT
from endpoints.grades import get_student_grades
from endpoints.schedule import get_student_schedule
from endpoints.semesters import get_semesters
from endpoints.user import *
from endpoints.usos_endpoints import *
from endpoints.isod_endpoints import *
from localization.localization_manager import LocalizationManager
from looptasks.invoke_handlers import invoke_handlers
from usosapi.usosapi import USOSAPISession
from utils.rate_limiter import rate_limiter

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - [%(funcName)s] - %(message)s')
logging.getLogger('aiohttp.access').setLevel(logging.WARNING)
warnings.filterwarnings("ignore", message="Detected filter using positional arguments. Prefer using the 'filter' keyword argument instead.")


async def create_session(app):
    app['localization_manager'] = loc
    app['database_manager'] = firestore_async.client()
    app['http_session'] = ClientSession()

    with open('credentials/usos_api_credentials.json', 'r') as file:
        usosapi_credentials = json.load(file)

    app['usosapi_session'] = USOSAPISession(
        usosapi_credentials['api_base_address'],
        usosapi_credentials['consumer_key'],
        usosapi_credentials['consumer_secret'],
        'offline_access|studies'
    )


async def close_session(app):
    await app['http_session'].close()
    app['database_manager'].close()


if __name__ == '__main__':
    cred = credentials.Certificate('credentials/isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)

    loc = LocalizationManager('localization/strings')

    app = web.Application(middlewares=[rate_limiter(max_requests=5, period=60)])
    app.add_routes([web.post('/link_isod_account', link_isod_account),
                    web.post('/unlink_isod_account', unlink_isod_account),
                    web.post('/get_isod_link_status', get_isod_link_status),
                    web.post('/get_usos_auth_url', get_usos_auth_url),
                    web.post('/link_usos_account', link_usos_account),
                    web.post('/unlink_usos_account', unlink_usos_account),
                    web.post('/get_usos_link_status', get_usos_link_status),
                    web.post('/logout_from_all_other_devices', logout_from_all_other_devices),
                    web.post('/delete_user_data', delete_user_data),
                    web.post('/get_semesters', get_semesters),
                    web.post('/get_student_schedule', get_student_schedule),
                    web.post('/get_student_grades', get_student_grades)])
    app.on_startup.append(create_session)
    app.on_startup.append(invoke_handlers)
    app.on_cleanup.append(close_session)

    ssl_context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    ssl_context.load_cert_chain('credentials/certificate.crt', 'credentials/private.key')

    logging.info(f'Starting service on port {SERVICE_PORT}')
    web.run_app(app, port=SERVICE_PORT, ssl_context=ssl_context)
