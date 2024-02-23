import asyncio
import aiohttp
import json


def handle_request_errors(func):
    async def wrapper(*args, **kwargs):
        try:
            return await func(*args, **kwargs)

        except aiohttp.ClientConnectionError as err:
            raise aiohttp.ClientError(f"Error Connecting: {err}")

        except asyncio.TimeoutError as err:
            raise aiohttp.ClientError(f"Timeout Error: {err}")

    return wrapper


@handle_request_errors
async def async_get_request(session, url):
    async with session.get(url) as response:
        response.raise_for_status()
        return json.loads(await response.text())
