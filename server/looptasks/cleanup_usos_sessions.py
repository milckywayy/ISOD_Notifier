import asyncio
import logging

from constants import CLEANUP_USOS_SESSIONS_INTERVAL


async def cleanup_usos_sessions(usosapi):
    while True:
        logging.info(f"Cleaning up usos sessions")

        usosapi.cleanup_auth_sessions()

        await asyncio.sleep(CLEANUP_USOS_SESSIONS_INTERVAL)
