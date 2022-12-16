package xenophan.microservice.authservice.dto.front;

import lombok.Data;

@Data
public class UserDTOFront {
    String mail;
    String username;
    String password;
    String passwordConfirm;
}
