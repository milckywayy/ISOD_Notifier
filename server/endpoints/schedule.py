import logging
from datetime import datetime, timedelta

from aiohttp import web

from asynchttp.async_http_request import async_get_request
from constants import ISOD_PORTAL_URL, EE_USOS_ID
from endpoints.validate_request import InvalidRequestError, validate_post_request


def get_date():
    today = datetime.now()
    formatted_today = today.strftime("%Y-%m-%d")

    one_week_later = today + timedelta(days=7)
    formatted_one_week_later = one_week_later.strftime("%Y-%m-%d")

    return formatted_today, formatted_one_week_later


def integrate_schedules(isod_schedule, usos_schedule, days_off):
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

        today, next_week = get_date()

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

            usos_schedule = usosapi.fetch_from_service('services/tt/user', fields='start_time|end_time|name|course_id|classtype_id|frequency')
            days_off = usosapi.fetch_from_service('services/calendar/search', faculty_id=EE_USOS_ID, start_date=today, end_date=next_week)

        # Integrate schedules
        final_schedule = integrate_schedules(isod_schedule, usos_schedule, days_off)

        logging.info(f"Created schedule for student: {student_number}")
        return web.Response(status=200)

    except InvalidRequestError as e:
        logging.info(f"Invalid request received: {e}")
        return web.Response(status=400, text=loc.get('invalid_input_data_error', device_language))
