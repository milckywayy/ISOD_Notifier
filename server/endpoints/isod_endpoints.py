import asyncio
import logging
from datetime import datetime
import aiohttp
from aiohttp import web
from firebase_admin import exceptions

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, DEFAULT_RESPONSE_LANGUAGE
from endpoints.user import create_user
from endpoints.validate_request import InvalidRequestError, validate_post_request
from notifications.notify import send_silent_message, notify


async def link_isod_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['http_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(
            request,
            ['token_fcm', 'isod_username', 'isod_api_key', 'app_version', 'device_language', 'news_filter']
        )

        token_fcm = data['token_fcm']
        isod_username = data['isod_username']
        isod_api_key = data['isod_api_key']
        app_version = data['app_version']
        device_language = data['device_language']
        news_filter = data['news_filter']

        logging.info(f"Attempting to link ISOD account on device: {token_fcm}")

        # Verify token
        send_silent_message(token_fcm)

        # Verify isod account and get news fingerprint
        response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')
        news_fingerprint = response['fingerprint']

        # Get user data
        response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsheaders&username={isod_username}&apikey={isod_api_key}&from=0&to=10')
        news_hashes = [(item['hash'], item['type']) for item in response['items']]
        student_number = response['studentNo']
        firstname = response['firstname']

        # Add user
        user_token = await create_user(db, student_number, firstname)

        # Link ISOD account (username, api key)
        await db.collection('users').document(student_number).collection('isod_account').document(isod_username).set({
            'isod_api_key': isod_api_key,
            'news_fingerprint': news_fingerprint,
        })

        # Add user news
        news_collection = db.collection('users').document(student_number).collection('isod_news')

        async def save_news(news_hash, news_type):
            await news_collection.document(news_hash).set({
                'type': news_type,
                'date': datetime.now(),
            })

        tasks = [asyncio.create_task(save_news(news_hash, news_type)) for news_hash, news_type in news_hashes]
        await asyncio.gather(*tasks)

        # Add device
        await db.collection('users').document(student_number).collection('devices').document(token_fcm).set({
            'app_version': app_version,
            'news_filter': news_filter,
            'language': device_language,
        })

        # Confirm successful link
        notify(token_fcm, loc.get('hello_isod_notification_title', device_language), loc.get('hello_isod_notification_body', device_language))
        logging.info(f"ISOD account ({isod_username}) successfully linked to {student_number}")

        data = {
            'user_token': user_token,
            'firstname': firstname
        }
        return web.json_response(status=200, data=data)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))

    except exceptions.FirebaseError as e:
        logging.error(f"Invalid FCM token given during ISOD auth: {e}")
        return web.Response(status=400, text=loc.get('invalid_fcm_token_error', device_language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during ISOD auth (bad request or ISOD credentials): {e}")
        if e.status == 400:
            return web.Response(status=400, text=loc.get('invalid_isod_auth_data_error', device_language))
        else:
            return web.Response(status=e.status, text=loc.get('isod_server_error', device_language))

    except aiohttp.ClientError as e:
        logging.error(f"Internal server error during registration: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', device_language))


async def unlink_isod_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']

        logging.info(f"Attempting to unlink ISOD account for user: {user_token}")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.info(f"Such user does not exist")
            return web.Response(status=200, text=loc.get('user_not_found_info', device_language))
        user = user[0]

        # Check if user has linked ISOD account
        isod_account = await user.reference.collection('isod_account').get()
        if not isod_account:
            logging.info(f"User has no linked ISOD account")
            return web.Response(status=200, text=loc.get('no_isod_account_linked_info', device_language))
        isod_account = isod_account[0]
        isod_username = isod_account.id

        # Delete ISOD Account
        await isod_account.reference.delete()

        logging.info(f"Unlinked ISOD account ({isod_username}) for user: {user.id}")
        return web.Response(status=200, text=loc.get('isod_account_successfully_unlinked_info', device_language))

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))


async def get_isod_link_status(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['http_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']

        logging.info(f"Attempting to check ISOD account link status for user: {user_token}")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.info(f"No such user")
            return web.Response(status=200, text='0')
        user = user[0]

        # Check if user has linked ISOD account
        isod_account = await user.reference.collection('isod_account').get()
        if not isod_account:
            logging.info(f"User has no linked ISOD account")
            return web.Response(status=200, text='0')
        isod_account = isod_account[0]

        # Fetch ISOD auth data
        isod_username = isod_account.id
        isod_api_key = isod_account.get('isod_api_key')

        # Verify api key
        try:
            await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')

        except aiohttp.ClientResponseError as e:
            if e.status == 400:
                # Key expired, unlink ISOD account
                logging.info(f"ISOD api key expired")
                isod_account.reference.delete()
                return web.Response(status=200, text='0')

        logging.info(f"ISOD account ({isod_username}) is linked with user: {user.id}")
        return web.Response(status=200, text='1')

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during ISOD status check (bad request or ISOD credentials): {e}")
        if e.status == 400:
            return web.Response(status=400, text=loc.get('invalid_isod_auth_data_error', device_language))
        else:
            return web.Response(status=e.status, text=loc.get('isod_server_error', device_language))
