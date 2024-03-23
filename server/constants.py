
SERVICE_PORT = 8080
ISOD_PORTAL_URL = "https://isod.ee.pw.edu.pl/isod-portal"
DEFAULT_NOTIFICATION_URL = "https://isod.ee.pw.edu.pl/isod-stud/"
EE_USOS_ID = '104000'
EE_USOS_ID_IN_COURSE = '1040'
DEFAULT_RESPONSE_LANGUAGE = 'en'

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
