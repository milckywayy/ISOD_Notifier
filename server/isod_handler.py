import asyncio

REQUESTS_INTERVAL_TIME_SECONDS = 60


async def check_for_new_notifications(db):
    while True:
        print("Handling ISOD news")

        db.commit()

        await asyncio.sleep(REQUESTS_INTERVAL_TIME_SECONDS)


async def start_isod_handler(app):
    db = app['db_manager']

    app['scheduled_task'] = asyncio.create_task(check_for_new_notifications(db))