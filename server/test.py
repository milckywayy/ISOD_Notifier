import requests

# url = 'https://localhost:8080/link_isod_account'  # Zastąp 'port' i 'endpoint' odpowiednimi wartościami
# data = {
#     'token_fcm': 'frwcC6IuT2G5vRkLErFX37:APA91bFt9Qh1yyQojFVi_O42ya9lyMfqDXfPccZTDMumhJsZHeW9IUyuPbymcvI4clSmqvDkd5fHqXY5QwPjJYWfU-gmUMD3N3i5gtbG1sHuKTP94KMGkQDqnKdxUIRTVbDB8CSRibZN',
#     'isod_username': 'fraczem1',
#     'isod_api_key': 'Z1oHo869l1GmpxC86PbUGg',
#     'app_version': '0.0.1',
#     'device_language': 'en',
#     'news_filter': '15',
# }

# url = 'https://localhost:8080/unlink_isod_account'
# data = {
#     'user_token': 'b4795747744a052ceb61a74659639122303e6cb38f4c99bbfdfd4491ac63d64e',
# }

url = 'https://localhost:8080/get_isod_link_status'
data = {
    'user_token': 'b4795747744a052ceb61a74659639122303e6cb38f4c99bbfdfd4491ac63d64e',
}

# W przypadku problemów z certyfikatem SSL, możesz dodać verify=False, ale nie jest to zalecane ze względów bezpieczeństwa
response = requests.post(url, json=data, verify=False)  # Użyj verify=False tylko jeśli masz problem z certyfikatem

print(response.text)
