import logging
import aiohttp
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from usosapi.usosapi import USOSAPIAuthorizationError
from utils.classtypes import convert_usos_to_isod_classtype
from utils.firestore import user_exists, isod_account_exists, usos_account_exists, delete_isod_account, \
    delete_usos_account
from utils.studies import get_current_semester, is_course_from_ee_faculty


def add_envelope(grades, course_id, classtype, final_grade):
    grades['course_id'] = course_id
    grades['classtype'] = classtype
    grades['final_grade'] = final_grade

    return grades


def get_isod_grades_id(isod_courses, course_id, classtype):
    for course in isod_courses['items']:
        if course.get('courseNumber') == course_id:
            for course_class in course['classes']:
                if course_class.get('type') == classtype:
                    return course_class.get('id'), course.get('finalGradeComment')
    return None, None


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
                points_sum += float(eval(item['value'].replace(',', '.'))) * item['weight']
        except Exception:
            pass

    formatted_json['items'] = items
    formatted_json['points_sum'] = points_sum
    return formatted_json


async def get_isod_course_grades(session, isod_account, course_id, isod_classtype, semester):
    if not isod_account:
        return None, None

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')
    isod_courses = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mycourses&username={isod_username}&apikey={isod_api_key}&semester={semester}')

    isod_grades_id, final_grade = get_isod_grades_id(isod_courses, course_id, isod_classtype)
    if isod_grades_id is not None:
        isod_grades = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=myclass&username={isod_username}&apikey={isod_api_key}&id={isod_grades_id}')
        return final_grade, format_isod_grades(isod_grades)

    return final_grade, None


def get_usos_final_grade(usos_grades):
    try:
        grade = usos_grades.get('course_grades', [{}])[0].get('1')
        return grade.get('value_symbol', '') if grade else ''
    except IndexError:
        return ''


def get_usos_node_root_id(root_nodes, course_id, term_id):
    term_tests = root_nodes.get('tests', {}).get(term_id, {})
    for item_details in term_tests.values():
        if item_details.get('course_edition', {}).get('course_id') == course_id:
            return item_details.get('root_id')
    return None


def format_usos_grades(usosapi, subnodes, language):
    items = []
    for node in subnodes['subnodes']:
        node_id = node.get('id')
        name = node.get('name', {}).get(language, '')
        node_type = node.get('type')
        value = ''
        value_note = ''

        if node_type == 'task':
            task = usosapi.fetch_from_service('services/crstests/student_point', node_id=node_id, fields='points|comment')
            value = task['points']
            value_note = task.get('comment', '')
        elif node_type == 'grade':
            task = usosapi.fetch_from_service('services/crstests/student_grade', node_id=node_id, fields='grade_value|comment')
            value = task['grade_value']['symbol']
            value_note = task.get('comment', '')

        items.append({
            'name': name,
            'value': value,
            'weight': 1,
            'accounted': False,
            'value_note': value_note
        })

    return {'items': items}


async def get_usos_course_grades(usosapi, usos_account, course_id, semester, language):
    if not usos_account:
        return None, None

    usos_access_token = usos_account.get('access_token')
    usos_access_token_secret = usos_account.get('access_token_secret')
    usosapi.resume_session(usos_access_token, usos_access_token_secret)

    usos_final_grade = usosapi.fetch_from_service('services/grades/course_edition2', course_id=course_id, term_id=semester)
    final_grade = get_usos_final_grade(usos_final_grade) if usos_final_grade.get('course_grades') else ''

    root_nodes = usosapi.fetch_from_service('services/crstests/participant')
    root_id = get_usos_node_root_id(root_nodes, course_id, semester)
    if not root_id:
        return final_grade, None

    subnodes = usosapi.fetch_from_service('services/crstests/node2', node_id=root_id, fields='subnodes')
    return final_grade, format_usos_grades(usosapi, subnodes, language)


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

        logging.info(f"Attempting to read student grades for {user_token} course {course_id} {classtype}")

        # Get current semester id
        semester = get_current_semester(usosapi) if not semester else semester

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist: {user_token}")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        isod_classtype = convert_usos_to_isod_classtype(classtype)

        # Check if it's ISOD or USOS course
        if is_course_from_ee_faculty(course_id):
            # Fetch grades from ISOD
            isod_account = await isod_account_exists(user.reference)
            final_grade, grades = await get_isod_course_grades(session, isod_account, course_id, isod_classtype, semester)
        else:
            # Fetch grades from USOS
            usos_account = await usos_account_exists(user.reference)
            final_grade, grades = await get_usos_course_grades(usosapi, usos_account, course_id, semester, device_language)

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
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={"message": loc.get('invalid_data_received_form_external_service', device_language)})

    except aiohttp.ClientResponseError as e:
        if e.status == 400:
            logging.info(f"ISOD API key expired for {isod_account.id}")
            await delete_isod_account(isod_account.reference)
            return web.json_response(status=400, data={"message": loc.get('isod_api_key_expired', device_language)})
        else:
            logging.error(f"HTTP error: {e}")
            return web.json_response(status=e.status, data={"message": loc.get('isod_server_error', device_language)})

    except USOSAPIAuthorizationError:
        logging.info(f"USOSAPI access tokens expired for {usos_account.id}")
        await delete_usos_account(usos_account.reference)
        return web.json_response(status=400, data={"message": loc.get('usos_session_expired_error', device_language)})
