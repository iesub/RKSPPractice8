package witcher.rpg.project.game.object;

public class ComponentAlreadyOnAnObjectException extends Exception{
    public ComponentAlreadyOnAnObjectException(String errorMessage){
        super(errorMessage);
    }
}
