import logging

from aiohttp import web
from endpoints.general import create_user
from notifications.notify import send_silent_message
from usosapi.usosapi import *
from firebase_admin import exceptions


async def get_usos_auth_url(request):
    usosapi = request.app['usosapi_session']

    logging.info(f"Request for USOSAPI auth url")
    request_token, request_url = usosapi.get_auth_url()
    logging.info(f"Created auth session for request token: {request_token}")

    data = {
        'request_token': request_token,
        'request_url': request_url
    }
    return web.json_response(status=200, data=data)


async def link_usos_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    usosapi = request.app['usosapi_session']
    device_language = 'en'

    try:
        data = await request.json()

        token_fcm = data['token_fcm']
        request_token = data['request_token']
        request_pin = data['request_pin']
        app_version = data['app_version']
        device_language = data['device_language']
        news_filter = data['news_filter']

        logging.info(f"Attempting to link USOS account on device: {token_fcm}")

        # Verify token
        send_silent_message(token_fcm)

        # Authorize USOSAPI session
        usosapi.authorize(request_token, request_pin)

        # Access user data
        access_token, access_token_secret = usosapi.get_access_data()
        user_data = usosapi.fetch_from_service(
            'services/users/user',
            fields='id|first_name|student_number'
        )
        usos_id = user_data.get('id')
        firstname = user_data.get('first_name')
        student_number = user_data.get('student_number')

        # Create user
        user_token = await create_user(db, student_number, firstname)

        # Link USOS account (access_token, access_token_secret)
        await db.collection('users').document(student_number).collection('usos_account').document(usos_id).set({
            'access_token': access_token,
            'access_token_secret': access_token_secret,
        })

        # Add device
        await db.collection('users').document(student_number).collection('devices').document(token_fcm).set({
            'app_version': app_version,
            'news_filter': news_filter,
            'language': device_language,
        })

        # Confirm successful link
        logging.info(f"USOS account ({usos_id}) successfully linked to {student_number}")

        data = {
            'user_token': user_token,
            'firstname': firstname
        }
        return web.json_response(status=200, data=data)

    except ValueError as e:
        logging.error(f"USOS account auth error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))

    except exceptions.FirebaseError as e:
        logging.info(f"Invalid FCM token given during USOS auth: {e}")
        return web.Response(status=400, text=loc.get('invalid_fcm_token_error', device_language))

    except USOSAPIAuthorizationError:
        logging.info(f"Invalid USOSAPI pin was given")
        return web.Response(status=400, text=loc.get('invalid_usos_auth_data_error', device_language))


async def unlink_usos_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = 'en'

    try:
        data = await request.json()
        user_token = data['user_token']

        logging.info(f"Attempting to unlink USOS account for user: {user_token}")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.info(f"Such user does not exist")
            return web.Response(status=200, text=loc.get('user_not_found_info', device_language))
        user = user[0]

        # Check if user has linked USOS account
        usos_account = await user.reference.collection('usos_account').get()
        if not usos_account:
            logging.info(f"User has no linked USOS account")
            return web.Response(status=200, text=loc.get('no_usos_account_linked_info', device_language))
        usos_account = usos_account[0]
        usos_id = usos_account.id

        # Delete USOS Account
        await usos_account.reference.delete()

        logging.info(f"Unlinked USOS account ({usos_id}) for user: {user.id}")
        return web.Response(status=200, text=loc.get('usos_account_successfully_unlinked_info', device_language))

    except ValueError as e:
        logging.error(f"Error during USOS unlink: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))


async def get_usos_link_status(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    usosapi = request.app['usosapi_session']
    device_language = 'en'

    try:
        data = await request.json()
        user_token = data['user_token']

        logging.info(f"Attempting to check USOS account link status for user: {user_token}")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            return web.Response(status=200, text='0')
        user = user[0]

        # Check if user has linked USOS account
        usos_account = await user.reference.collection('usos_account').get()
        if not usos_account:
            logging.info(f"User has no linked USOS account")
            return web.Response(status=200, text='0')
        usos_account = usos_account[0]

        # Fetch USOS auth data
        usos_account_id = usos_account.id
        usos_access_token = usos_account.get('access_token')
        usos_access_token_secret = usos_account.get('access_token_secret')

        # Verify USOSAPI tokens
        try:
            usosapi.resume_session(usos_access_token, usos_access_token_secret)

        except USOSAPIAuthorizationError:
            # Tokens expired, unlink USOS account
            logging.info(f"USOSAPI access tokens expired")
            usos_account.reference.delete()
            return web.Response(status=200, text='0')

        logging.info(f"USOS account ({usos_account_id}) is now linked with user: {user.id}")
        return web.Response(status=200, text='1')

    except ValueError as e:
        logging.error(f"Error during USOS status check: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))