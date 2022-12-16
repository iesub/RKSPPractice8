package witcher.rpg.project.game.object;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameObjectSaverService {
    private final ObjectSaverRepository objectSaverRepository;

    public void saveGameObject(ObjectSaver object){
        objectSaverRepository.save(object);
    }
}
