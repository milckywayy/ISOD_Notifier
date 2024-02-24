import logging
import secrets


async def create_user(db, index, firstname):

    # Check if user exists
    user = await db.collection('users').document(index).get()
    if user.exists:
        # Get token from db
        logging.info(f"Such user already exists: {index}")
        user_token = user.get('token')

    else:
        # Add user and generate new token
        logging.info(f"Adding new user: {index}")
        user_token = secrets.token_hex(32)

        # Add user (index, name)
        await db.collection('users').document(index).set({
            'first_name': firstname,
            'token': user_token,
        })

    return user_token
