from collections import defaultdict
from aiohttp import web
import time


def rate_limiter(max_requests, period):
    requests = defaultdict(int)
    last_time_checked = defaultdict(int)

    async def middleware(app, handler):
        async def middleware_handler(request):
            client_ip = request.remote
            current_time = time.time()

            if current_time - last_time_checked[client_ip] > period:
                requests[client_ip] = 0
                last_time_checked[client_ip] = int(current_time)

            if requests[client_ip] > max_requests:
                return web.Response(status=429, text="Too many requests")

            requests[client_ip] += 1
            return await handler(request)

        return middleware_handler

    return middleware
