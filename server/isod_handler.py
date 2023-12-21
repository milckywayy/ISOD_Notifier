import asyncio

REQUESTS_INTERVAL_TIME_SECONDS = 5


async def check_for_new_notifications():
    while True:
        print("Send notifications")

        await asyncio.sleep(REQUESTS_INTERVAL_TIME_SECONDS)


async def start_isod_handler(app):
    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications())
