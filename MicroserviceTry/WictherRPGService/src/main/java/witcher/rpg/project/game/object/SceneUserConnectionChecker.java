package witcher.rpg.project.game.object;

import lombok.Data;
import lombok.SneakyThrows;
import witcher.rpg.project.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class SceneUserConnectionChecker implements Runnable {

    Scene scene;
    Map<String, LocalDateTime> userLastEntry = new TreeMap<>();
    SceneController sceneController;

    @SneakyThrows
    @Override
    public void run() {
        while(true){
        Thread.sleep(30 * 1000);
        synchronized (this){
            List<String> usersToDelete = new ArrayList<>();
            for (String key : userLastEntry.keySet()){
                if (LocalDateTime.now().isAfter(userLastEntry.get(key).plusMinutes(1L))){
                    usersToDelete.add(key);
                }
            }
            for (String key : usersToDelete){
                System.out.println("*** " + LocalDateTime.now() + " - User with mail " + key + " deleted because of expired heartbeat ***");
                userLastEntry.remove(key);
                User user = scene.connectedUsers.stream().filter(v -> v.getMail().equals(key)).findFirst().orElse(null);
                scene.deleteUser(user);
                sceneController.deleteUserFromScene(key);
            }
            if (scene.connectedUsers.size() == 0){
                sceneController.deleteScene(scene.getSceneHash());
                break;
            }
        }
        }
    }

    public synchronized void updateUserLastEntry(User user){
        userLastEntry.put(user.getMail(), LocalDateTime.now());
        int a = 1;
    }
}
