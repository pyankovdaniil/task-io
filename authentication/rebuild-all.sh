make install
docker rmi task-io-authentication:latest -f
docker rmi pyankovdaniil/task-io-authentication:latest -f
docker build --tag task-io-authentication .
docker tag task-io-authentication pyankovdaniil/task-io-authentication
docker push pyankovdaniil/task-io-authentication
