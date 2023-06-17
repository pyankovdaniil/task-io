kubectl apply -f ./mongo-config.yaml
kubectl apply -f ./mongo-secret.yaml
kubectl apply -f ./mongo.yaml
kubectl apply -f ./redis.yaml
kubectl apply -f ./redis-config.yaml
kubectl apply -f ./authentication.yaml
kubectl apply -f ./projects.yaml
kubectl apply -f ./ingress.yaml
