import aiohttp_cors
import firebase_admin
from firebase_admin import credentials

from rate_limiter import rate_limiter
from endpoints import *
from database.database_manager import DatabaseManager
from wrs_sub_server.wrs_endpoints import authenticate_admin, send_test_notification, send_notification

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

SUB_SERVICE_PORT = 8060


if __name__ == '__main__':
    cred = credentials.Certificate('../credentials/isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)

    db = DatabaseManager('../clients.db')
    db.open()

    app = web.Application(middlewares=[rate_limiter(max_requests=10, period=60)])
    app.add_routes([web.post('/authenticate_admin', authenticate_admin),
                    web.post('/send_test_notification', send_test_notification),
                    web.post('/send_notification', send_notification)])
    app['db_manager'] = db

    cors = aiohttp_cors.setup(app, defaults={
        "*": aiohttp_cors.ResourceOptions(
            allow_credentials=True,
            expose_headers="*",
            allow_headers="*",
            allow_methods="*",
        )
    })

    for route in list(app.router.routes()):
        cors.add(route)

    logging.info(f'Starting sub service on port {SUB_SERVICE_PORT}')
    web.run_app(app, port=SUB_SERVICE_PORT)
