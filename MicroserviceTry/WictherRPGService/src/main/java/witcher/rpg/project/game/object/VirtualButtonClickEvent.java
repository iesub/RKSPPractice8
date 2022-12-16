package witcher.rpg.project.game.object;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirtualButtonClickEvent {
    VirtualButton button;

    public VirtualButtonClickEvent(VirtualButton button){
        this.button = button;
    }

    public void runClickEvent(){
        button.execute();
    }
}
