import asyncio
import logging


async def isod_news_handler(loc, db, session):
    while True:
        logging.info(f"Checking for ISOD news")

        # TODO isod_news_handler

        await asyncio.sleep(60)
