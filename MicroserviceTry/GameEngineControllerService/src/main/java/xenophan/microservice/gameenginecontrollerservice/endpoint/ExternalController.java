package xenophan.microservice.gameenginecontrollerservice.endpoint;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import xenophan.microservice.gameenginecontrollerservice.model.*;
import xenophan.microservice.gameenginecontrollerservice.service.GameServiceMessageProducerService;
import xenophan.microservice.gameenginecontrollerservice.service.ServiceCreationService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Controller
@Getter
@Setter
public class ExternalController {
    @Autowired
    private GameServiceMessageProducerService gameServiceMessageProducerService;
    @Value("${controlled.app.name}")
    String appName;
    @Autowired
    ServiceCreationService service;


    @Autowired
    private EurekaClient eurekaClient;
    @PostConstruct
    public void test(){
        System.out.println("***APPLICATION NAME " + appName + "***");
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @PostMapping("/api/closed/scene/create")
    @ResponseBody
    public synchronized SceneCreatedMessage sceneCreationHandler(@RequestBody CreateSceneMessage sceneMessage, HttpServletRequest request) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return gameServiceMessageProducerService.sendToQueue(sceneMessage, user, appName, request.getHeader("Authorization"));
    }

    @PostMapping("/api/internal/service/create")
    @ResponseBody
    public ServiceCreatedMessage serviceCreationHandler(@RequestBody CreateServiceMessage message){
        return service.createService(message.getPodName());
    }
}
