from rate_limiter import rate_limiter
from endpoints import *
from isod_handler import start_isod_handler
from database_manager import DatabaseManager


db = DatabaseManager('clients.db')
db.open()

app = web.Application(middlewares=[rate_limiter(max_requests=10, period=60)])
app.add_routes([web.post('/register', register),
                web.post('/unregister', unregister),
                web.post('/check', registration_status)])
app['db_manager'] = db
app.on_startup.append(start_isod_handler)

if __name__ == '__main__':
    web.run_app(app, port=8080)
