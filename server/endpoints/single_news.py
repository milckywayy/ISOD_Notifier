import logging

import aiohttp
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL, ISOD_PORTAL_URL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from utils.firestore import user_exists, isod_account_exists


async def read_isod_news_body(session, isod_account, news_body, news_hash):
    if not isod_account:
        return news_body

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')

    isod_news = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfull&username={isod_username}&apikey={isod_api_key}&hash={news_hash}')

    body = isod_news['items']
    if len(body) < 1:
        return news_body
    body = body[0]

    news_body['subject'] = body['subject']
    news_body['content'] = body['content']
    news_body['date'] = body['modifiedDate']
    news_body['who'] = body['modifiedBy']

    return news_body


def add_envelope(news_body, news_hash):
    if news_body is None:
        news_body = {}

    news_body['hash'] = news_hash

    return news_body


async def get_single_news(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    cache_manager = request.app['cache_manager']
    session = request.app['http_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'news_hash'])
        user_token = data['user_token']
        news_hash = data['news_hash']
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_single_news', user_token, request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        logging.info(f"Attempting to get student news ({news_hash}) body")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        news_body = {}

        # Fetch ISOD news body
        isod_account = await isod_account_exists(user.reference)
        if isod_account:
            news_body = await read_isod_news_body(session, isod_account, news_body, news_hash)

        news_body = add_envelope(news_body, news_hash)

        logging.info(f"Got news body for student: {user.id}")
        await cache_manager.set('get_single_news', user_token, request, news_body, ttl=ENDPOINT_CACHE_TTL['NEWS_BODY'])
        return web.json_response(status=200, data=news_body)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={"message": loc.get('invalid_data_received_form_external_service', device_language)})

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during ISOD status check (bad request or ISOD credentials): {e}")
        if e.status == 400:
            return web.json_response(status=400, data={"message": loc.get('invalid_isod_auth_data_error', device_language)})
        else:
            return web.json_response(status=e.status, data={"message": loc.get('isod_server_error', device_language)})
