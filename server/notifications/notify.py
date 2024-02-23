import logging
from firebase_admin import messaging

from constants import DEFAULT_FORWARD_URL


def notify(token, title, body, url=DEFAULT_FORWARD_URL):
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        token=token,
        android=messaging.AndroidConfig(
            priority='high'
        ),
        data={"url": url}
    )

    response = messaging.send(message)
    logging.info(f'Successfully sent message: {response}')


def send_silent_message(token):
    message = messaging.Message(token=token)
    response = messaging.send(message)
    logging.info(f'Successfully sent silent message: {response}')
