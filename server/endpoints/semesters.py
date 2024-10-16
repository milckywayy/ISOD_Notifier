import logging

from aiohttp import web

from constants import DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL
from endpoints.validate_request import validate_post_request
from utils.studies import get_current_semester


async def get_semesters(request):
    loc = request.app['localization_manager']
    cache_manager = request.app['cache_manager']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    def calculate_previous_semesters(current_id, count=10):
        semesters = []
        year, season = int(current_id[:-1]), current_id[-1]

        for _ in range(count):
            semesters.append(f"{year}{season}")
            if season == 'Z':
                season = 'L'
            else:
                season = 'Z'
                year -= 1

        return semesters

    try:
        data = await validate_post_request(request, [])
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_semesters', 'all', request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        semester = get_current_semester(usosapi)
        all_semesters = calculate_previous_semesters(semester)

        await cache_manager.set('get_semesters', 'all', request, all_semesters, ttl=ENDPOINT_CACHE_TTL['SEMESTERS'])
        return web.json_response(status=200, data=all_semesters)

    except Exception as e:
        logging.error(f"Internal server error: {e}")
        return web.json_response(status=500, data={'message': loc.get('internal_server_error', device_language)})
