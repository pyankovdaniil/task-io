import requests

response = requests.post("http://192.168.49.2:80/rest/api/v1/auth/register", json={
    "email": "test@email.com",
    "password": "password",
    "fullName": "Harry Potter"
})

print(f"Registration request done. Status Code: {response.status_code}, Response: {response.json()}")

response = requests.post("http://192.168.49.2:80/rest/api/v1/auth/authenticate", json={
    "email": "test@email.com",
    "password": "password"
})

print(f"Authentication request done. Status Code: {response.status_code}, Response: {response.json()}")

accessToken = response.json().get('accessToken')

headers = {'Authorization': 'Bearer ' + accessToken}
print(headers)
response = requests.post("http://192.168.49.2:80/rest/api/v1/projects/create", json={
    "name": "task.io",
    "description": "Tool for development in teams"
}, headers=headers)

print(f"Create project request done. Status Code: {response.status_code}, Response: {response.json()}")
