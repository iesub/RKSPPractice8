@echo off
helm repo add bitnami https://charts.bitnami.com/bitnami > NUL 2>NUL
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts > NUL 2>NUL
helm repo update > NUL 2>NUL

helm install prometheus bitnami/kube-prometheus >NUL 2>NUL
helm install -f values.yaml prometheus-adapter prometheus-community/prometheus-adapter > NUL 2>NUL
helm install grafana bitnami/grafana >NUL 2>NUL

kubectl apply -f prometheus-service-monitor.yaml
kubectl apply -f prometheus-adapter-conf.yaml
kubectl rollout restart deployment prometheus-adapter
kubectl create -f api-service.yaml

pause