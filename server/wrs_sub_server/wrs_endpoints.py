import logging

import aiohttp
from aiohttp import web
from async_http_request import async_get_request
from database.sql_queries import GET_DEVICES_QUERY, GET_CLIENTS_QUERY
from notifications.notify import notify
from utils.decode_filter import decode_filter

ADMINS_ISOD_NAMES = ['fraczem1',]


class NoAdminPrivilegesException(Exception):
    pass


async def auth_admin(isod_username, isod_api_key):
    # Auth ISOD account
    await async_get_request(
        f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsfingerprint&username={isod_username}&apikey={isod_api_key}')

    # Auth admin privileges
    if isod_username not in ADMINS_ISOD_NAMES:
        raise NoAdminPrivilegesException(f'User {isod_username} does not have admin privileges')


async def authenticate_admin(request):
    try:
        data = await request.json()
        isod_username, isod_api_key = data['isod_username'], data['isod_api_key']

        logging.info(f"Attempting to authenticate admin: {isod_username}")

        await auth_admin(isod_username, isod_api_key)

        logging.info(f"User {isod_username} successfully authenticated")
        return web.Response(status=200, text='OK')

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during admin auth check: {e}")
        if e.status == 400:
            return web.Response(status=400, text='Invalid username or API key.')
        else:
            return web.Response(status=e.status, text='Bad request to external service.')

    except NoAdminPrivilegesException as e:
        return web.Response(status=400, text=str(e))


async def send_test_notification(request):
    db = request.app['db_manager']

    try:
        data = await request.json()
        isod_username, isod_api_key = data['isod_username'], data['isod_api_key']
        notification_title = data['notification_title']
        notification_body = data['notification_body']
        notification_url = data['notification_url']
        target = data['target']

        logging.info(f"Attempting to send test notification from: {isod_username} to: {target}")

        await auth_admin(isod_username, isod_api_key)

        target_devices = db.execute(GET_DEVICES_QUERY, (target,))

        if not target_devices:
            logging.info(f"Target user is not in the database")
            return web.Response(status=400, text='Użytkownik nie znajduje się w bazie danych.')

        for device in target_devices:
            target_token = device[0]

            if notification_url != '':
                notify(target_token, notification_title, notification_body, url=notification_url)
            else:
                notify(target_token, notification_title, notification_body)

        logging.info(f"Notification was successfully sent to: {target}")
        return web.Response(status=200, text='OK')

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during admin auth check: {e}")
        if e.status == 400:
            return web.Response(status=400, text='Invalid username or API key.')
        else:
            return web.Response(status=e.status, text='Bad request to external service.')


async def send_notification(request):
    db = request.app['db_manager']

    try:
        data = await request.json()
        isod_username, isod_api_key = data['isod_username'], data['isod_api_key']
        notification_title = data['notification_title']
        notification_body = data['notification_body']
        notification_url = data['notification_url']

        logging.info(f"Attempting to send official notification from: {isod_username}")

        await auth_admin(isod_username, isod_api_key)

        clients = db.execute(GET_CLIENTS_QUERY)
        for client in clients:
            username, _, _ = client

            devices = db.execute(GET_DEVICES_QUERY, (username,))
            for device in devices:
                target_token = device[0]
                _, _, wrs_filter, _ = decode_filter(int(device[2]))

                if not wrs_filter:
                    return

                try:
                    if notification_url:
                        notify(target_token, notification_title, notification_body, notification_url)
                    else:
                        notify(target_token, notification_title, notification_body)

                except Exception as e:
                    logging.error(f"Error sending notification to {username}: {e}")

        logging.info(f"Official notification was successfully sent from: {isod_username}")
        return web.Response(status=200, text='OK')

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during admin auth check: {e}")
        if e.status == 400:
            return web.Response(status=400, text='Invalid username or API key.')
        else:
            return web.Response(status=e.status, text='Bad request to external service.')

    except NoAdminPrivilegesException as e:
        return web.Response(status=400, text=str(e))