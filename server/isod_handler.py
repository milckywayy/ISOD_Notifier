import asyncio

REQUESTS_INTERVAL_TIME_SECONDS = 5


async def check_for_new_notifications(db):
    while True:
        print("Send notifications")

        db.commit()

        await asyncio.sleep(REQUESTS_INTERVAL_TIME_SECONDS)


async def start_isod_handler(app):
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db))
