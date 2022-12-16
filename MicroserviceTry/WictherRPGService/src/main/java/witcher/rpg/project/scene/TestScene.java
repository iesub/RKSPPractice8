package witcher.rpg.project.scene;

import witcher.rpg.project.character.component.CharacterScript;
import witcher.rpg.project.game.object.*;


@SceneInitializer
public class TestScene implements SceneCreator {

    Scene scene;

    @Override
    public Scene initScene() {
        Scene testScene = new Scene("TestScene");
        this.scene = testScene;
        testScene.setAllowedUserAmount(2);
        testScene.setSceneTickInterval(2);
        return testScene;
    }

    @Override
    public GameObject initPlayerObject(String playerObjectName) {
        GameObject testCharacter = new GameObject(playerObjectName);
        CharacterScript component = new CharacterScript();
        component.setRace(3);
        testCharacter.addComponent(component);

        try {
            scene.addVirtualButton(new VirtualButton("LifePathRollButton" + playerObjectName,component, component.getClass().getMethod("lifePathRoll")));
            scene.addVirtualInput(new VirtualInput("NameInput" + playerObjectName, component, component.getClass().getMethod("setName", String.class)));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return testCharacter;
    }
}
