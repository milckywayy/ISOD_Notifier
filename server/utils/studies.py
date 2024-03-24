from datetime import datetime


def get_current_semester(usosapi):
    today = datetime.now().strftime('%Y-%m-%d')

    response = usosapi.fetch_anonymously_from_service('services/terms/search', min_finish_date=today, max_start_date=today, query='semester')

    return response[0]['id']

