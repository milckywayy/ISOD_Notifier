from aiohttp import web

from notify import send_notification


async def register(request):
    db = request.app['db_manager']

    print('Register request')

    return web.Response(status=200)


async def unregister(request):
    db = request.app['db_manager']

    print('Unregister request')

    return web.Response(status=200)


async def registration_status(request):
    db = request.app['db_manager']

    print('Register status request')

    return web.Response(status=200)
