import requests.exceptions
from aiohttp import web

from http_request import get_request
from notify import notify
import time

INSERT_QUERY = '''INSERT INTO clients (token, username, api_key, version) VALUES (?, ?, ?, ?)'''
DELETE_QUERY = '''DELETE FROM clients WHERE token = ?'''


def is_response_valid(data):
    try:
        message = data.get("message", "")

        return "exception" not in message.lower()
    except Exception as e:
        print(f"Error in is_response_valid: {e}")
        return False


async def register(request):
    db = request.app['db_manager']

    print('Register request')

    try:
        data = await request.json()
        token = data['token']
        username = data['username']
        api_key = data['api_key']
        version = data['version']
    except Exception as e:
        return web.Response(status=400, text=f"Invalid request: {e}")

    try:
        response = get_request(username, api_key)
    except requests.exceptions.RequestException as err:
        return web.Response(status=err.response.status_code, text=str(err))

    if is_response_valid(response):
        try:
            db.execute(INSERT_QUERY, (token, username, api_key, version))
        except Exception as e:
            return web.Response(status=500, text='Server failure.')

        notify(token, "Successfully registered!", "You'll be notified with ISOD news.")
        return web.Response(status=200, text='Successfully registered')

    return web.Response(status=500, text='Invalid username or API key.')


async def unregister(request):
    db = request.app['db_manager']

    print('Unregister request')

    try:
        data = await request.json()
        token = data['token']
    except Exception as e:
        return web.Response(status=400, text=f"Invalid request: {e}")

    try:
        db.execute(DELETE_QUERY, (token,))
    except Exception as e:
        return web.Response(status=500, text='Server failure.')

    return web.Response(status=200, text='Unregistered successfully')


async def registration_status(request):
    db = request.app['db_manager']

    print('Register status request')

    return web.Response(status=200)