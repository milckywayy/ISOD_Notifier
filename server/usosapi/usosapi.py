import logging
import time
import rauth

_REQUEST_TOKEN_SUFFIX = 'services/oauth/request_token'
_AUTHORIZE_SUFFIX = 'services/oauth/authorize'
_ACCESS_TOKEN_SUFFIX = 'services/oauth/access_token'


class USOSAPISessionNotAuthorizedError(Exception):
    def __init__(self, message):
        self.message = message
        super().__init__(self.message)


class USOSAPIAuthorizationError(Exception):
    def __init__(self, message):
        self.message = message
        super().__init__(self.message)


class USOSAPISession:
    def __init__(self, api_base_address, consumer_key, consumer_secret, scopes):
        self.scopes = scopes

        base_address = api_base_address
        if not base_address.endswith('/'):
            base_address += '/'

        self.service = rauth.OAuth1Service(
            consumer_key=consumer_key,
            consumer_secret=consumer_secret,
            name='USOSAPI',
            request_token_url=base_address + _REQUEST_TOKEN_SUFFIX,
            authorize_url=base_address + _AUTHORIZE_SUFFIX,
            access_token_url=base_address + _ACCESS_TOKEN_SUFFIX,
            base_url=base_address
        )

        # self.auth_sessions = {
        #   request_token: {
        #       secret: request_token_secret,
        #       timestamp: time_since_created
        #   }
        # }
        self.auth_sessions = {}

        self.authorized_session = None

    def get_auth_url(self):
        params = {'oauth_callback': 'oob', 'scopes': self.scopes}
        token_tuple = self.service.get_request_token(params=params)
        request_token, request_token_secret = token_tuple

        self.auth_sessions[request_token] = {'secret': request_token_secret, 'timestamp': time.time()}

        return request_token, self.service.get_authorize_url(request_token)

    def authorize(self, request_token, pin):
        try:
            request_token_secret = self.auth_sessions.pop(request_token)['secret']
        except KeyError:
            raise USOSAPIAuthorizationError('Invalid request token')

        try:
            self.authorized_session = self.service.get_auth_session(
                request_token,
                request_token_secret,
                params={'oauth_verifier': pin}
            )
        except KeyError:
            raise USOSAPIAuthorizationError('Consumer key or token key does not match')

    def get_access_data(self):
        return self.authorized_session.access_token, self.authorized_session.access_token_secret

    def resume_session(self, access_token, access_token_secret):
        self.authorized_session = self.service.get_session()
        self.authorized_session.access_token = access_token
        self.authorized_session.access_token_secret = access_token_secret

        if not self.is_session_authorized():
            raise USOSAPIAuthorizationError('Error resuming USOSAPI session')

    def fetch_from_service(self, service, **kwargs):
        session = self.authorized_session
        if session is None:
            raise USOSAPISessionNotAuthorizedError('Trying to fetch data from not authorized USOSAPI session')

        response = session.post(service, params=kwargs, data={})

        if not response.ok:
            response.raise_for_status()

        return response.json()

    def fetch_anonymously_from_service(self, service, **kwargs):
        session = self.service.get_session()
        response = session.post(service, params=kwargs, data={})

        if not response.ok:
            response.raise_for_status()

        return response.json()

    def is_session_authorized(self):
        if self.authorized_session is None:
            return False

        try:
            identity = self.fetch_from_service('services/users/user')
            return bool(identity['id'])
        except USOSAPISessionNotAuthorizedError:
            return False

    def close_session(self):
        if self.authorized_session is None:
            return

        self.fetch_from_service('services/oauth/revoke_token')
        self.authorized_session = None

    def cleanup_auth_sessions(self):
        expiration_time = 1800
        current_time = time.time()

        expired_tokens = [token for token, details in self.auth_sessions.items() if
                          current_time - details['timestamp'] > expiration_time]

        expired_sessions_count = 0
        for token in expired_tokens:
            del self.auth_sessions[token]
            expired_sessions_count += 1

        logging.info(f"Removed {expired_sessions_count} expired sessions")
