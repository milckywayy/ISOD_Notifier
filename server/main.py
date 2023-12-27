import firebase_admin
from firebase_admin import credentials
import ssl

from rate_limiter import rate_limiter
from endpoints import *
from isod_handler import start_isod_handler
from database_manager import DatabaseManager

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

SERVICE_PORT = 8080


if __name__ == '__main__':
    cred = credentials.Certificate('isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)

    db = DatabaseManager('clients.db')
    db.open()

    app = web.Application(middlewares=[rate_limiter(max_requests=10, period=60)])
    app.add_routes([web.post('/register', register),
                    web.post('/unregister', unregister),
                    web.post('/registration_status', registration_status)])
    app['db_manager'] = db
    app.on_startup.append(start_isod_handler)

    ssl_context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    ssl_context.load_cert_chain('certificate.crt', 'private.key')

    logging.info(f'Starting service on port {SERVICE_PORT}')
    web.run_app(app, port=SERVICE_PORT, ssl_context=ssl_context)
