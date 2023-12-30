import logging
import aiohttp
from aiohttp import web

from async_http_request import async_get_request
from notify import notify

INSERT_QUERY = '''INSERT INTO clients (token, username, api_key, version, news_fingerprint) VALUES (?, ?, ?, ?, ?)'''
DELETE_QUERY = '''DELETE FROM clients WHERE token = ?'''
REGISTRATION_STATUS_QUERY = '''SELECT COUNT(token), version FROM clients WHERE token = ?'''
VERSION_UPDATE_QUERY = '''UPDATE clients SET version = ? WHERE token = ?'''


async def register(request):
    db = request.app['db_manager']

    try:
        data = await request.json()
        token, username, api_key, version = data['token'], data['username'], data['api_key'], data['version']

        logging.info(f"Attempting to register user: {username}")

        response = await async_get_request(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={username}&apikey={api_key}')

        db.execute(INSERT_QUERY, (token, username, api_key, version, response['fingerprint']))
        logging.info(f"User registered successfully: {username}, {token}")
        notify(token, "Successfully registered!", "You'll be notified with ISOD news.")
        return web.Response(status=200, text='Successfully registered')

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

        logging.info(f"Attempting to unregister token: {token}")

        db.execute(DELETE_QUERY, (token,))
        logging.info(f"Token unregistered successfully: {token}")
        return web.Response(status=200, text='Unregistered successfully')

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

        query_result = db.execute(REGISTRATION_STATUS_QUERY, (token,))[0]
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
        logging.error(f"Registration error: {e}")
        return web.Response(status=400, text="Invalid input data.")

    except RuntimeError as e:
        logging.error(f"Database error during registration: {e}")
        return web.Response(status=500, text="Internal server error.")
