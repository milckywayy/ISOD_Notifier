import logging

import aiohttp
from aiohttp import web
from datetime import datetime
from asynchttp.async_http_request import async_get_request
from constants import DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL, ISOD_PORTAL_URL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from usosapi.usosapi import USOSAPIAuthorizationError
from utils.firestore import user_exists, isod_account_exists, usos_account_exists, delete_isod_account, \
    delete_usos_account


def format_isod_date(date_str):
    day, month, year = date_str.split('.')
    formatted_date = f"{year}.{month.zfill(2)}.{day.zfill(2)}"
    return formatted_date


def format_usos_hour(time_str):
    hour, minute, _ = time_str.split(':')
    formatted_time = f"{hour}:{minute}"
    return formatted_time


async def read_isod_news(session, isod_account, news_list):
    if news_list.get('news') is None:
        news_list['news'] = []

    if not isod_account:
        return news_list

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')

    isod_news = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mynewsheaders&username={isod_username}&apikey={isod_api_key}')

    for item in isod_news['items']:
        date = item['modifiedDate'].split(' ')

        news_item = {
            'service': 'ISOD',
            'hash': item['hash'],
            'subject': item['subject'],
            'type': str(item['type']),
            'day': format_isod_date(date[0]),
            'hour': date[1]
        }

        news_list['news'].append(news_item)

    return news_list


async def read_usos_news(usosapi, usos_account, news_list, language):
    if news_list.get('news') is None:
        news_list['news'] = []

    if not usos_account:
        return news_list

    usos_access_token = usos_account.get('access_token')
    usos_access_token_secret = usos_account.get('access_token_secret')
    usosapi.resume_session(usos_access_token, usos_access_token_secret)

    usos_news = usosapi.fetch_from_service('services/pw_jednostka/daj_newsy')

    for item in usos_news:
        date = item['data_od'].split(' ')

        news_item = {
            'service': 'USOS',
            'hash': str(item['id']),
            'subject': item['nazwa'] if language == 'pl' and item['nazwa'] else item['nazwa_ang'] if item['nazwa_ang'] else item['nazwa'],
            'type': 'USOS',
            'day': date[0].replace('-', '.'),
            'hour': format_usos_hour(date[1])
        }

        news_list['news'].append(news_item)

    return news_list


async def get_student_news(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    cache_manager = request.app['cache_manager']
    session = request.app['http_session']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'page', 'page_size'])
        user_token = data['user_token']
        page = int(data['page'])
        page_size = int(data['page_size'])
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_student_news', user_token, request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        logging.info(f"Attempting to create student news list for {user_token}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist: {user_token}")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        news_list = {}

        # Fetch ISOD news
        isod_account = await isod_account_exists(user.reference)
        if isod_account:
            news_list = await read_isod_news(session, isod_account, news_list)

        # Fetch USOS news
        usos_account = await usos_account_exists(user.reference)
        if usos_account:
            news_list = await read_usos_news(usosapi, usos_account, news_list, device_language)

        # Sort news by date
        news_list["news"] = sorted(news_list['news'], key=lambda x: datetime.strptime(f"{x['day']} {x['hour']}", '%Y.%m.%d %H:%M'))

        all_news_count = len(news_list['news'])
        news_list['all_news_count'] = str(all_news_count)

        # Paginate news
        news_from = page * page_size
        news_to = news_from + page_size
        news_list["news"] = (news_list["news"][::-1])[news_from:news_to]

        logging.info(f"Created news list for student: {user.id}")
        await cache_manager.set('get_student_news', user_token, request, news_list, ttl=ENDPOINT_CACHE_TTL['NEWS'])
        return web.json_response(status=200, data=news_list)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={'message': loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={'message': loc.get('invalid_data_received_form_external_service', device_language)})

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
