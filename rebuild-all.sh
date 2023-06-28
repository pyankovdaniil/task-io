helm uninstall task-io
cd ./utils
make install
cd ../amqp
make install
cd ../notification
./rebuild-all.sh
cd ../authentication
./rebuild-all.sh
cd ../projects
./rebuild-all.sh
cd ../kubernetes
helm install task-io ./task-io-chart
