package xenophan.microservice.gameenginecontrollerservice.service;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import xenophan.microservice.gameenginecontrollerservice.model.CreateServiceMessage;
import xenophan.microservice.gameenginecontrollerservice.model.ServiceCreatedMessage;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceCreationService {
    private KubernetesClient client;

    List<Integer> availablePorts = new ArrayList<>();
    List<Integer> takenPorts = new ArrayList<>();
    @Value("${port.range.min}")
    private int portMin;
    @Value("${port.range.max}")
    private int portMax;
    @PostConstruct
    public void ServiceCreationService(){
        client = new KubernetesClientBuilder().build();
        for (int i = portMin; i <= portMax; i++){
            availablePorts.add(i);
        }
    }

    public ServiceCreatedMessage createService(String podName){
        Service testService = client.services().inNamespace("default").withName(podName + "-service").get();
        if (testService!=null) {
            takenPorts.add(testService.getSpec().getPorts().get(0).getNodePort());
            availablePorts.remove(testService.getSpec().getPorts().get(0).getNodePort());
            return ServiceCreatedMessage.builder().port(testService.getSpec().getPorts().get(0).getNodePort()).build();
        }
        if (availablePorts.size() == 0) ServiceCreatedMessage.builder().port(0).build();
        Integer servicePort = availablePorts.get(0);
        takenPorts.add(availablePorts.get(0));
        availablePorts.remove(0);
        Service myService = new ServiceBuilder()
                .withNewMetadata()
                .withName(podName + "-service")
                .addToLabels("app", "game-engine")
                .endMetadata()
                .withNewSpec()
                .withSelector(Collections.singletonMap("statefulset.kubernetes.io/pod-name", podName))
                .withType("NodePort")
                .addNewPort()
                .withName("http-traffic")
                .withProtocol("TCP")
                .withPort(20500)
                .withTargetPort(new IntOrString(20500))
                .withNodePort(servicePort)
                .endPort()
                .endSpec()
                .build();
        myService = client.services().inNamespace("default").create(myService);
        return ServiceCreatedMessage.builder().port(servicePort).build();
    }
}
