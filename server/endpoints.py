import logging
import aiohttp
from aiohttp import web

from async_http_request import async_get_request
from notify import notify

INSERT_QUERY = '''INSERT INTO clients (token, username, api_key, version, news_fingerprint) VALUES (?, ?, ?, ?, ?)'''
DELETE_QUERY = '''DELETE FROM clients WHERE token = ?'''
REGISTRATION_STATUS_QUERY = '''SELECT COUNT(token), version FROM clients WHERE token = ?'''
VERSION_UPDATE_QUERY = '''UPDATE clients SET version = ? WHERE token = ?'''


async def get_request_data(request):
    try:
        return await request.json()

    except Exception as e:
        logging.error(f"Error parsing request data: {e}")
        raise ValueError(f"Invalid request: {e}")


def is_isod_response_valid(data):
    try:
        message = data.get("message", "")
        valid = "exception" not in message.lower()
        if not valid:
            logging.warning(f"ISOD response invalid: {message}")
        return valid

    except Exception as e:
        logging.error(f"Error in is_response_valid: {e}")
        return False


async def register(request):
    db = request.app['db_manager']

    try:
        data = await get_request_data(request)
        token, username, api_key, version = data['token'], data['username'], data['api_key'], data['version']
        logging.info(f"Attempting to register user: {username}")

        response = (await async_get_request(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}'))

        if is_isod_response_valid(response):
            db.execute(INSERT_QUERY, (token, username, api_key, version, response['fingerprint']))
            logging.info(f"User registered successfully: {username}, {token}")
            notify(token, "Successfully registered!", "You'll be notified with ISOD news.")
            return web.Response(status=200, text='Successfully registered')
        else:
            logging.warning(f"Registration failed - invalid username or API key: {username}")
            return web.Response(status=400, text='Invalid username or API key.')

    except ValueError as e:
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text=str(e))
    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text=str(e))
    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during registration: {e}")
        return web.Response(status=e.status, text=str(e))
    except aiohttp.ClientError as e:
        logging.error(f"Client error during registration: {e}")
        return web.Response(status=500, text=f"Client error: {e}")


async def unregister(request):
    db = request.app['db_manager']

    try:
        data = await get_request_data(request)
        token = data['token']
        logging.info(f"Attempting to unregister token: {token}")
        db.execute(DELETE_QUERY, (token,))
        logging.info(f"Token unregistered successfully: {token}")
        return web.Response(status=200, text='Unregistered successfully')

    except ValueError as e:
        logging.error(f"Unregistration error: {e}")
        return web.Response(status=400, text=str(e))
    except RuntimeError as e:
        logging.error(f"Database error during unregistration: {e}")
        return web.Response(status=500, text=str(e))


async def registration_status(request):
    db = request.app['db_manager']

    try:
        data = await get_request_data(request)
        token = data['token']
        app_version = data['version']
        logging.info(f"Checking registration status for token: {token}")
        db.execute(REGISTRATION_STATUS_QUERY, (token,))
        query_result = db.fetchone()
        is_registered = bool(query_result[0])
        db_version = query_result[1]
        if is_registered:
            status_text = 'User is Registered'
            if app_version != db_version:
                logging.info(f"Updating version for token: {token} from {db_version} to {app_version}")
                db.execute(VERSION_UPDATE_QUERY, (app_version, token))
            status = 250
        else:
            status_text = 'User is unregistered'
            status = 251
        logging.info(f"Registration status for token {token}: {status_text}")
        return web.Response(status=status, text=status_text)

    except ValueError as e:
        logging.error(f"Error checking registration status: {e}")
        return web.Response(status=400, text=str(e))
    except RuntimeError as e:
        logging.error(f"Database error during registration status check: {e}")
        return web.Response(status=500, text=str(e))
