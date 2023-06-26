helm uninstall task-io
cd ./utils
make install
cd ../authentication
./rebuild-all-no-push.sh
cd ../projects
./rebuild-all-no-push.sh
cd ../kubernetes
helm install task-io ./task-io-chart
