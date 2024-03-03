import logging
from firebase_admin import messaging


def notify(token, title, body, url=None, news_hash=None):
    data = {
        'url': url if url is not None else '',
        'news_hash': news_hash if news_hash is not None else ''
    }

    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        token=token,
        android=messaging.AndroidConfig(
            priority='high'
        ),
        data=data
    )

    response = messaging.send(message)
    logging.info(f'Successfully sent message: {response}')


def send_silent_message(token):
    message = messaging.Message(token=token)
    response = messaging.send(message)
    logging.info(f'Successfully sent silent message: {response}')
