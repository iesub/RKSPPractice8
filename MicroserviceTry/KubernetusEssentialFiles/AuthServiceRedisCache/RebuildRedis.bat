::Принимаем новый конфиг для базы данных пользователей и перезапускаем деплой

kubectl apply -f redis-cash-PV.yaml
kubectl apply -f redis-cash-PVC.yaml
kubectl apply -f redis-cash-deployment.yaml
kubectl apply -f redis-cash-service.yaml
