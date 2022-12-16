package witcher.rpg.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SceneCreatedResponse {
    private String serverUrl;
    private String sceneHash;
    private boolean result;
    private String randomHash;
    private String userObjectName;
    private String error;
}
