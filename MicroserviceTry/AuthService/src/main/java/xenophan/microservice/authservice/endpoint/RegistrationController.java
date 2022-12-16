package xenophan.microservice.authservice.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xenophan.microservice.authservice.dto.front.RegistrationResponseDTO;
import xenophan.microservice.authservice.dto.front.UserDTOFront;
import xenophan.microservice.authservice.entity.User;
import xenophan.microservice.authservice.service.UserService;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class RegistrationController {

    @Autowired
    UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<RegistrationResponseDTO> registerUser(@RequestBody UserDTOFront userDTOFront){
        String emailPattern =
                "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(userDTOFront.getMail());
        RegistrationResponseDTO data = new RegistrationResponseDTO();

        if (Objects.equals(userDTOFront.getMail(), "")){
            data.setMailEmpty(true);
            data.setGotError(true);
        } else {
            if(!matcher.matches()){
                data.setMailCorrect(false);
                data.setGotError(true);
            }
        }
        if (!userDTOFront.getPassword().equals(userDTOFront.getPasswordConfirm())) {
            data.setPasswordsCorrect(false);
            data.setGotError(true);
        }
        if (userDTOFront.getUsername() == ""){
            data.setNicknameEmpty(true);
            data.setGotError(true);
        }
        if (userDTOFront.getPassword() == ""){
            data.setPasswordEmpty(true);
            data.setGotError(true);
        }
        if (data.isGotError()){
            return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
        }
        if (userService.saveUser(User.builder()
                        .mail(userDTOFront.getMail())
                        .password(userDTOFront.getPassword())
                        .username(userDTOFront.getUsername())
                        .build())){
            data.setMailExist(false);
            return new ResponseEntity<>(data, HttpStatus.OK);
        } else {
            data.setMailExist(true);
            return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
        }
    }
}
