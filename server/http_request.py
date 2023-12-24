import requests


def get_request(url):
    try:
        response = requests.get(url)

        return response.json()

    except requests.exceptions.HTTPError as err:
        raise requests.exceptions.RequestException("Http Error:", err)
    except requests.exceptions.ConnectionError as err:
        raise requests.exceptions.RequestException("Error Connecting:", err)
    except requests.exceptions.Timeout as err:
        raise requests.exceptions.RequestException("Timeout Error:", err)
    except requests.exceptions.RequestException as err:
        raise requests.exceptions.RequestException("OOps: Something went wrong:", err)
