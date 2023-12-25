from firebase_admin import messaging

DEFAULT_URL = "https://isod.ee.pw.edu.pl/isod-stud/"


def notify(token, title, body, url=DEFAULT_URL):
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

    try:
        response = messaging.send(message)
        print('Successfully sent message:', response)
    except messaging.exceptions.NotFoundError:
        print('Token inactive.')
    except Exception as e:
        print('Error sending message:', e)
