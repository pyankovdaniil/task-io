helm uninstall task-io
cd ./utils
make install
cd ../amqp
make install
cd ../notification
./rebuild-all-no-push.sh
cd ../authentication
./rebuild-all-no-push.sh
cd ../projects
./rebuild-all-no-push.sh
cd ../kubernetes
helm install task-io ./task-io-chart
