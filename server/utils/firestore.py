
async def delete_collection(collection_ref):
    docs = await collection_ref.get()
    for doc in docs:
        await doc.reference.delete()


async def user_exists(db, token=None, usos_id=None):
    if token:
        user = await db.collection('users').where('token', '==', token).get()
        return user[0] if user else None
    elif usos_id:
        user = await db.collection('users').document(usos_id).get()
        return user if user.exists else None
    return None


async def isod_account_exists(user_ref):
    isod_account = await user_ref.collection('isod_account').get()
    return isod_account[0] if isod_account else None


async def delete_isod_account(isod_account_ref):
    await delete_collection(isod_account_ref.collection('isod_news'))
    await isod_account_ref.delete()


async def usos_account_exists(user_ref):
    usos_account = await user_ref.collection('usos_account').get()
    return usos_account[0] if usos_account else None


async def delete_usos_account(usos_account_ref):
    await usos_account_ref.delete()
