

class InvalidRequestError(Exception):
    pass


async def validate_post_request(request, required_fields):
    if request.method != 'POST':
        raise InvalidRequestError('This endpoint accepts only POST requests')

    try:
        data = await request.json()
    except Exception:
        raise InvalidRequestError('Invalid JSON format')

    missing_fields = [field for field in required_fields if field not in data]
    if missing_fields:
        raise InvalidRequestError(f'Missing required fields: {", ".join(missing_fields)}')

    return data
