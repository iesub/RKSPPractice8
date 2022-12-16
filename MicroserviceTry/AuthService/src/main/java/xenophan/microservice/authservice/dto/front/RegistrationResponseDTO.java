package xenophan.microservice.authservice.dto.front;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDTO {
    private boolean gotError = false;
    private boolean passwordsCorrect = true;
    private boolean mailCorrect = true;
    private boolean mailExist = false;
    private boolean mailEmpty = false;
    private boolean passwordEmpty = false;
    private boolean nicknameEmpty = false;
}
