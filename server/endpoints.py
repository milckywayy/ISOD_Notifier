import logging
import aiohttp
from aiohttp import web
from firebase_admin import exceptions

from async_http_request import async_get_request
from notify import send_silent_message
from notify import notify
from sql_queries import *


async def register(request):
    db = request.app['db_manager']

    try:
        data = await request.json()
        token, username, api_key, version = data['token'], data['username'], data['api_key'], data['version']

        logging.info(f"Attempting to register device: {username}, {token}")

        try:
            send_silent_message(token)
        except exceptions.FirebaseError as e:
            logging.info(f"Invalid FCM token: {token}")
            return web.Response(status=400, text="Invalid FCM token.")

        response = await async_get_request(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=3')
        news_hashes = [(item['hash'], item['type']) for item in response['items']]

        client_exists = db.execute(CLIENT_EXISTS_QUERY, (username,))[0][0]
        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]

        if not client_exists:
            db.execute(INSERT_CLIENT_QUERY, (username, api_key))

            for news_hash, news_type in news_hashes:
                db.execute(INSERT_NEWS_QUERY, (username, news_hash, news_type))

            logging.info(f"User registered successfully: {username}")
        else:
            logging.info(f"User is already registered: {username}")

            old_api_key = db.execute(GET_API_KEY_QUERY, (username,))[0][0]

            if old_api_key != api_key:
                db.execute(UPDATE_API_KEY_QUERY, (api_key, username))
                logging.info(f"Updated: {username} api_key.")

        if not device_exists:
            db.execute(INSERT_DEVICE_QUERY, (token, version, username, 63))
            notify(token, "Successfully registered!", "You'll be notified with ISOD news.")
            response_text = "Successfully registered"
            logging.info(f"Device registered successfully: {token}")
        else:
            response_text = "Device is already registered."
            logging.info(f"Device is already registered: {token}")

        return web.Response(status=200, text=response_text)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text="Invalid input data.")

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text="Internal server error.")

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during registration: {e}")
        if e.status == 400:
            return web.Response(status=400, text="Invalid username or API key.")
        else:
            return web.Response(status=e.status, text="Bad request to external service.")

    except aiohttp.ClientError as e:
        logging.error(f"Client error during registration: {e}")
        return web.Response(status=500, text=f"ISOD server error.")


async def unregister(request):
    db = request.app['db_manager']

    try:
        data = await request.json()
        token = data['token']
        username = data['username']

        logging.info(f"Attempting to unregister token: {token}")

        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]

        if device_exists:
            db.execute(DELETE_DEVICE_QUERY, (token,))
            response_text = "Successfully unregistered device."
            logging.info(f"Token unregistered successfully: {token}")
        else:
            response_text = "Device is already unregistered."
            logging.info(f"Token is already unregistered: {token}")

        device_count = db.execute(DEVICE_COUNT_QUERY, (username,))[0][0]

        if device_count < 1:
            db.execute(DELETE_CLIENT_QUERY, (username,))
            db.execute(DELETE_NEWS_QUERY, (username,))
            logging.info(f"Removed last {username} device, unregistered client.")

        return web.Response(status=200, text=response_text)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text="Invalid input data.")

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text="Internal server error.")


async def registration_status(request):
    db = request.app['db_manager']

    try:
        data = await request.json()
        token = data['token']
        app_version = data['version']

        logging.info(f"Checking registration status for token: {token}")

        device_exists = db.execute(DEVICE_EXISTS_QUERY, (token,))[0][0]
        if device_exists:
            status_text = 'User is registered.'
            status = 250

            db_version = db.execute(GET_DEVICE_VERSION_QUERY, (token,))[0][0]
            if app_version != db_version:
                logging.info(f"Updating version for token: {token} from {db_version} to {app_version}")
                db.execute(UPDATE_VERSION_QUERY, (app_version, token))
        else:
            status_text = 'User is unregistered.'
            status = 251

        logging.info(f"Registration status for token {token}: {status_text}")
        return web.Response(status=status, text=status_text)

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text="Invalid input data.")

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text="Internal server error.")
