import logging

from aiohttp import web


async def get_usos_link_url(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    usosapi = request.app['usosapi_session']
    device_language = 'en'

    return web.Response(status=200)

