helm uninstall task-io
cd ./utils
make install
cd ../authentication
./rebuild-all.sh
cd ../projects
./rebuild-all.sh
cd ../kubernetes
helm install task-io ./task-io-chart
