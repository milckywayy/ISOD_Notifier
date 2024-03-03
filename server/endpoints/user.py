import logging
import secrets

from aiohttp import web

from endpoints.validate_request import validate_post_request, InvalidRequestError


def generate_new_user_token():
    return secrets.token_hex(32)


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
        user_token = generate_new_user_token()

        # Add user (index, name)
        await db.collection('users').document(index).set({
            'first_name': firstname,
            'token': user_token,
        })

    return user_token


async def logout_from_all_other_devices(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = 'en'

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']

        logging.info(f"Attempting to logout user from all devices except: {user_token}")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.error(f"Such user does not exist")
            return web.Response(status=400, text=loc.get('user_not_found_info', device_language))
        user = user[0]
        student_number = user.id

        # Update user token
        new_user_token = generate_new_user_token()

        await user.reference.update({
            'token': new_user_token
        })

        logging.info(f"Logged out {student_number} from all other devices")
        return web.Response(status=200, text=new_user_token)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))


async def delete_user_data(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = 'en'

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']

        logging.info(f"Attempting to remove all user data from device: {user_token}")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.info(f"Such user does not exist")
            return web.Response(status=200, text=loc.get('user_not_found_info', device_language))
        user = user[0]
        student_number = user.id

        # Delete user devices
        devices_collection = await user.reference.collection('devices').get()
        for doc in devices_collection:
            await doc.reference.delete()

        # Delete ISOD account
        isod_account = await user.reference.collection('isod_account').get()
        if isod_account:
            isod_account = isod_account[0]

            # Delete ISOD news
            isod_news_collection = await isod_account.reference.collection('isod_news').get()
            for doc in isod_news_collection:
                await doc.reference.delete()

            # Delete ISOD account
            await isod_account.reference.delete()

        # Delete user
        await user.reference.delete()

        logging.info(f"Successfully removed all {student_number}'s data")
        return web.Response(status=200, text=loc.get('all_user_data_successfully_removed_info', device_language))

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))
