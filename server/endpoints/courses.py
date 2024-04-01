import logging

import aiohttp
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, DEFAULT_RESPONSE_LANGUAGE, ENDPOINT_CACHE_TTL
from endpoints.validate_request import validate_post_request, InvalidRequestError
from usosapi.usosapi import USOSAPIAuthorizationError
from utils.classtypes import convert_isod_to_usos_classtype
from utils.firestore import user_exists, isod_account_exists, usos_account_exists, get_device_language
from utils.studies import convert_usos_to_isod_course_id, trim_usos_course_name


def format_isod_courses(isod_courses):
    reformatted_courses = {
        'courses': []
    }

    for course in isod_courses['items']:
        course_info = {
            'name': course['courseName'],
            'course_id': course['courseNumber'],
        }

        classes_list = []

        for class_item in course['classes']:
            class_info = {
                'classtype': convert_isod_to_usos_classtype(class_item['type'])
            }

            classes_list.append(class_info)

        course_info['classes'] = classes_list
        reformatted_courses['courses'].append(course_info)

    return reformatted_courses


def format_usos_courses(usos_courses, semester, language):
    courses = usos_courses['groups'].get(semester, [])

    reformatted_courses = {
        'courses': []
    }

    processed_courses = set()

    for course in courses:
        course_id = course['course_id']
        if course_id not in processed_courses:
            processed_courses.add(course_id)

            course_info = {
                'name': trim_usos_course_name(course['course_name'][language]),
                'course_id': convert_usos_to_isod_course_id(course_id),
                'classes': [
                    {
                        'classtype': class_course['class_type_id']
                    }
                    for class_course in courses if class_course['course_id'] == course_id
                ]
            }

            reformatted_courses['courses'].append(course_info)

    return reformatted_courses


async def read_isod_courses(session, isod_account, semester):
    if not isod_account:
        return None

    isod_username = isod_account.id
    isod_api_key = isod_account.get('isod_api_key')

    isod_courses = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=mycourses&username={isod_username}&apikey={isod_api_key}&semester={semester}')

    return format_isod_courses(isod_courses)


async def read_usos_courses(usosapi, usos_account, semester, language):
    if not usos_account:
        return None

    usos_access_token = usos_account.get('access_token')
    usos_access_token_secret = usos_account.get('access_token_secret')
    usosapi.resume_session(usos_access_token, usos_access_token_secret)

    usos_courses = usosapi.fetch_from_service('services/groups/user', fields='course_id|class_type_id')

    return format_usos_courses(usos_courses, semester, language)


def add_envelope(courses, semester):
    courses['semester'] = semester

    return courses


async def get_student_courses(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    cache_manager = request.app['cache_manager']
    session = request.app['http_session']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'semester'])
        user_token = data['user_token']
        semester = data['semester']
        device_language = loc.validate_language(data.get('language'))

        cache = await cache_manager.get('get_student_courses', user_token, request)
        if cache is not None:
            return web.json_response(status=200, data=cache)

        logging.info(f"Attempting to create student course list")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        # Fetch user accounts
        isod_account = await isod_account_exists(user.reference)
        usos_account = await usos_account_exists(user.reference)

        if usos_account:
            # Read USOS courses
            course_list = await read_usos_courses(usosapi, usos_account, semester, device_language)
        else:
            # Read ISOD courses
            course_list = await read_isod_courses(session, isod_account, semester)

        if course_list is None:
            course_list = {}

        if course_list.get('courses') is None:
            course_list['courses'] = []

        course_list = add_envelope(course_list, semester)

        logging.info(f"Created course list for student: {user.id}")
        await cache_manager.set('get_student_courses', user_token, request, course_list, ttl=ENDPOINT_CACHE_TTL['COURSES'])
        return web.json_response(status=200, data=course_list)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={"message": loc.get('invalid_data_received_form_external_service', device_language)})

    except aiohttp.ClientResponseError as e:
        logging.error(f"HTTP error during ISOD status check (bad request or ISOD credentials): {e}")
        if e.status == 400:
            return web.json_response(status=400, data={"message": loc.get('invalid_isod_auth_data_error', device_language)})
        else:
            return web.json_response(status=e.status, data={"message": loc.get('isod_server_error', device_language)})

    except USOSAPIAuthorizationError:
        logging.info(f"USOSAPI access tokens expired for {usos_account.ic}")
        return web.json_response(status=400, data={"message": loc.get('usos_session_expired_error', device_language)})
