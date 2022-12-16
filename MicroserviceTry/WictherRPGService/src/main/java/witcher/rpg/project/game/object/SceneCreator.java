package witcher.rpg.project.game.object;

public interface SceneCreator {
    Scene initScene();
    GameObject initPlayerObject(String playerObjectName);
}
