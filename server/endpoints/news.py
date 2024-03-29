import logging

import aiohttp
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL, ISOD_PORTAL_URL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from utils.firestore import user_exists, isod_account_exists


async def read_isod_news(session, isod_account, news_list):
    if news_list.get('news') is None:
        news_list['news'] = []

    if not isod_account:
        return news_list

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')

    isod_news = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsheaders&username={isod_username}&apikey={isod_api_key}&from=0&to=15')

    for item in isod_news['items']:
        date = item['modifiedDate'].split(' ')

        news_item = {
            'hash': item['hash'],
            'subject': item['subject'],
            'type': item['type'],
            'day': date[0],
            'hour': date[1]
        }

        news_list['news'].append(news_item)

    return news_list


async def get_student_news(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    cache_manager = request.app['cache_manager']
    session = request.app['http_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_student_news', user_token, request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        logging.info(f"Attempting to create student news list")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.Response(status=400, text=loc.get('user_not_found_info', device_language))

        news_list = {}

        # Fetch ISOD news
        isod_account = await isod_account_exists(user.reference)
        if isod_account:
            news_list = await read_isod_news(session, isod_account, news_list)

        logging.info(f"Created news list for student: {user.id}")
        await cache_manager.set('get_student_news', user_token, request, news_list, ttl=ENDPOINT_CACHE_TTL['NEWS'])
        return web.json_response(status=200, data=news_list)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.Response(status=502, text=loc.get('invalid_data_received_form_external_service', device_language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during ISOD status check (bad request or ISOD credentials): {e}")
        if e.status == 400:
            return web.Response(status=400, text=loc.get('invalid_isod_auth_data_error', device_language))
        else:
            return web.Response(status=e.status, text=loc.get('isod_server_error', device_language))
