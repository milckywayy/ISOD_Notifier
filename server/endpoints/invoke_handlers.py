import asyncio


async def invoke_handlers(app):
    loc = app['localization_manager']

    # app['scheduled_task'] = asyncio.create_task()
