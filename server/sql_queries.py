CLIENT_EXISTS_QUERY = '''SELECT EXISTS(SELECT 1 FROM clients WHERE username = ?)'''
DEVICE_EXISTS_QUERY = '''SELECT EXISTS(SELECT 1 FROM devices WHERE token = ?)'''
DEVICE_COUNT_QUERY = '''SELECT COUNT(token) FROM devices WHERE username = ?'''

GET_CLIENTS_QUERY = '''SELECT username, api_key FROM clients'''
GET_DEVICES_QUERY = '''SELECT token FROM devices WHERE username = ?'''
GET_NEWS_QUERY = '''SELECT hash FROM news WHERE username = ?'''
GET_DEVICE_VERSION_QUERY = '''SELECT version FROM devices WHERE token = ?'''
GET_API_KEY_QUERY = '''SELECT api_key FROM clients WHERE username = ?'''

UPDATE_API_KEY_QUERY = '''UPDATE clients SET api_key = ? WHERE username = ?'''
UPDATE_VERSION_QUERY = '''UPDATE devices SET version = ? WHERE token = ?'''

INSERT_CLIENT_QUERY = '''INSERT INTO clients VALUES (?, ?)'''
INSERT_DEVICE_QUERY = '''INSERT INTO devices VALUES (?, ?, ?, ?)'''
INSERT_NEWS_QUERY = '''INSERT INTO news (username, hash, type) VALUES (?, ?, ?)'''

DELETE_DEVICE_QUERY = '''DELETE FROM devices WHERE token = ?'''
DELETE_CLIENT_QUERY = '''DELETE FROM clients WHERE username = ?'''
DELETE_NEWS_QUERY = '''DELETE FROM NEWS WHERE username = ?'''
DELETE_ONE_NEWS_QUERY = '''DELETE FROM news WHERE username = ? AND hash = ?'''
