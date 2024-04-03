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
from utils.firestore import user_exists, isod_account_exists, delete_isod_account


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

        logging.info(f"[link_isod_account] Attempting to link ISOD account {isod_username} on device: {token_fcm}")

        # Verify FCM token
        send_silent_message(token_fcm)

        # Verify isod account and get news fingerprint
        response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')
        news_fingerprint = response['fingerprint']

        # Get user data
        response = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsheaders&username={isod_username}&apikey={isod_api_key}&from=0&to=10')
        news_hashes = [(item['hash'], item['type']) for item in response['items']]
        firstname = response['firstname']
        usos_id = response.get('usosId')
        if usos_id is None:
            return web.json_response(status=400, data={'message': loc.get('user_has_no_usos_id_in_isod', device_language)})

        # Add user
        user_token = await create_user(db, usos_id, firstname)

        # Link ISOD account (username, api key)
        await db.collection('users').document(usos_id).collection('isod_account').document(isod_username).set({
            'isod_api_key': isod_api_key,
            'news_fingerprint': news_fingerprint,
        })

        # Add user news
        news_collection = db.collection('users').document(usos_id).collection('isod_account').document(isod_username).collection('isod_news')

        async def save_news(news_hash, news_type):
            await news_collection.document(news_hash).set({
                'type': news_type,
                'date': datetime.now(),
            })

        tasks = [asyncio.create_task(save_news(news_hash, news_type)) for news_hash, news_type in news_hashes]
        await asyncio.gather(*tasks)

        # Add device
        await db.collection('users').document(usos_id).collection('devices').document(token_fcm).set({
            'app_version': app_version,
            'news_filter': news_filter,
            'language': device_language,
        })

        # Confirm successful link
        notify(token_fcm, loc.get('hello_isod_notification_title', device_language), loc.get('hello_isod_notification_body', device_language))
        logging.info(f"ISOD account ({isod_username}) successfully linked to {usos_id}")

        return web.json_response(status=200, data={
            'user_token': user_token,
            'firstname': firstname
        })

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={"message": loc.get('invalid_data_received_form_external_service', device_language)})

    except exceptions.FirebaseError as e:
        logging.error(f"Invalid FCM token given during ISOD auth: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_fcm_token_error', device_language)})

    except aiohttp.ClientResponseError as e:
        if e.status == 400:
            logging.info(f"Invalid username or API key given")
            return web.json_response(status=400, data={"message": loc.get('invalid_isod_auth_data_error', device_language)})
        else:
            logging.error(f"HTTP error: {e}")
            return web.json_response(status=e.status, data={"message": loc.get('isod_server_error', device_language)})

    except aiohttp.ClientError as e:
        logging.error(f"Internal server error during registration: {e}")
        return web.json_response(status=500, data={"message": loc.get('internal_server_error', device_language)})


async def unlink_isod_account(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to unlink ISOD account for user: {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.info(f"Such user does not exist: {user_token}")
            return web.json_response(status=200, data={'message': loc.get('user_not_found_info', device_language)})

        # Check if user has linked ISOD account
        isod_account = await isod_account_exists(user.reference)
        if not isod_account:
            logging.info(f"User has no linked ISOD account: {user_token}")
            return web.json_response(status=200, data={'message': loc.get('no_isod_account_linked_info', device_language)})

        # Delete ISOD account
        await delete_isod_account(isod_account.reference)

        logging.info(f"Unlinked ISOD account ({isod_account.id}) for user: {user.id}")
        return web.json_response(status=200, data={'message': loc.get('isod_account_successfully_unlinked_info', device_language)})

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})


async def get_isod_link_status(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['http_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to check ISOD account link status for user: {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.info(f"No such user")
            return web.json_response(status=200, data={'is_isod_linked': False})

        # Check if user has linked ISOD account
        isod_account = await isod_account_exists(user.reference)
        if not isod_account:
            logging.info(f"User {user_token} has no linked ISOD account")
            return web.json_response(status=200, data={'is_isod_linked': False})

        # Fetch ISOD auth data
        isod_username = isod_account.id
        isod_api_key = isod_account.get('isod_api_key')

        # Verify API key
        try:
            await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')

        except aiohttp.ClientResponseError as e:
            if e.status == 400:
                # Key expired, unlink ISOD account
                logging.info(f"ISOD api key expired for {isod_username}. Unlinking ISOD account")
                await delete_isod_account(isod_account.reference)
                return web.json_response(status=200, data={'is_isod_linked': False})
            else:
                return web.json_response(status=e.status, data={'message': loc.get('isod_server_error', device_language)})

        logging.info(f"ISOD account ({isod_username}) is linked with user: {user.id}")
        return web.json_response(status=200, data={'is_isod_linked': True})

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={"message": loc.get('invalid_data_received_form_external_service', device_language)})
