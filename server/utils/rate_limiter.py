from collections import defaultdict
from aiohttp import web
import time

from endpoints.validate_request import validate_post_request


def rate_limiter(max_requests, period):
    requests = defaultdict(int)
    last_time_checked = defaultdict(int)

    async def middleware(app, handler):
        loc = app['localization_manager']

        async def middleware_handler(request):
            data = await validate_post_request(request, [])
            device_language = loc.validate_language(data.get('language'))
            client_ip = request.remote
            current_time = time.time()

            if current_time - last_time_checked[client_ip] > period:
                requests[client_ip] = 0
                last_time_checked[client_ip] = int(current_time)

            if requests[client_ip] > max_requests:
                return web.json_response(status=429, data={'message': loc.get('too_many_requests_error', device_language)})

            requests[client_ip] += 1
            return await handler(request)

        return middleware_handler

    return middleware
