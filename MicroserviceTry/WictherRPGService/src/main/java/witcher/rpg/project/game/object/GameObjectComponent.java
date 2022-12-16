package witcher.rpg.project.game.object;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
public class GameObjectComponent {
    String name;
    @JsonBackReference
    GameObject gameObject;
    @Transient
    @JsonBackReference
    boolean startRan = false;

    public void Start(){

    }

    public void Update(){

    }

    public void Destroy(Object object){
        if (object instanceof GameObject){
            ((GameObject) object).Destroy();
        }
        else if (object instanceof GameObjectComponent){
            ((GameObjectComponent) object).OnDestroy();
            ((GameObjectComponent) object).getGameObject().destroyComponent((GameObjectComponent) object);
        }
        else {

        }
    }

    public void OnDestroy(){

    }
}
