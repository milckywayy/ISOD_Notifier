import logging
from datetime import datetime
import aiohttp
from aiohttp import web
from firebase_admin import exceptions

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL
from endpoints.general import create_user
from notifications.notify import send_silent_message, notify


async def link_isod_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['session']
    device_language = 'en'

    try:
        data = await request.json()

        token_fcm = data['token_fcm']
        isod_username = data['isod_username']
        isod_api_key = data['isod_api_key']
        app_version = data['app_version']
        device_language = data['device_language']
        news_filter = data['news_filter']

        logging.info(f"Attempting to link ISOD account for token: {token_fcm}")

        # Verify token
        send_silent_message(token_fcm)

        # Verify isod account and get news fingerprint
        response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')
        news_fingerprint = response['fingerprint']

        # Get all news hashes, firstname and index
        response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsheaders&username={isod_username}&apikey={isod_api_key}')
        news_hashes = [(item['hash'], item['type']) for item in response['items']]
        index = response['studentNo']
        firstname = response['firstname']

        # Add user
        user_token = create_user(db, index, firstname)

        # Link ISOD account (username, api key)
        db.collection('users').document(index).collection('isod_account').document(isod_username).set({
            'isod_api_key': isod_api_key,
            'news_fingerprint': news_fingerprint,
        })

        # Add user news
        for news_hash, news_type in news_hashes:
            db.collection('users').document(index).collection('isod_news').document(news_hash).set({
                'type': news_type,
                'date': datetime.now(),
            })

        # Add device
        db.collection('users').document(index).collection('devices').document(token_fcm).set({
            'app_version': app_version,
            'news_filter': news_filter,
            'language': device_language,
        })

        notify(token_fcm, loc.get('hello_notification_title', device_language), loc.get('hello_notification_body', device_language))
        logging.info(f"ISOD account ({isod_username}) successfully linked to {index}")
        return web.Response(status=200, text=user_token)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', device_language))

    except exceptions.FirebaseError as e:
        logging.info(f"Invalid FCM token: {token_fcm}")
        return web.Response(status=400, text=loc.get('invalid_fcm_token', device_language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during registration (bad request or isod credentials): {e}")
        if e.status == 400:
            return web.Response(status=400, text=loc.get('invalid_username_or_api_key', device_language))
        else:
            return web.Response(status=e.status, text=loc.get('bad_request_to_external_service', device_language))

    except aiohttp.ClientError as e:
        logging.error(f"Client error during registration: {e}")
        return web.Response(status=500, text=loc.get('isod_server_error', device_language))


async def unlink_isod_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = 'en'

    try:
        data = await request.json()
        user_token = data['user_token']

        logging.info(f"Attempting to unlink ISOD account for user: {user_token}")

        # Check if user exists
        user = db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.info(f"Such user does not exist")
            return web.Response(status=200, text='Such user does not exist')
        user = user[0]

        # Check if user has linked ISOD account
        isod_account = user.reference.collection('isod_account').get()
        if not isod_account:
            logging.info(f"User has no linked ISOD account")
            return web.Response(status=200, text='User has no linked ISOD account')
        isod_account = isod_account[0]
        isod_username = isod_account.id

        # Delete ISOD Account
        isod_account.reference.delete()

        logging.info(f"Unlinked ISOD account ({isod_username}) for user: {user.id}")
        return web.Response(status=200)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', device_language))

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', device_language))


async def get_isod_link_status(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['session']
    device_language = 'en'

    try:
        data = await request.json()
        user_token = data['user_token']

        logging.info(f"Attempting to check ISOD account link status for user: {user_token}")

        # Check if user exists
        user = db.collection('users').where('token', '==', user_token).get()
        if not user:
            return web.Response(status=200, text='Unlinked')
        user = user[0]

        # Check if user has linked ISOD account
        isod_account = user.reference.collection('isod_account').get()
        if not isod_account:
            logging.info(f"User has no linked ISOD account")
            return web.Response(status=200, text='Unlinked')
        isod_account = isod_account[0]
        isod_username = isod_account.id
        isod_api_key = isod_account.get('isod_api_key')

        # Verify api key
        try:
            response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')
        except aiohttp.ClientResponseError as e:
            if e.status == 400:
                # Key expired, unlink ISOD account
                logging.info(f"ISOD api key expired")
                isod_account.reference.delete()
                return web.Response(status=200, text='Unlinked')

        logging.info(f"ISOD account ({isod_username}) is linked with user: {user.id}")
        return web.Response(status=200, text='Linked')

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', device_language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during api key check: {e}")
        return web.Response(status=e.status, text=loc.get('bad_request_to_external_service', device_language))

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', device_language))
