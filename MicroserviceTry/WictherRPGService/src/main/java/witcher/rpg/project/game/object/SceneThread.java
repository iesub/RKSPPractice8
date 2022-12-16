package witcher.rpg.project.game.object;

import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;

@Getter
@Setter
public class SceneThread extends Thread{

    Scene scene;
    Timer timer = new Timer();

    @Override
    public void run() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scene.tick();
            }
        }, 0, scene.getSceneTickInterval());
    }

    public void stopTimer(){
        timer.cancel();
    }
}
