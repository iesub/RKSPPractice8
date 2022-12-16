::Принимаем новый конфиг для базы данных пользователей и перезапускаем деплой

kubectl apply -f user-service-db-config.yaml
kubectl apply -f user-service-db-PV.yaml
kubectl apply -f user-service-db-PVC.yaml
kubectl apply -f user-service-db-deployment.yaml
kubectl apply -f user-service-db-service.yaml
