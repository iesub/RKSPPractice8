package xenophan.microservice.authservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtAuthenticationModel {
    private String mail;
    private String password;
}
