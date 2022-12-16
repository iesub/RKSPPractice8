package xenophan.microservice.gameenginecontrollerservice.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xenophan.microservice.gameenginecontrollerservice.model.CreateSceneMessage;
import xenophan.microservice.gameenginecontrollerservice.model.SceneCreatedMessage;
import xenophan.microservice.gameenginecontrollerservice.model.User;

import java.util.List;
import java.util.Random;

@Component
public class GameServiceMessageProducerService {

    @Autowired
    private EurekaClient eurekaClient;

    @LoadBalanced
    private final RestTemplate restTemplate = new RestTemplate();

    public SceneCreatedMessage sendToQueue(CreateSceneMessage sceneMessage, User user, String appName, String bearer) throws Exception {
        sceneMessage.setUser(user);

        Random random = new Random();
        Application application = eurekaClient.getApplication(appName);
        List<InstanceInfo> instanceInfo = application.getInstances();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearer);
        int listSize = instanceInfo.size();
        for(int i = 0; i < listSize; i++){
            InstanceInfo info = instanceInfo.get(random.nextInt(instanceInfo.size()));
            HttpEntity request = new HttpEntity(sceneMessage, headers);
            ResponseEntity<SceneCreatedMessage> response = restTemplate.exchange(
                    info.getHomePageUrl() + "api/create/scene", HttpMethod.POST, request, SceneCreatedMessage.class);
            SceneCreatedMessage message = response.getBody();
            if (message.getError()!= null && message.getError().equals("WRONG_SCENE_NAME")) return message;
            if (message.getError()!= null && message.getError().equals("USER_ALREADY_ON_A_SCENE")) return message;
            if (instanceInfo.size() == 1) return message;
            if (message.isResult()) return message;
            instanceInfo.remove(info);
        }
        return null;
    }
}
