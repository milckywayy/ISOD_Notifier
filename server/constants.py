
SERVICE_PORT = 5000
ISOD_PORTAL_URL = "https://isod.ee.pw.edu.pl/isod-portal"
DEFAULT_NOTIFICATION_URL = "https://isod.ee.pw.edu.pl/isod-stud/"
DEFAULT_RESPONSE_LANGUAGE = 'en'

RATE_LIMITER_MAX_REQUESTS = 30
RATE_LIMITER_PERIOD = 60

EE_USOS_ID = '104000'
EE_USOS_ID_IN_COURSE = '1040'

CLEANUP_USOS_SESSIONS_INTERVAL = 60 * 60 * 12  # 12h
ISOD_NEWS_CHECK_INTERVALS = {
    (0, 7): 1800,   # 00:00 - 06:59 | 30 min
    (7, 8): 600,    # 07:00 - 07:59 | 10 min
    (8, 16): 60,    # 08:00 - 15:59 | 1 min
    (16, 22): 120,  # 16:00 - 21:59 | 2 min
    (22, 24): 600,  # 22:00 - 23:59 | 10 min
}

CLASSTYPE_ISOD_TO_USOS = {
    'W': 'WYK',
    'C': 'CWI',
    'L': 'LAB',
    'P': 'PRO'
}
CLASSTYPE_USOS_TO_ISOD = {
    'WYK': 'W',
    'CWI': 'C',
    'LAB': 'L',
    'PRO': 'P'
}

MAX_CACHE_SIZE = 1500
ENDPOINT_CACHE_TTL = {
    'SCHEDULE': 60 * 60 * 12,   # 12 h
    'GRADES': 60 * 30,          # 30 min
    'SEMESTERS': 60 * 60 * 24,  # 24 h
    'COURSES': 60 * 60 * 24,    # 24 h
    'NEWS': 60 * 60 * 12,       # 12 h
    'NEWS_BODY': 60 * 60 * 6,   # 6 h
}

NEWS_CODES = {
    'WRS': 2137
}
