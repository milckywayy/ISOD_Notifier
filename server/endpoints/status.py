import logging

from aiohttp import web

from constants import DEFAULT_RESPONSE_LANGUAGE
from endpoints.validate_request import InvalidRequestError, validate_post_request
from utils.firestore import user_exists, isod_account_exists, usos_account_exists, device_exists


async def update_device(device_ref, language, app_version):
    await device_ref.update({
        'language': language,
        'app_version': app_version
    })


async def get_user_status(request):
    loc = request.app['localization_manager']
    db = request.app['database_manager']
    device_language = DEFAULT_RESPONSE_LANGUAGE

    try:
        data = await validate_post_request(request, ['user_token', 'token_fcm', 'app_version'])
        user_token = data['user_token']
        token_fcm = data['token_fcm']
        app_version = data['app_version']
        device_language = loc.validate_language(data.get('language'))

        logging.info(f"Check user status for {user_token}")

        is_isod_linked = False
        is_usos_linked = False

        user = await user_exists(db, token=user_token)
        if not user:
            logging.error(f"Such user does not exist: {user_token}")
            return web.json_response(status=400, data={'message': loc.get('user_not_found_info', device_language)})

        isod_account = await isod_account_exists(user.reference)
        if isod_account is not None:
            is_isod_linked = True

        usos_account = await usos_account_exists(user.reference)
        if usos_account is not None:
            is_usos_linked = True

        device = await device_exists(user.reference, token_fcm)
        if device is not None:
            if device_language != device.get('language') or app_version != device.get('app_version'):
                logging.info(f'Updating {user.id}\'s device data: {token_fcm}. ({device.get("app_version")}, {device.get("language")}) -> ({app_version}, {device_language})')
                await update_device(device.reference, device_language, app_version)

        status = {
            'is_isod_linked': is_isod_linked,
            'is_usos_linked': is_usos_linked
        }
        logging.info(f"User ({user.id}) status: isod({is_isod_linked}), usos({is_usos_linked})")
        return web.json_response(status=200, data=status)

    except InvalidRequestError as e:
        logging.error(f"Invalid request received: {e}")
        return web.json_response(status=400, data={"message": loc.get('invalid_input_data_error', device_language)})

    except KeyError as e:
        logging.error(f"Invalid data received from external service: {e}")
        return web.json_response(status=502, data={"message": loc.get('invalid_data_received_form_external_service', device_language)})

    except RuntimeError as e:
        logging.error(f"Couldn't create schedule: {e}")
        return web.json_response(status=500, data={"message": loc.get('internal_server_error', device_language)})
