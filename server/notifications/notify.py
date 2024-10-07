import logging
from firebase_admin import messaging


def notify(token, title, body, url=None, service=None, news_hash=None, news_type=None):
    data = {
        'title': title,
        'body': body
    }

    if url is not None:
        data['url'] = url
    if service is not None:
        data['service'] = service
    if news_hash is not None:
        data['news_hash'] = news_hash
    if news_type is not None:
        data['news_type'] = news_type

    message = messaging.Message(
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
