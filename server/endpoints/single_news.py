import logging

import aiohttp
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL, ISOD_PORTAL_URL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from usosapi.usosapi import USOSAPIAuthorizationError
from utils.firestore import user_exists, isod_account_exists, usos_account_exists, delete_isod_account, \
    delete_usos_account


async def read_isod_news_body(session, isod_account, news_hash):
    if not isod_account:
        return None

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')

    isod_news = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsfull&username={isod_username}&apikey={isod_api_key}&hash={news_hash}')
    body = isod_news['items']
    if len(body) < 1:
        return None
    body = body[0]

    return {
        'subject': body['subject'],
        'content': body['content'],
        'date': body['modifiedDate'],
        'who': body['modifiedBy']
    }


async def read_usos_news_body(usosapi, usos_account, news_hash, language):
    if not usos_account:
        return None

    usos_access_token = usos_account.get('access_token')
    usos_access_token_secret = usos_account.get('access_token_secret')
    usosapi.resume_session(usos_access_token, usos_access_token_secret)

    usos_news = usosapi.fetch_from_service('services/pw_jednostka/daj_newsy')
    body = None
    for news in usos_news:
        if str(news['id']) == news_hash:
            body = news
    if body is None:
        return None

    return {
        'subject': body['nazwa'] if language == 'pl' else body['nazwa_ang'],
        'content': body['opis'] if language == 'pl' else body['opis_ang'],
        'date': body['data_od'],
        'who': ''
    }


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
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'news_service', 'news_hash'])
        user_token = data['user_token']
        news_service = data['news_service']
        news_hash = data['news_hash']
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_single_news', user_token, request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        logging.info(f"Attempting to get student news ({news_service}: {news_hash}) body for {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        news_body = None

        if news_service == 'ISOD':
            # Fetch ISOD news body
            isod_account = await isod_account_exists(user.reference)
            if isod_account:
                news_body = await read_isod_news_body(session, isod_account, news_hash)
        elif news_service == 'USOS':
            # Fetch USOS news body
            usos_account = await usos_account_exists(user.reference)
            if usos_account:
                news_body = await read_usos_news_body(usosapi, usos_account, news_hash, device_language)

        if news_body is None:
            news_body = {}

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
        if e.status == 400:
            logging.info(f"ISOD API key expired for {isod_account.id}")
            await delete_isod_account(isod_account.reference)
            return web.json_response(status=400, data={"message": loc.get('isod_api_key_expired', device_language)})
        else:
            logging.error(f"HTTP error: {e}")
            return web.json_response(status=e.status, data={"message": loc.get('isod_server_error', device_language)})

    except USOSAPIAuthorizationError:
        logging.info(f"USOSAPI access tokens expired for {usos_account.id}")
        await delete_usos_account(usos_account.reference)
        return web.json_response(status=400, data={"message": loc.get('usos_session_expired_error', device_language)})

