import logging

from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from utils.classtypes import convert_usos_to_isod_classtype
from utils.firestore import user_exists, isod_account_exists


def get_isod_grades_id(isod_courses, course_id, classtype):
    for course in isod_courses['items']:
        if course.get('courseNumber', '') != course_id:
            continue

        for course_class in course['classes']:
            if course_class.get('type', '') == classtype:
                return course_class.get('id'), course_class.get('credit', '')

    return None, None


def add_envelope(grades, course_id, classtype, final_grade):
    grades['course_id'] = course_id
    grades['classtype'] = classtype
    grades['final_grade'] = final_grade

    return grades


def format_isod_grades(isod_grades):
    formatted_json = {}
    points_sum = 0.0
    items = []

    for column in isod_grades['columns']:
        item = {
            'name': column.get('name', ''),
            'value': column.get('value', ''),
            'weight': column.get('weight', 1),
            'accounted': column.get('accounted', False),
            'value_note': column.get('valueNote', '')
        }

        items.append(item)

        if item['accounted'] and item['value'] != '':
            points_sum += float(item['value']) * item['weight']

    formatted_json['items'] = items
    formatted_json['points_sum'] = points_sum
    return formatted_json


async def get_student_grades(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['http_session']
    usosapi = request.app['usosapi_session']
    device_language = 'en'

    try:
        data = await validate_post_request(request, ['user_token', 'course_id', 'classtype'])
        user_token = data['user_token']
        course_id = data['course_id']
        classtype = data['classtype']

        logging.info(f"Attempting to read student grades for course {course_id}")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.Response(status=400, text=loc.get('user_not_found_info', device_language))

        isod_classtype = convert_usos_to_isod_classtype(classtype)
        final_grade = ''
        grades = {}

        # Check if course is in ISOD
        isod_account = await isod_account_exists(user.reference)
        if isod_account:
            isod_username = isod_account.id
            isod_api_key = isod_account.get('isod_api_key')

            isod_courses = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mycourses&username={isod_username}&apikey={isod_api_key}')
            isod_grades_id, final_grade = get_isod_grades_id(isod_courses, course_id, isod_classtype)

            # Read grades from ISOD
            if isod_grades_id is not None:
                isod_grades = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=myclass&username={isod_username}&apikey={isod_api_key}&id={isod_grades_id}')
                grades = format_isod_grades(isod_grades)

            # Read grades from USOS
            else:
                # TODO grades = fetch_usos_grades()
                pass

        grades = add_envelope(grades, course_id, classtype, final_grade)

        logging.info(f"Created grades json for student: {user.id}")
        return web.json_response(status=200, data=grades)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))
