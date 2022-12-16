kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.6.1/aio/deploy/recommended.yaml
kubectl apply -f service-account.yaml
kubectl apply -f cluster-role-binding.yaml
kubectl -n kubernetes-dashboard create token admin-user
kubectl proxy
pause