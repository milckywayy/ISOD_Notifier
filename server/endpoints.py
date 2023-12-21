from aiohttp import web

from notify import send_notification


async def register(request):
    print('Register request')

    return web.Response(status=200)


async def unregister(request):
    print('Unregister request')

    return web.Response(status=200)


async def registration_status(request):
    print('Register status request')

    return web.Response(status=200)
