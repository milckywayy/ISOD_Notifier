import logging
import secrets
from aiohttp import web

from constants import DEFAULT_RESPONSE_LANGUAGE
from endpoints.validate_request import validate_post_request, InvalidRequestError
from utils.firestore import delete_collection, user_exists, isod_account_exists, delete_isod_account


def generate_new_user_token():
    return secrets.token_hex(32)


async def create_user(db, usos_id, firstname):
    user = await user_exists(db, usos_id=usos_id)
    if user:
        # Get token from db
        logging.info(f"Such user already exists: {usos_id}")
        user_token = user.get('token')
    else:
        # Add user and generate new token
        logging.info(f"Adding new user: {usos_id}")
        user_token = generate_new_user_token()

        # Add user (index, name)
        await db.collection('users').document(usos_id).set({
            'first_name': firstname,
            'token': user_token,
        })

    return user_token


async def logout_from_all_other_devices(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'token_fcm'])
        user_token = data['user_token']
        device_token = data['token_fcm']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to logout user {user_token} from all devices except: {device_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        # Remove other devices
        devices = await user.reference.collection('devices').get()
        for device in devices:
            if device.id != device_token:
                await device.reference.delete()

        # Update user token
        new_user_token = generate_new_user_token()
        await user.reference.update({
            'token': new_user_token
        })

        logging.info(f"Logged out {user.id} from all other devices")
        return web.json_response(status=200, data={'user_token': new_user_token})

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={'message': loc.get('invalid_input_data_error', device_language)})


async def delete_user_data(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to remove all user data for {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist: {user_token}")
            return web.json_response(status=200, data={'message': loc.get('user_not_found_info', device_language)})

        # Delete user devices
        await delete_collection(user.reference.collection('devices'))

        # Delete ISOD account
        isod_account = await isod_account_exists(user.reference)
        if isod_account:
            await delete_isod_account(isod_account.reference)

        # Delete user
        await user.reference.delete()

        logging.info(f"Successfully removed all {user.id}'s data")
        return web.json_response(status=200, data={'message': loc.get('all_user_data_successfully_removed_info', device_language)})

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={'message': loc.get('invalid_input_data_error', device_language)})
