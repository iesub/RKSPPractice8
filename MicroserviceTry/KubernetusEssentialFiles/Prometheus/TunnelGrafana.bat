@echo off
kubectl get secret grafana-admin --namespace default -o jsonpath="{.data.GF_SECURITY_ADMIN_PASSWORD}" > ./pass.txt
certutil -decode ./pass.txt ./pass_dec.txt >NUL
echo GrafanaPassword:
type pass_dec.txt
echo:

del pass.txt >NUL
del pass_dec.txt >NUL

echo Grafana tunnel created
echo:

kubectl port-forward svc/grafana 8080:3000

pause