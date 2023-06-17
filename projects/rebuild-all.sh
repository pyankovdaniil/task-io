make build
docker rmi task-io-projects:latest -f
docker rmi pyankovdaniil/task-io-projects:latest -f
docker build --tag task-io-projects .
docker tag task-io-projects pyankovdaniil/task-io-projects
docker push pyankovdaniil/task-io-rojects
cd ../kubernetes
./delete-all.sh
./apply-all.sh
