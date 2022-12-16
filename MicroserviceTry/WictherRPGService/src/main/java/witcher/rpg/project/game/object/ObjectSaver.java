package witcher.rpg.project.game.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_game_object")
public class ObjectSaver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = Long.valueOf(0);
    private Long userId;
    private String dataJson;

    public ObjectSaver(Long userId, GameObject object) throws JsonProcessingException {
        this.userId = userId;
        convertObjectToJson(object);
    }

    public void convertObjectToJson(GameObject object) throws JsonProcessingException {
        ObjectWriter writer = new ObjectMapper().writer();
        dataJson = writer.writeValueAsString(object);
    }
}
