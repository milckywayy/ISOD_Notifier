import firebase_admin
from firebase_admin import credentials
import ssl

from rate_limiter import rate_limiter
from endpoints import *
from isod_handler import start_isod_handler
from database_manager import DatabaseManager


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

    web.run_app(app, port=8080, ssl_context=ssl_context)
