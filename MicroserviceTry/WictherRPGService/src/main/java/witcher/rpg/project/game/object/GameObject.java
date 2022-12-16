package witcher.rpg.project.game.object;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import witcher.rpg.project.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameObject {
    String name;
    GameObjectTag tag;

    @JsonBackReference
    Scene scene;
    List<GameObjectComponent> components = new ArrayList<>();
    @JsonBackReference
    User user;
    public GameObject(){}

    public GameObject (String name){
        this.name = name;
    }

    public void addComponent(GameObjectComponent component){
        for (GameObjectComponent comp : components){
            if (comp.getClass().getSimpleName().equals(component.getClass().getSimpleName())){
                try {
                    throw new ComponentAlreadyOnAnObjectException("Component " + component.getClass().getSimpleName() +
                            " already attached to an object!");
                } catch (ComponentAlreadyOnAnObjectException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        component.setGameObject(this);
        components.add(component);
        component.Start();
    }

    public void componentsStart(){
        for (GameObjectComponent component : components) {
            if (!component.isStartRan()) component.Start();
        }
    }

    public void componentsUpdate(){
        for (GameObjectComponent component : components) {
            component.Update();
        }
    }

    public <T> T getObjectComponent(String className){
        for (GameObjectComponent component : components) {
            if (className.equals(component.getClass().getSimpleName())) {
                return (T) component;
            }
        }
        return null;
    }

    public void destroyComponent(GameObjectComponent component){
        components.remove(component);
    }

    public void Destroy(){
        for (int i = 0; i < components.size(); i++){
            components.get(i).OnDestroy();
        }
        scene.deleteObject(this);
    }

    public void saveObject(){

    }
}
