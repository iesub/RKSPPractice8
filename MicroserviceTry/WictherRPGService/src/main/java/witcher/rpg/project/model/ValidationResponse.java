package witcher.rpg.project.model;

import lombok.Data;

import java.util.List;

@Data
public class ValidationResponse {
    private String status;
    private boolean isAuthenticated;
    private String methodType;
    private String username;
    private String token;
    private List<Authorities> authorities;
}
