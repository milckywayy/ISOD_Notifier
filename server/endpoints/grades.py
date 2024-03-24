import logging

import aiohttp
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from usosapi.usosapi import USOSAPIAuthorizationError
from utils.classtypes import convert_usos_to_isod_classtype
from utils.firestore import user_exists, isod_account_exists, usos_account_exists
from utils.studies import get_current_semester, is_usos_course


def get_isod_grades_id(isod_courses, course_id, classtype):
    for course in isod_courses['items']:
        if course.get('courseNumber', '') != course_id:
            continue

        for course_class in course['classes']:
            if course_class.get('type', '') == classtype:
                return course_class.get('id'), course.get('finalGradeComment', '')

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

        try:
            if item['accounted'] and item['value'] != '':
                points_sum += float(item['value'].replace(',', '.')) * item['weight']
        except Exception:
            pass

    formatted_json['items'] = items
    formatted_json['partial_grade'] = isod_grades.get('credit', '')
    formatted_json['points_sum'] = points_sum
    return formatted_json


def get_usos_final_grade(usos_grades):
    grade = usos_grades['course_grades'][0]['1']
    return grade['value_symbol'] if grade is not None else ''


def get_usos_grade_name(usosapi, grade_id):
    grade_info = usosapi.fetch_from_service('services/examrep2/examrep', examrep_id=grade_id, fields='description')
    return grade_info['description']['pl']


def format_usos_grades(usosapi, usos_grades):
    formatted_json = {}
    items = []

    for unit, grades in usos_grades['course_units_grades'].items():
        for grade in grades:
            for key, value in grade.items():
                if value is None:
                    continue

                name = get_usos_grade_name(usosapi, value['exam_id'])

                item = {
                    'name': name,
                    'value': value['value_symbol']
                }

                items.append(item)

    formatted_json['items'] = items
    return formatted_json


async def get_isod_course_grades(session, isod_account, course_id, isod_classtype, semester):
    if not isod_account:
        return None, None

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')

    isod_courses = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mycourses&username={isod_username}&apikey={isod_api_key}&semester={semester}')
    isod_grades_id, final_grade = get_isod_grades_id(isod_courses, course_id, isod_classtype)

    # Read grades from ISOD
    if isod_grades_id is not None:
        isod_grades = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=myclass&username={isod_username}&apikey={isod_api_key}&id={isod_grades_id}')
        return final_grade, format_isod_grades(isod_grades)

    return final_grade, None


async def get_usos_course_grades(usosapi, usos_account, course_id, semester):
    if not usos_account:
        return None, None

    usos_access_token = usos_account.get('access_token')
    usos_access_token_secret = usos_account.get('access_token_secret')
    usosapi.resume_session(usos_access_token, usos_access_token_secret)

    usos_grades = usosapi.fetch_from_service('services/grades/course_edition2', course_id=course_id, term_id=semester)

    if usos_grades['course_grades']:
        final_grade = get_usos_final_grade(usos_grades)
        return final_grade, format_usos_grades(usosapi, usos_grades)

    return None, None


async def get_student_grades(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    cache_manager = request.app['cache_manager']
    session = request.app['http_session']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'course_id', 'classtype'])
        user_token = data['user_token']
        course_id = data['course_id']
        classtype = data['classtype']
        semester = data['semester']
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_student_grades', user_token, request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        logging.info(f"Attempting to read student grades for course {course_id} {classtype}")

        # Get current semester id
        semester = get_current_semester(usosapi) if not semester else semester

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.Response(status=400, text=loc.get('user_not_found_info', device_language))

        isod_classtype = convert_usos_to_isod_classtype(classtype)

        # Check if it's ISOD or USOS course
        if is_usos_course(course_id):
            # Fetch grades from USOS
            usos_account = await usos_account_exists(user.reference)
            final_grade, grades = await get_usos_course_grades(usosapi, usos_account, course_id, semester)
        else:
            # Fetch grades from ISOD
            isod_account = await isod_account_exists(user.reference)
            final_grade, grades = await get_isod_course_grades(session, isod_account, course_id, isod_classtype, semester)

        if final_grade is None:
            final_grade = ''

        if grades is None:
            grades = {}

        if grades.get('items') is None:
            grades['items'] = []

        grades = add_envelope(grades, course_id, classtype, final_grade)

        logging.info(f"Created grade list for student: {user.id}")
        await cache_manager.set('get_student_grades', user_token, request, grades, ttl=ENDPOINT_CACHE_TTL['GRADES'])
        return web.json_response(status=200, data=grades)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.Response(status=502, text=loc.get('invalid_data_received_form_external_service', device_language))

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during ISOD status check (bad request or ISOD credentials): {e}")
        if e.status == 400:
            return web.Response(status=400, text=loc.get('invalid_isod_auth_data_error', device_language))
        else:
            return web.Response(status=e.status, text=loc.get('isod_server_error', device_language))

    except USOSAPIAuthorizationError:
        logging.info(f"USOSAPI access tokens expired for {usos_account.ic}")
        return web.Response(status=400, text='0')
