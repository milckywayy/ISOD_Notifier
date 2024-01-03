import logging
from datetime import datetime
import aiohttp
from aiohttp import web
from firebase_admin import exceptions

from async_http_request import async_get_request
from server.notifications.notify import send_silent_message
from server.notifications.notify import notify
from server.database.sql_queries import *


async def register(request):
    loc = request.app['localization_manager']
    db = request.app['db_manager']
    language = 'en'

    try:
        data = await request.json()
        token, username, api_key, version, language = data['token'], data['username'], data['api_key'], data['version'], data['language']

        logging.info(f"Attempting to register device: {username}, {token}")

        try:
            send_silent_message(token)
        except exceptions.FirebaseError as e:
            logging.info(f"Invalid FCM token: {token}")
            return web.Response(status=400, text=loc.get('invalid_fcm_token', language))

        response = await async_get_request(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=10')
        news_hashes = [(item['hash'], item['type'], item['modifiedDate']) for item in response['items']]

        client_exists = db.execute(CLIENT_EXISTS_QUERY, (username,))[0][0]
        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]

        if not client_exists:
            db.execute(INSERT_CLIENT_QUERY, (username, api_key))

            for news_hash, news_type, news_date in news_hashes:
                db.execute(INSERT_NEWS_QUERY, (username, news_hash, news_type, datetime.now()))

            logging.info(f"User registered successfully: {username}")
        else:
            logging.info(f"User is already registered: {username}")

            old_api_key = db.execute(GET_API_KEY_QUERY, (username,))[0][0]

            if old_api_key != api_key:
                db.execute(UPDATE_API_KEY_QUERY, (api_key, username))
                logging.info(f"Updated: {username} api_key.")

        if not device_exists:
            db.execute(INSERT_DEVICE_QUERY, (token, version, username, 63, language))
            notify(token, loc.get('hello_notification_title', language), loc.get('hello_notification_body', language))
            response_text = loc.get('successfully_registered', language)
            logging.info(f"Device registered successfully: {token}")
        else:
            response_text = loc.get('device_already_registered', language)
            logging.info(f"Device is already registered: {token}")

        return web.Response(status=200, text=response_text)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', language))

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during registration: {e}")
        if e.status == 400:
            return web.Response(status=400, text=loc.get('invalid_username_or_api_key', language))
        else:
            return web.Response(status=e.status, text=loc.get('bad_request_to_external_service', language))

    except aiohttp.ClientError as e:
        logging.error(f"Client error during registration: {e}")
        return web.Response(status=500, text=loc.get('isod_server_error', language))


async def unregister(request):
    loc = request.app['localization_manager']
    db = request.app['db_manager']
    language = 'en'

    try:
        data = await request.json()
        token = data['token']
        username = data['username']

        logging.info(f"Attempting to unregister token: {token}")

        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]

        if device_exists:
            language = db.execute(GET_DEVICE_LANGUAGE_QUERY, (token,))[0][0]
            db.execute(DELETE_DEVICE_QUERY, (token,))
            response_text = loc.get('successfully_unregistered_device', language)
            logging.info(f"Token unregistered successfully: {token}")
        else:
            response_text = loc.get('device_already_unregistered', language)
            logging.info(f"Token is already unregistered: {token}")

        device_count = db.execute(DEVICE_COUNT_QUERY, (username,))[0][0]

        if device_count < 1:
            db.execute(DELETE_CLIENT_QUERY, (username,))
            db.execute(DELETE_NEWS_QUERY, (username,))
            logging.info(f"Removed last {username} device, unregistered client.")

        return web.Response(status=200, text=response_text)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', language))

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', language))


async def registration_status(request):
    loc = request.app['localization_manager']
    db = request.app['db_manager']
    language = 'en'

    try:
        data = await request.json()
        token = data['token']
        app_version = data['version']

        logging.info(f"Checking registration status for token: {token}")

        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]
        if device_exists:
            status_text = loc.get('user_is_registered', language)
            status = 250

            db_version = db.execute(GET_DEVICE_VERSION_QUERY, (token,))[0][0]
            if app_version != db_version:
                logging.info(f"Updating version for token: {token} from {db_version} to {app_version}")
                db.execute(UPDATE_VERSION_QUERY, (app_version, token))
        else:
            status_text = loc.get('user_is_unregistered', language)
            status = 251

        logging.info(f"Registration status for token {token}: {status_text}")
        return web.Response(status=status, text=status_text)

    except ValueError as e:
        logging.error(f"Registration status check error: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data', language))

    except RuntimeError as e:
        logging.error(f"Database error while checking registration status: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', language))


def convert_date(old_date_format):
    date_object = datetime.strptime(old_date_format, '%d.%m.%Y %H:%M')

    new_date_format = date_object.strftime('%Y-%m-%d %H:%M:%S')
    return new_date_format
