import firebase_admin
from firebase_admin import credentials

from notify import notify
from database_manager import DatabaseManager

GET_CLIENTS_QUERY = '''SELECT * FROM clients'''

title = 'Service Interruption Alert'
message = '''Dear user, we're temporarily pausing our service for a brief maintenance update. Expect to be back online shortly.'''


if __name__ == '__main__':
    cred = credentials.Certificate('isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)
    
    db = DatabaseManager('clients.db')

    clients = db.execute(GET_CLIENTS_QUERY)

    for client in clients:
        token, username, api_key, _, fingerprint = client

        notify(token, title, message)
