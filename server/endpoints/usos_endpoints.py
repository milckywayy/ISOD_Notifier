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


async def authorize_usos_session(request):
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

        logging.info(f"Attempting to link USOS account for token: {token_fcm}")

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

        data = {
            'user_token': user_token,
            'firstname': firstname
        }
        return web.json_response(status=200, data=data)

    except ValueError as e:
        logging.error(f"USOS account auth error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', device_language))

    except exceptions.FirebaseError as e:
        logging.info(f"Invalid FCM token given during USOS auth: {e}")
        return web.Response(status=400, text=loc.get('invalid_fcm_token', device_language))

    except USOSAPIAuthorizationError:
        logging.info(f"Invalid USOSAPI pin was given")
        return web.Response(status=400, text=loc.get('invalid_usosapi_pin', device_language))
