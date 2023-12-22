import requests


def get_request(username, api_key):
    try:
        response = requests.get(f'https://isod.ee.pw.edu.pl/isod-portal/wapi?q=mynewsheaders&username={username}&apikey={api_key}&from=0&to=3')

        return response.json()

    except requests.exceptions.HTTPError as err:
        raise requests.exceptions.RequestException("Http Error:", err)
    except requests.exceptions.ConnectionError as err:
        raise requests.exceptions.RequestException("Error Connecting:", err)
    except requests.exceptions.Timeout as err:
        raise requests.exceptions.RequestException("Timeout Error:", err)
    except requests.exceptions.RequestException as err:
        raise requests.exceptions.RequestException("OOps: Something went wrong:", err)
