import asyncio

from looptasks.cleanup_usos_sessions import cleanup_usos_sessions
from looptasks.isod_news_handler import isod_news_handler


async def invoke_handlers(app):
    loc = app['localization_manager']
    db = app['database_manager']
    session = app['http_session']
    usosapi = app['usosapi_session']

    app['isod_news_handler_task'] = asyncio.create_task(isod_news_handler(db, loc, session))
    app['cleanup_usos_sessions_task'] = asyncio.create_task(cleanup_usos_sessions(usosapi))
