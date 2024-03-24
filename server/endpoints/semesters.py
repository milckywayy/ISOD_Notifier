import logging

from aiohttp import web

from utils.studies import get_current_semester


async def get_semesters(request):
    loc = request.app['localization_manager']
    usosapi = request.app['usosapi_session']
    device_language = 'en'

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
        semester = get_current_semester(usosapi)
        all_semesters = calculate_previous_semesters(semester)

        return web.json_response(status=200, data=all_semesters)

    except Exception as e:
        logging.error(f"Internal server error: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', device_language))
