package witcher.rpg.project.game.object;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Data
public class VirtualButton {
    String buttonName;
    GameObjectComponent component;
    Method method;

    public VirtualButton(String name, GameObjectComponent component, Method method){
        this.buttonName = name;
        this.component = component;
        this.method = method;
    }

    public void execute(){
        try {
            method.invoke(component);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
