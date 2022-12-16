package witcher.rpg.project.game.object;

public class VirtualInputEvent {
    VirtualInput input;

    public VirtualInputEvent(VirtualInput input){
        this.input = input;
    }

    public void runClickEvent(){
        input.execute();
    }
}
