import logging

from aiohttp import web


async def register_usos_account(request):
    logging.info(f"Attempting to register usos account")

    return web.Response(status=200)


async def unregister_usos_account(request):
    logging.info(f"Attempting to unregister usos account")

    return web.Response(status=200)


async def get_usos_registration_status(request):
    logging.info(f"Attempting to check usos account status")

    return web.Response(status=200)
