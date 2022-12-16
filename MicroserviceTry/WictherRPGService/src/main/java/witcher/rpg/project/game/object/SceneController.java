package witcher.rpg.project.game.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.discovery.EurekaClient;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import witcher.rpg.project.UtilMethods;
import witcher.rpg.project.model.CreateSceneMessage;
import witcher.rpg.project.model.SceneCreatedResponse;
import witcher.rpg.project.model.User;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Controller
public class SceneController {
    HashMap<String, Scene> activeScenes = new HashMap<>();
    HashMap<String, SceneThread> sceneThreads = new HashMap<>();
    HashMap<String, SceneCreator> sceneTypes = new HashMap<>();
    HashMap<String, String> usersOnAScenes = new HashMap<>();

    private Gauge sessionGauge;
    private Gauge userCountGauge;
    @Autowired
    GameObjectSaverService gameObjectSaverService;
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    @Value("${user.destination.prefix}")
    private String userDestination;

    @Value("${client.simple.broker}")
    private String clientBroker;

    @Value("${spring.websocket.connection.name}")
    private String wsDestination;

    @Value("${max.scenes.allowed}")
    private Integer maxScenes;

    @Value("${server.nodeIp}")
    private String nodeIp;

    @Value("${server.podName}")
    private String podName;

    private int podPort;

    @Value("${spring.cloud.client.hostname}:${spring.application.name}:${spring.application.instance_id:${random.value}}")
    String value;
    @Autowired
    private ServletWebServerApplicationContext webServerAppContext;
    @Autowired
    EurekaClient eurekaClient;

    public SceneController(MeterRegistry registry){
        sessionGauge = Gauge.builder("session.count", activeScenes, HashMap::size)
                .register(registry);
        userCountGauge = Gauge.builder("user.count",  usersOnAScenes, HashMap::size)
                .register(registry);
    }


    @SneakyThrows
    @PostConstruct
    public void initialize(){
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SceneInitializer.class));
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("witcher.rpg.project.scene")){
            Object annotatedClass = Class.forName(beanDefinition.getBeanClassName()).getConstructor().newInstance();
            if (annotatedClass instanceof SceneCreator){
                addNewSceneType(annotatedClass.getClass().getSimpleName(), (SceneCreator) annotatedClass);
                System.out.println("Added to scene list: " + annotatedClass.getClass().getSimpleName());
            }
        }

        podPort = UtilMethods.createService(podName, eurekaClient, 0);
    }

    @Timed(value = "scene.creation.time", description = "Time taken to create a scene")
    @PostMapping("/api/create/scene")
    @ResponseBody
    public SceneCreatedResponse createScene(@RequestBody CreateSceneMessage createSceneMessage){
        User user = createSceneMessage.getUser();
        String sceneType = createSceneMessage.getSceneName();
        if(usersOnAScenes.containsKey(user.getMail())){
            return SceneCreatedResponse.builder()
                    .result(false)
                    .sceneHash(usersOnAScenes.get(user.getMail()))
                    .userObjectName(Objects.requireNonNull(activeScenes.get(usersOnAScenes.get(user.getMail())).getObjects()
                            .stream()
                            .filter(v -> v.getUser().getId().equals(user.getId()))
                            .findFirst().orElse(null)).getName()
                    )
                    .error("USER_ALREADY_ON_A_SCENE")
                    .build();
        }
        if (activeScenes.size() + 1 > maxScenes){
            return SceneCreatedResponse.builder()
                    .result(false)
                    .error("MAX_SCENES_ON_A_SERVER")
                    .build();
        }
        if (!sceneTypes.containsKey(sceneType)){
            return SceneCreatedResponse.builder()
                    .result(false)
                    .error("WRONG_SCENE_NAME")
                    .build();
        } else {
            SceneCreator sceneCreator = sceneTypes.get(sceneType);
            Scene newScene = sceneCreator.initScene();
            SceneThread newThread = new SceneThread();
            String sceneHash = DigestUtils.sha256Hex(newScene.toString());

            newThread.setScene(newScene);
            newScene.setSceneHash(sceneHash);
            newScene.setController(this);
            newScene.setSceneCreator(sceneCreator);
            newScene.addUser(user);
            UUID playerObjectID = UUID.randomUUID();
            String name = playerObjectID.toString();
            GameObject playerObject = newScene.getSceneCreator().initPlayerObject(name);
            playerObject.setUser(user);
            newScene.addObject(playerObject);

            SceneUserConnectionChecker sceneUserConnectionChecker = new SceneUserConnectionChecker();
            sceneUserConnectionChecker.setSceneController(this);
            sceneUserConnectionChecker.setScene(newScene);
            sceneUserConnectionChecker.getUserLastEntry().put(user.getMail(), LocalDateTime.now());
            newScene.setSceneUserConnectionChecker(sceneUserConnectionChecker);
            Thread sceneConnectionCheckerThread = new Thread(sceneUserConnectionChecker);

            usersOnAScenes.put(user.getMail(), sceneHash);
            sceneThreads.put(sceneHash, newThread);
            activeScenes.put(sceneHash, newScene);

            System.out.println("Active scenes: " + sceneThreads.size());
            newThread.start();
            sceneConnectionCheckerThread.start();
            return SceneCreatedResponse.builder()
                    .sceneHash(sceneHash)
                    .result(true)
                    .serverUrl("http://" + nodeIp + ":" + podPort)
                    .userObjectName(name)
                    .build();
        }
    }

    public void sendSceneInfo(Scene scene){
        simpMessagingTemplate.convertAndSend(clientBroker + "/scene/" + scene.getSceneHash()
                + "/sceneUpdate", scene);
    }

    @PostMapping("/api/delete/scene/{sceneHash}")
    public String deleteScene(@PathVariable String sceneHash, Model model){
        if (!activeScenes.containsKey(sceneHash)){
            model.addAttribute("response", "ERROR_INCORRECT_SCENE_HASH");
        } else {
            sceneThreads.get(sceneHash).stopTimer();
            sceneThreads.remove(sceneHash);
            activeScenes.remove(sceneHash);
            model.addAttribute("response", "SUCCESS");

            System.out.println("Active scenes: " + sceneThreads.size());
        }
        return "jsonTemplate";
    }

    @PostMapping("/api/connectUser/scene/{sceneHash}")
    public String userConnect(@PathVariable String sceneHash, HttpSession httpSession, Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Scene activeScene = activeScenes.get(sceneHash);

        if (activeScene.allowedUserAmount < activeScene.userAmount + 1){
            model.addAttribute("response", "ERROR_LOBBY_IS_FULL");
        } else {
            activeScene.addUser(user);
            UUID playerObjectID = UUID.randomUUID();
            String name = playerObjectID.toString();
            GameObject playerObject = activeScene.getSceneCreator().initPlayerObject(name);
            playerObject.setUser(user);
            activeScene.addObject(playerObject);

            model.addAttribute("response", "SUCCESS");
            model.addAttribute("playerObjectName", name);
        }
        return "jsonTemplate";
    }

    @PostMapping("/api/disconnectUser/scene/{sceneHash}")
    public String userDisconnect(@PathVariable String sceneHash, Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        activeScenes.get(sceneHash).deleteUser(user);
        deleteUserFromScene(user.getMail());

        if (activeScenes.get(sceneHash).getConnectedUsers().size() == 0){
            sceneThreads.get(sceneHash).stopTimer();
            sceneThreads.remove(sceneHash);
            activeScenes.remove(sceneHash);

            System.out.println("Active scenes: " + sceneThreads.size());
        }

        model.addAttribute("response", "SUCCESS");
        return "jsonTemplate";
    }

    @PostMapping("/api/deleteGameObject/scene/{sceneHash}/{objectName}")
    public String deleteObject(@PathVariable String sceneHash, @PathVariable String objectName, Model model){
        activeScenes.get(sceneHash).deleteObject(activeScenes.get(sceneHash).findObject(objectName));
        model.addAttribute("response", "SUCCESS");
        return "jsonTemplate";
    }

    @PostMapping("/api/saveUserObject/scene/{sceneHash}/{objectName}")
    public String saveUserObject(@PathVariable String sceneHash, @PathVariable String objectName, Model model
                                 ) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        GameObject gameObject = activeScenes.get(sceneHash).findObject(objectName);
        saveObject(gameObject, user.getId());
        model.addAttribute("response", "SUCCESS");
        return "jsonTemplate";
    }

    @GetMapping("/api/getList/scene/{sceneType}")
    public String getListOfActiveScenesByType(@PathVariable String sceneType, Model model){

        HashMap<String, Scene> responseMap = new HashMap<>();
        for (String key : activeScenes.keySet()){
            if (activeScenes.get(key).getSceneName().equals(sceneType)){
                responseMap.put(key, activeScenes.get(key));
            }
        }
        model.addAttribute("sceneMap", responseMap);
        return "jsonTemplate";
    }

    @MessageMapping("/scene/{sceneHash}/{buttonName}.pushButton")
    public void callFunction(@DestinationVariable String sceneHash, @DestinationVariable String buttonName,
                             Principal principal){

        Scene scene = activeScenes.get(sceneHash);
        VirtualButton object = scene.getVirtualButton(buttonName);

        try {
            scene.addUIButtonClickEvent(new VirtualButtonClickEvent(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/scene/{sceneHash}/{inputName}.inputData")
    public void setVariable(@DestinationVariable String sceneHash, @DestinationVariable String inputName,
                            InputObject input){

        Scene scene = activeScenes.get(sceneHash);
        VirtualInput object = scene.getVirtualInput(inputName);
        object.value = input.getInput();

        try {
            scene.addInputEvent(new VirtualInputEvent(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/scene/{sceneHash}.heartBeat")
    public void processHeartBeat(@DestinationVariable String sceneHash, StompHeaderAccessor stompHeaderAccessor){
        Scene scene = activeScenes.get(sceneHash);
        UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken)stompHeaderAccessor.getHeader("simpUser");
        SceneUserConnectionChecker sceneUserConnectionChecker = scene.getSceneUserConnectionChecker();
        sceneUserConnectionChecker.updateUserLastEntry((User) user.getPrincipal());
    }

    @GetMapping("/api/open/getIp")
    @ResponseBody
    public String getIp(){
        return value;
    }

    public void addNewSceneType(String sceneName, SceneCreator sceneInitiationCommand){
        sceneTypes.put(sceneName, sceneInitiationCommand);
    }

    public void saveObject(GameObject object, Long userId) throws JsonProcessingException {
        gameObjectSaverService.saveGameObject(new ObjectSaver(userId, object));
    }

    public synchronized void deleteUserFromScene(String mail){
        usersOnAScenes.remove(mail);
    }

    public void deleteScene(String sceneHash){
        sceneThreads.get(sceneHash).stopTimer();
        sceneThreads.remove(sceneHash);
        activeScenes.remove(sceneHash);
        System.out.println("Active scenes: " + sceneThreads.size());
    }
}
