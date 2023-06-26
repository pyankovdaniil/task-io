import requests
import sys

baseUrl = ""

USAGE_INFO = "Usage: python3 send_requests.py [hostname].\n" +\
             "If no args passed - tries to read from file \"server.txt\"\n" +\
             "Hostname should be \"http(s)://ip:port\""
def printErrorWithUsageInfo(error_text: str):
    print("ERROR: " + error_text + "\n\n" + USAGE_INFO + "\n")


if len(sys.argv) > 2:
    printErrorWithUsageInfo("Wrong number of arguments!")
    exit(1)

if len(sys.argv) == 1:
    print("No command line args found. Reading from file.")
    try:
        f = open("server.txt")
    except OSError:
        printErrorWithUsageInfo("Can not open the file \"server.txt\"!")
        exit(1)
    baseUrl = f.readline().strip()
    f.close()
    if baseUrl == "":
        printErrorWithUsageInfo("Empty file!")
        exit(1)

elif len(sys.argv) == 2:
    print("Found url in args.")
    baseUrl = sys.argv[1]
    if baseUrl == "":
        printErrorWithUsageInfo("Empty baseUrl!")
        exit(1)

else:
    printErrorWithUsageInfo("Cant even imagine how you did get there...")
    exit(1)

print("Got url: \"" + baseUrl + "\"")
print("Start testing...\n\n")


response = requests.post(baseUrl + "/rest/api/v1/auth/register", json={
    "email": "test@email.com",
    "password": "passwordD123A!",
    "fullName": "Harry Potter"
})

print(f"Registration request done. Status Code: {response.status_code}, Response: {response.json()}")

response = requests.post(baseUrl + "/rest/api/v1/auth/authenticate", json={
    "email": "test@email.com",
    "password": "passwordD123A!"
})

print(f"Authentication request done. Status Code: {response.status_code}, Response: {response.json()}")

accessToken = response.json().get('accessToken')

headers = {'Authorization': 'Bearer ' + accessToken}
<<<<<<< HEAD
print(headers)
response = requests.post(baseUrl + "/rest/api/v1/projects/create", json={
=======
response = requests.post("http://192.168.49.2:80/rest/api/v1/projects/create", json={
    "name": "task.io",
    "description": "Tool for development in teams"
}, headers=headers)

print(f"Create project request done. Status Code: {response.status_code}, Response: {response.json()}")

response = requests.post("http://192.168.49.2:80/rest/api/v1/projects/create", json={
>>>>>>> 2d46eef (Fixed /create endpoint in projects microservice)
    "name": "task.io",
    "description": "Tool for development in teams"
}, headers=headers)

print(f"Create project request done. Status Code: {response.status_code}, Response: {response.json()}")
