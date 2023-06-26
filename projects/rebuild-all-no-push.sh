make install
docker rmi task-io-projects:1.0.0 -f
docker rmi pyankovdaniil/task-io-projects:1.0.0 -f
minikube image rm docker.io/pyankovdaniil/task-io-projects:1.0.0
docker build --tag task-io-projects:1.0.0 . --force-rm
docker tag task-io-projects:1.0.0 pyankovdaniil/task-io-projects:1.0.0
docker rmi $(docker images -f "dangling=true" -q)

