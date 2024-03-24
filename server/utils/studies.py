from datetime import datetime

from constants import EE_USOS_ID_IN_COURSE


def get_current_semester(usosapi):
    today = datetime.now().strftime('%Y-%m-%d')

    response = usosapi.fetch_anonymously_from_service('services/terms/search', min_finish_date=today, max_start_date=today, query='semester')

    return response[0]['id']


def is_usos_course(course_id):
    return course_id.startswith(EE_USOS_ID_IN_COURSE)


def convert_usos_to_isod_course_id(course_id):
    return course_id[course_id.rfind('-') + 1:] if is_usos_course(course_id) else course_id


def trim_usos_course_name(course_name):
    return course_name.split(' - ')[0]

