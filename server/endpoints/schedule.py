import logging
from datetime import datetime, timedelta

from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, EE_USOS_ID
from endpoints.validate_request import InvalidRequestError, validate_post_request


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
            "name": {"pl": item['courseName'], "en": item['courseName']},
            "courseId": item['courseNumber'],
            "typeOfClasses": item['typeOfClasses'],
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


def format_usos_schedule(input_data):
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
            "name": {"pl": item['name']['pl'].split(' - ')[0], "en": item['name']['en'].split(' - ')[0]},
            "courseId": item['course_id'].split('-')[-1],
            "typeOfClasses": item['classtype_id'],
            "building": item['building_id'].split('-')[1],
            "room": item['room_number'],
            "note": '',
            "isActive": "1"
        }

        days[day_of_week]["lessons"].append(lesson)

    formatted_data["classes"] = list(days.values())
    return formatted_data


def integrate_schedules(isod_schedule, usos_schedule, days_off):
    print(format_isod_schedule(isod_schedule))
    print(format_usos_schedule(usos_schedule))

    if isod_schedule == '' and usos_schedule == '':
        raise RuntimeError("Schedule couldn't be created")

    elif isod_schedule == '':
        return format_usos_schedule(usos_schedule)

    elif usos_schedule == '':
        return format_isod_schedule(isod_schedule)

    # TODO combine schedules

    return ''


async def get_student_schedule(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    session = request.app['http_session']
    usosapi = request.app['usosapi_session']
    device_language = 'en'

    try:
        data = await validate_post_request(request, ['user_token'])
        user_token = data['user_token']

        logging.info(f"Attempting to create student schedule")

        # Check if user exists
        user = await db.collection('users').where('token', '==', user_token).get()
        if not user:
            logging.info(f"Such user does not exist")
            return web.Response(status=400, text=loc.get('user_not_found_info', device_language))
        user = user[0]
        student_number = user.id

        isod_schedule = ''
        usos_schedule = ''
        days_off = ''

        monday, friday = get_week_start_end()

        # Get ISOD schedule
        isod_account = await user.reference.collection('isod_account').get()
        if isod_account:
            isod_account = isod_account[0]
            isod_username = isod_account.id
            isod_api_key = isod_account.get('isod_api_key')

            isod_schedule = await async_get_request(session, ISOD_PORTAL_URL + f'/wapi?q=myplan&username={isod_username}&apikey={isod_api_key}')

        # Get USOS schedule
        usos_account = await user.reference.collection('usos_account').get()
        if usos_account:
            usos_account = usos_account[0]
            usos_access_token = usos_account.get('access_token')
            usos_access_token_secret = usos_account.get('access_token_secret')

            usosapi.resume_session(usos_access_token, usos_access_token_secret)

            usos_schedule = usosapi.fetch_from_service('services/tt/user', fields='start_time|end_time|name|course_id|classtype_id|frequency|room_number|building_id')
            days_off = usosapi.fetch_from_service('services/calendar/search', faculty_id=EE_USOS_ID, start_date=monday, end_date=friday)

        # Integrate schedules
        final_schedule = integrate_schedules(isod_schedule, usos_schedule, days_off)

        logging.info(f"Created schedule for student: {student_number}")
        return web.Response(status=200)

    except InvalidRequestError as e:
        logging.info(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))
