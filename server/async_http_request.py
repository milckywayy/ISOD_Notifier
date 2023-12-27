import asyncio
import aiohttp
from aiohttp import ClientSession
import json


def handle_request_errors(func):
    async def wrapper(*args, **kwargs):
        try:
            return await func(*args, **kwargs)
        except aiohttp.ClientResponseError as err:
            raise aiohttp.ClientError(f"Http Error: {err}")
        except aiohttp.ClientConnectionError as err:
            raise aiohttp.ClientError(f"Error Connecting: {err}")
        except asyncio.TimeoutError as err:
            raise aiohttp.ClientError(f"Timeout Error: {err}")
        except aiohttp.ClientError as err:
            raise aiohttp.ClientError(f"Oops: Something went wrong: {err}")
    return wrapper


@handle_request_errors
async def async_get_request(url):
    async with ClientSession() as session:
        async with session.get(url) as response:
            response.raise_for_status()
            return json.loads(await response.text())


@handle_request_errors
async def async_get_request_with_session(session, url):
    async with session.get(url) as response:
        response.raise_for_status()
        return json.loads(await response.text())
