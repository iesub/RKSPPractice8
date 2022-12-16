package witcher.rpg.project.game.object;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Configurable;
import witcher.rpg.project.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configurable
public class Scene{
    String sceneName;
    List<GameObject> objects = new ArrayList<>();
    @JsonBackReference
    List<VirtualButton> virtualButtons = new ArrayList<>();
    @JsonBackReference
    List<VirtualInput> virtualInputs = new ArrayList<>();
    @JsonBackReference
    List<VirtualButtonClickEvent> virtualButtonClickEvents = new ArrayList<>();
    @JsonBackReference
    List<VirtualInputEvent> virtualInputEvents = new ArrayList<>();
    @JsonBackReference
    List<User> connectedUsers = new ArrayList<>();
    @JsonBackReference
    SceneUserConnectionChecker sceneUserConnectionChecker;
    int userAmount = 0;
    int allowedUserAmount = 1;
    @JsonBackReference
    SceneCreator sceneCreator;
    @JsonBackReference
    String sceneHash;
    @JsonBackReference
    int sceneTickInterval = 1000;

    @JsonBackReference
    SceneController controller;

    public Scene(String name){
        this.sceneName = name;
    }

    public void addObject(GameObject object){

        for (GameObject obj : objects){
            if (obj.getName().equals(object.getName())){
                try {
                    throw new ComponentAlreadyOnAnObjectException("Object " + obj.getClass().getSimpleName() +
                            " already attached to a scene!");
                } catch (ComponentAlreadyOnAnObjectException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        object.setScene(this);
        objects.add(object);
    }

    public void addUIButtonClickEvent(VirtualButtonClickEvent virtualButtonClickEvent){
        virtualButtonClickEvents.add(virtualButtonClickEvent);
    }

    public void addInputEvent(VirtualInputEvent virtualInputEvent){
        virtualInputEvents.add(virtualInputEvent);
    }

    public void addVirtualButton(VirtualButton virtualButton){
        virtualButtons.add(virtualButton);
    }

    public VirtualButton getVirtualButton(String buttonName){
        return virtualButtons.stream().filter(v -> v.getButtonName().equals(buttonName)).findFirst().orElse(null);
    }

    public void addVirtualInput(VirtualInput virtualInput){
        virtualInputs.add(virtualInput);
    }

    public VirtualInput getVirtualInput(String inputName){
        return virtualInputs.stream().filter(v -> v.getInputName().equals(inputName)).findFirst().orElse(null);
    }

    public void addUser(User user){
        if (allowedUserAmount >= userAmount + 1){
            userAmount++;
            connectedUsers.add(user);
        }
    }

    public synchronized void deleteUser(User user){
        connectedUsers.removeIf(cUser -> cUser.getId().equals(user.getId()));
        if (connectedUsers.size() < userAmount){
            userAmount--;
        }
    }

    public void deleteObject(GameObject object){
        objects.remove(object);
    }

    public GameObject findObject(String name){
        for (GameObject object : objects) {
            if (name.equals(object.getName())) {
                return object;
            }
        }
        return null;
    }

    void tick(){

        for (GameObject object : objects) {
            object.componentsStart();
        }

        handelUIButtonClickEvents();
        handelInputEvents();

        for (GameObject object : objects) {
            object.componentsUpdate();
        }

        controller.sendSceneInfo(this);
    }

    void handelUIButtonClickEvents(){
        List<VirtualButtonClickEvent> events = new ArrayList<>();
        for (VirtualButtonClickEvent event : virtualButtonClickEvents){


            event.runClickEvent();
            events.add(event);
        }
        for (VirtualButtonClickEvent event : events){
            virtualButtonClickEvents.remove(event);
        }
    }

    void handelInputEvents(){
        List<VirtualInputEvent> events = new ArrayList<>();
        for (VirtualInputEvent event : virtualInputEvents){


            event.runClickEvent();
            events.add(event);
        }
        for (VirtualInputEvent event : events){
            virtualInputEvents.remove(event);
        }
    }

}
