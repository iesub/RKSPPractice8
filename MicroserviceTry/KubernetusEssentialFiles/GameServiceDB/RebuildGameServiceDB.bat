::Принимаем новый конфиг для базы данных пользователей и перезапускаем деплой

kubectl apply -f game-service-db-config.yaml
kubectl apply -f game-service-db-PV.yaml
kubectl apply -f game-service-db-PVC.yaml
kubectl apply -f game-service-db-deployment.yaml
kubectl apply -f game-service-db-service.yaml
