import requests
import sys

baseUrl = ""

if len(sys.argv) > 2:
    print("Wrong number of arguments!\n" +
           "Usage: python3 send_requests.py [hostname].\n" +
           "If no args passed - tries to read from file \"server.txt\"\n" +
           "Hostname should be \"http(s)://ip:port\"\n")
    exit(1)

if len(sys.argv) == 1:
    print("Reading from file")
    try:
        f = open("server.txt")
    except OSError:
        print("Can not open the file \"server.txt\"!")
        exit(1)
    baseUrl = f.readline().strip()
    f.close()
    if baseUrl == "":
        print("Empty file!")
        exit(1)

elif len(sys.argv) == 2:
    print("Found url in args.")
    baseUrl = sys.argv[1]
    if baseUrl == "":
        print("Empty baseUrl!")
        exit(1)

else:
    print("Cant even imagine how you did get there...")
    exit(1)

print("Got url: \"" + baseUrl + "\"")
print("Start testing...\n\n")


response = requests.post(baseUrl + "/rest/api/v1/auth/register", json={
    "email": "test@email.com",
    "password": "password",
    "fullName": "Harry Potter"
})

print(f"Registration request done. Status Code: {response.status_code}, Response: {response.json()}")

response = requests.post(baseUrl + "/rest/api/v1/auth/authenticate", json={
    "email": "test@email.com",
    "password": "password"
})

print(f"Authentication request done. Status Code: {response.status_code}, Response: {response.json()}")

accessToken = response.json().get('accessToken')

headers = {'Authorization': 'Bearer ' + accessToken}
print(headers)
response = requests.post(baseUrl + "/rest/api/v1/projects/create", json={
    "name": "task.io",
    "description": "Tool for development in teams"
}, headers=headers)

print(f"Create project request done. Status Code: {response.status_code}, Response: {response.json()}")
