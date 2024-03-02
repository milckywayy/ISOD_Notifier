import asyncio
import logging


async def cleanup_usos_sessions(usosapi):
    while True:
        logging.info(f"Cleaning up usos sessions")

        # TODO cleanup USOS sessions

        await asyncio.sleep(60)
