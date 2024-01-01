import firebase_admin
from firebase_admin import credentials
import logging

from notify import notify
from database_manager import DatabaseManager
from sql_queries import *


def initialize_firebase():
    cred = credentials.Certificate('isod-notifier-6c6a8e2eca56.json')
    firebase_admin.initialize_app(cred)


def fetch_clients(db, filter_condition):
    return db.execute(GET_CLIENTS_QUERY + f' WHERE {filter_condition}')


def fetch_devices(db, filter_condition, username):
    return db.execute(GET_DEVICES_QUERY + f' AND {filter_condition}', (username,))


def display_recipients(clients):
    print("Notifications will be sent to the following recipients:")
    for client in clients:
        print(client[0])  # Assuming username is the second item in the tuple


def confirm_sending():
    return input("Do you want to continue sending? (Y/n): ") == 'Y'


def send_notifications(db, clients, device_filter_condition, title, message, url=None):
    for client in clients:
        username, _ = client

        tokens = fetch_devices(db, device_filter_condition, username)

        for token in tokens:
            try:
                if url:
                    notify(token[0], title, message, url)
                else:
                    notify(token[0], title, message)

            except Exception as e:
                logging.error(f"Error sending notification to {username}: {e}")


def main(db):
    # Configure your notification settings and filter condition

    title = 'Service Interruption Alert'
    message = '''Dear user, we're temporarily pausing our service for a brief maintenance update. Expect to be back online shortly.'''
    url = 'https://github.com/milckywayy/ISOD_Notifier'

    send_url = False
    client_filter_condition = '1'
    device_filter_condition = '1'

    # Fetch and display clients
    clients = fetch_clients(db, client_filter_condition)
    display_recipients(clients)

    # Confirm and send notifications
    if confirm_sending():
        send_notifications(db, clients, device_filter_condition, title, message, url if send_url else None)


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    initialize_firebase()
    database_manager = DatabaseManager('clients.db')

    main(database_manager)
