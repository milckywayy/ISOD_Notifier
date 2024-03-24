import logging
from datetime import datetime, timedelta
from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, EE_USOS_ID, EE_USOS_ID_IN_COURSE, DEFAULT_RESPONSE_LANGUAGE
from endpoints.validate_request import InvalidRequestError, validate_post_request
from utils.classtypes import convert_isod_to_usos_classtype
from utils.firestore import user_exists, usos_account_exists, isod_account_exists
from utils.studies import convert_usos_to_isod_course_id, trim_usos_course_name


def get_week_start_end():
    today = datetime.now()
    weekday = today.weekday()

    if weekday >= 5:
        today += timedelta(days=(7-weekday))

    weekday = today.weekday()

    start_delta = timedelta(days=-weekday)
    start_of_week = today + start_delta

    end_delta = timedelta(days=4-weekday)
    end_of_week = today + end_delta

    formatted_start_of_week = start_of_week.strftime("%Y-%m-%d")
    formatted_end_of_week = end_of_week.strftime("%Y-%m-%d")

    return formatted_start_of_week, formatted_end_of_week


def convert_time(time_str, input_format, output_format='%H:%M'):
    time_obj = datetime.strptime(time_str, input_format)
    return time_obj.strftime(output_format)


def format_isod_schedule(data):
    formatted_data = {"classes": []}
    days = {str(i): [] for i in range(1, 8)}

    for item in data['planItems']:
        if not item['teachers'] or item['cycle'] == 'Cykl inny':
            continue

        lesson = {
            "startTime": convert_time(item['startTime'], '%I:%M:%S %p'),
            "endTime": convert_time(item['endTime'], '%I:%M:%S %p'),
            "name": item['courseName'],
            "courseId": item['courseNumber'],
            "typeOfClasses": convert_isod_to_usos_classtype(item['typeOfClasses']),
            "building": item['buildingShort'],
            "room": item['room'],
            "note": item['notesTeachers'] if 'notesTeachers' in item else '',
            "isActive": "1"
        }

        days[item['dayOfWeek']].append(lesson)

    for day, lessons in days.items():
        if lessons:
            formatted_data["classes"].append({"dayOfWeek": day, "isDayOff": "0", "lessons": lessons})

    return formatted_data


def format_usos_schedule(input_data, language):
    formatted_data = {"classes": []}
    days = {}

    for item in input_data:
        if 'frequency' in item and item['frequency'] == 'other':
            continue

        start_time = convert_time(item['start_time'], '%Y-%m-%d %H:%M:%S')
        end_time = convert_time(item['end_time'], '%Y-%m-%d %H:%M:%S')
        day_of_week = datetime.strptime(item['start_time'], '%Y-%m-%d %H:%M:%S').strftime('%u')

        if day_of_week not in days:
            days[day_of_week] = {"dayOfWeek": day_of_week, "isDayOff": "0", "lessons": []}

        lesson = {
            "startTime": start_time,
            "endTime": end_time,
            "name": trim_usos_course_name(item['name'][language]),
            "courseId": convert_usos_to_isod_course_id(item['course_id']),
            "typeOfClasses": item['classtype_id'],
            "building": item['building_id'].split('-')[1],
            "room": item['room_number'],
            "note": '',
            "isActive": "1"
        }

        days[day_of_week]["lessons"].append(lesson)

    formatted_data["classes"] = list(days.values())
    return formatted_data


def read_days_off(date_ranges):
    days_of_week = set()

    for date_range in date_ranges:
        start_date_str = date_range['start_date']
        end_date_str = date_range['end_date']

        start_date = datetime.strptime(start_date_str, '%Y-%m-%d %H:%M:%S')
        end_date = datetime.strptime(end_date_str, '%Y-%m-%d %H:%M:%S')

        current_date = start_date
        while current_date <= end_date:
            days_of_week.add(str(current_date.isoweekday()))
            current_date += timedelta(days=1)

    return list(days_of_week)


def merge_schedules(isod_schedule, usos_schedule, days_off, language):
    if isod_schedule == '' and usos_schedule == '':
        raise RuntimeError("At least one account should be linked in order to create schedule")

    elif isod_schedule == '':
        return format_usos_schedule(usos_schedule)

    elif usos_schedule == '':
        return format_isod_schedule(isod_schedule)

    final_schedule = {'classes': []}
    days_off = read_days_off(days_off)
    isod_schedule = format_isod_schedule(isod_schedule)
    usos_schedule = format_usos_schedule(usos_schedule, language)
    isod_days = {day['dayOfWeek']: day for day in isod_schedule['classes']}
    usos_days = {day['dayOfWeek']: day for day in usos_schedule['classes']}

    all_days = sorted(set(isod_days.keys()) | set(usos_days.keys()), key=int)

    for day in all_days:
        final_day = {'dayOfWeek': day, 'isDayOff': '0', 'lessons': []}
        if day in days_off:
            final_day['isDayOff'] = '1'

        isod_lessons = {lesson['courseId']: lesson for lesson in isod_days.get(day, {}).get('lessons', [])}
        usos_lessons = {lesson['courseId']: lesson for lesson in usos_days.get(day, {}).get('lessons', [])}

        all_course_ids = set(isod_lessons.keys()) | set(usos_lessons.keys())

        for course_id in all_course_ids:
            if course_id in isod_lessons and course_id in usos_lessons:
                lesson = usos_lessons[course_id].copy()
                lesson['note'] = isod_lessons[course_id]['note']
            elif course_id in isod_lessons:
                lesson = isod_lessons[course_id].copy()
                lesson['isActive'] = '0'
            else:
                lesson = usos_lessons[course_id].copy()

            final_day['lessons'].append(lesson)

        final_schedule['classes'].append(final_day)

    return final_schedule


async def get_student_schedule(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['http_session']
    usosapi = request.app['usosapi_session']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Attempting to create student schedule")

        # Check if user exists
        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist")
            return web.Response(status=400, text=loc.get('user_not_found_info', device_language))

        isod_schedule = ''
        usos_schedule = ''
        days_off = ''

        monday, friday = get_week_start_end()

        # Get ISOD schedule
        isod_account = await isod_account_exists(user.reference)
        if isod_account:
            isod_username = isod_account.id
            isod_api_key = isod_account.get('isod_api_key')

            isod_schedule = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=myplan&username={isod_username}&apikey={isod_api_key}')

        # Get USOS schedule
        usos_account = await usos_account_exists(user.reference)
        if usos_account:
            usos_access_token = usos_account.get('access_token')
            usos_access_token_secret = usos_account.get('access_token_secret')

            usosapi.resume_session(usos_access_token, usos_access_token_secret)
            usos_schedule = usosapi.fetch_from_service('services/tt/user', start=monday, days=5, fields='start_time|end_time|name|course_id|classtype_id|frequency|room_number|building_id')
            days_off = usosapi.fetch_from_service('services/calendar/search', faculty_id=EE_USOS_ID, start_date=monday, end_date=friday, fields='start_date|end_date')

        # Merge schedules
        final_schedule = merge_schedules(isod_schedule, usos_schedule, days_off, device_language)

        logging.info(f"Created schedule for student: {user.id}")
        return web.json_response(status=200, data=final_schedule)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.Response(status=502, text=loc.get('invalid_data_received_form_external_service', device_language))

    except RuntimeError as e:
        logging.error(f"Couldn't create schedule: {e}")
        return web.Response(status=500, text=loc.get('internal_server_error', device_language))
