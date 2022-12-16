kubectl apply -f game-engine-controller-config.yaml
kubectl apply -f game-engine-controller-account.yaml
kubectl apply -f cluster-role.yaml
kubectl apply -f game-engine-controller.yaml
kubectl create clusterrolebinding game-engine-controller-role --clusterrole=edit --serviceaccount=default:game-service-controller-account --namespace=default
kubectl apply -f game-engine-controller-service.yaml
pause