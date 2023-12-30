import firebase_admin
from firebase_admin import credentials
import logging

from notify import notify
from database_manager import DatabaseManager


def initialize_firebase():
    cred = credentials.Certificate('isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)


def fetch_clients(db, filter_condition):
    GET_CLIENTS_QUERY = f'''SELECT * FROM clients WHERE {filter_condition}'''
    return db.execute(GET_CLIENTS_QUERY)


def display_recipients(clients):
    print("Notifications will be sent to the following recipients:")
    for client in clients:
        print(client[1])  # Assuming username is the second item in the tuple


def confirm_sending():
    return input("Do you want to continue sending? (Y/n): ") == 'Y'


def send_notifications(clients, title, message, url=None):
    for client in clients:
        token, username, _, _, _ = client

        try:
            if url:
                notify(token, title, message, url)
            else:
                notify(token, title, message)

        except Exception as e:
            logging.error(f"Error sending notification to {username}: {e}")


def main(db):
    # Configure your notification settings and filter condition

    title = 'Service Interruption Alert'
    message = '''Dear user, we're temporarily pausing our service for a brief maintenance update. Expect to be back online shortly.'''
    url = 'https://github.com/milckywayy/ISOD_Notifier'

    send_url = False
    filter_condition = "1"

    # Fetch and display clients
    clients = fetch_clients(db, filter_condition)
    display_recipients(clients)

    # Confirm and send notifications
    if confirm_sending():
        send_notifications(clients, title, message, url if send_url else None)


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    initialize_firebase()
    database_manager = DatabaseManager('clients.db')

    main(database_manager)
