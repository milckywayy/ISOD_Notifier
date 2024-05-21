from datetime import datetime, timedelta
from typing import Any, Dict, Tuple


class CacheManager:
    def __init__(self, max_cache_size: int = 1000):
        self.cache: Dict[Tuple[str, str, Any], Tuple[Any, datetime]] = {}
        self.max_cache_size = max_cache_size

    async def get(self, endpoint: str, user_token: str, request: Any) -> Any:
        if not self._validate_input(endpoint, user_token):
            return None
        request_id = await self._identify_request(request)
        cache_key = (endpoint, user_token, request_id)
        if cache_key in self.cache:
            response, expiry_time = self.cache[cache_key]
            if datetime.now() < expiry_time:
                return response
            else:
                del self.cache[cache_key]
        return None

    async def set(self, endpoint: str, user_token: str, request: Any, response: Any, ttl: int = 3600) -> None:
        if not self._validate_input(endpoint, user_token, ttl):
            return
        if len(self.cache) >= self.max_cache_size:
            self._evict_cache()
        request_id = await self._identify_request(request)
        expiry_time = datetime.now() + timedelta(seconds=ttl)
        self.cache[(endpoint, user_token, request_id)] = (response, expiry_time)

    async def delete(self, endpoint: str, user_token: str) -> None:
        if not self._validate_input(endpoint, user_token):
            return
        keys_to_delete = [key for key in self.cache if key[0] == endpoint and key[1] == user_token]
        for key in keys_to_delete:
            del self.cache[key]

    async def delete_user_cache(self, user_token: str) -> None:
        keys_to_delete = [key for key in self.cache if key[1] == user_token]
        for key in keys_to_delete:
            del self.cache[key]

    async def _identify_request(self, request: Any) -> Any:
        return str(await request.text())

    def _validate_input(self, endpoint: str, user_token: str, ttl: int = None) -> bool:
        if not all([endpoint, user_token]) or (ttl is not None and type(ttl) is not int):
            return False
        return True

    def _evict_cache(self):
        oldest_key = min(self.cache.keys(), key=lambda k: self.cache[k][1])
        del self.cache[oldest_key]
