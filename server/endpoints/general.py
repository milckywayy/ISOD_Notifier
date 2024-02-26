import logging
import secrets


async def validate_post_request(request, required_fields):
    if request.method != 'POST':
        return False, "This endpoint accepts only POST requests."

    try:
        data = await request.json()
    except Exception:
        return False, "Invalid JSON format."

    missing_fields = [field for field in required_fields if field not in data]
    if missing_fields:
        return False, f"Missing required fields: {', '.join(missing_fields)}"

    return True, ""


async def create_user(db, index, firstname):

    # Check if user exists
    user = await db.collection('users').document(index).get()
    if user.exists:
        # Get token from db
        logging.info(f"Such user already exists: {index}")
        user_token = user.get('token')

    else:
        # Add user and generate new token
        logging.info(f"Adding new user: {index}")
        user_token = secrets.token_hex(32)

        # Add user (index, name)
        await db.collection('users').document(index).set({
            'first_name': firstname,
            'token': user_token,
        })

    return user_token
