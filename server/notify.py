from firebase_admin import messaging


def send_notification(token, title, body):
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        token=token,
        android=messaging.AndroidConfig(
            priority='high'
        )
    )

    response = messaging.send(message)
    print('Successfully sent message:', response)
