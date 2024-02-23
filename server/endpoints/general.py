import logging
import secrets


def create_user(db, index, firstname):

    # Check if user exists
    if db.collection('users').document(index).get():
        # Get token from db
        logging.info(f"Such user already exists: {index}")
        user_token = db.collection('users').document(index).get().get('token')

    else:
        # Add user and generate new token
        logging.info(f"Adding new user: {index}")
        user_token = secrets.token_hex(32)

        # Add user (index, name)
        db.collection('users').document(index).set({
            'first_name': firstname,
            'token': user_token,
        })

    return user_token
