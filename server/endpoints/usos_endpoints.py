import logging
from aiohttp import web

from constants import DEFAULT_RESPONSE_LANGUAGE
from endpoints.user import create_user
from endpoints.validate_request import InvalidRequestError, validate_post_request
from notifications.notify import send_silent_message, notify
from usosapi.usosapi import *
from firebase_admin import exceptions
from utils.firestore import user_exists, usos_account_exists, delete_usos_account


async def get_usos_auth_url(request):
    loc = request.app['localization_manager']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, [])
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Request for USOSAPI auth url")
        request_token, request_url = usosapi.get_auth_url()
        logging.info(f"Created auth session for request token: {request_token}")

        data = {
            'request_token': request_token,
            'request_url': request_url
        }
        return web.json_response(status=200, data=data)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})


async def link_usos_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(
            request,
            ['token_fcm', 'request_token', 'request_pin', 'app_version', 'device_language', 'news_filter']
        )

        token_fcm = data['token_fcm']
        request_token = data['request_token']
        request_pin = data['request_pin']
        app_version = data['app_version']
        device_language = data['device_language']
        news_filter = data['news_filter']

        logging.info(f"Attempting to link USOS account with session {request_token} on device: {token_fcm}")

        # Verify token
        send_silent_message(token_fcm)

        # Authorize USOSAPI session
        usosapi.authorize(request_token, request_pin)

        # Access user data
        access_token, access_token_secret = usosapi.get_access_data()
        user_data = usosapi.fetch_from_service(
            'services/users/user',
            fields='id|first_name'
        )
        usos_id = user_data['id']
        firstname = user_data['first_name']

        # Create user
        user_token = await create_user(db, usos_id, firstname)

        # Link USOS account (access_token, access_token_secret)
        await db.collection('users').document(usos_id).collection('usos_account').document(usos_id).set({
            'access_token': access_token,
            'access_token_secret': access_token_secret,
        })

        # Add device
        await db.collection('users').document(usos_id).collection('devices').document(token_fcm).set({
            'app_version': app_version,
            'news_filter': news_filter,
            'language': device_language,
        })

        # Confirm successful link
        notify(token_fcm, loc.get('hello_usos_notification_title', device_language), loc.get('hello_usos_notification_body', device_language))
        logging.info(f"USOS account ({usos_id}) successfully linked to {usos_id}")

        return web.json_response(status=200, data={
            'user_token': user_token,
            'firstname': firstname
        })

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={
            "message": loc.get('invalid_data_received_form_external_service', device_language)})

    except exceptions.FirebaseError as e:
        logging.error(f"Invalid FCM token given during USOS auth: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_fcm_token_error', device_language)})

    except USOSAPIAuthorizationError:
        logging.info(f"Invalid USOSAPI pin was given")
        return web.json_response(status=400, data={"message": loc.get('invalid_usos_auth_data_error', device_language)})


async def unlink_usos_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to unlink USOS account for user: {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.info(f"Such user does not exist: {user_token}")
            return web.json_response(status=200, data={'message': loc.get('user_not_found_info', device_language)})

        # Check if user has linked USOS account
        usos_account = await usos_account_exists(user.reference)
        if not usos_account:
            logging.info(f"User has no linked USOS account: {user_token}")
            return web.json_response(status=200, data={'message': loc.get('no_usos_account_linked_info', device_language)})

        # Delete USOS Account
        await delete_usos_account(usos_account.reference)

        logging.info(f"Unlinked USOS account ({usos_account.id}) for user: {user.id}")
        return web.json_response(status=200, data={'message': loc.get('usos_account_successfully_unlinked_info', device_language)})

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={'message': loc.get('invalid_input_data_error', device_language)})


async def get_usos_link_status(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to check USOS account link status for user: {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.info(f"No such user: {user_token}")
            return web.json_response(status=200, data={'is_usos_linked': False})

        # Check if user has linked USOS account
        usos_account = await usos_account_exists(user.reference)
        if not usos_account:
            logging.info(f"User has no linked USOS account: {user_token}")
            return web.json_response(status=200, data={'is_usos_linked': False})

        # Fetch USOS auth data
        usos_access_token = usos_account.get('access_token')
        usos_access_token_secret = usos_account.get('access_token_secret')

        # Verify USOSAPI tokens
        try:
            usosapi.resume_session(usos_access_token, usos_access_token_secret)

        except USOSAPIAuthorizationError:
            # Tokens expired, unlink USOS account
            logging.info(f"USOSAPI access tokens expired for {usos_account.id}")
            await delete_usos_account(usos_account.reference)
            return web.json_response(status=200, data={'is_usos_linked': False})

        logging.info(f"USOS account ({usos_account.id}) is linked with user: {user.id}")
        return web.json_response(status=200, data={'is_usos_linked': True})

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={'message': loc.get('invalid_input_data_error', device_language)})
