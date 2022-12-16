package xenophan.microservice.authservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import xenophan.microservice.authservice.entity.User;
import xenophan.microservice.authservice.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class ApplicationStartUp {
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Bean
    public CommandLineRunner adminUserInit(){
        return (args) -> {
            List<User> users = userRepository.findAll();
            if (users.size() == 0){
                this.userRepository.save(User.builder().
                        username("Admin").
                        mail("admin@mail.com").
                        password(encoder.encode("AdminPass")).
                        roles(Arrays.asList("USER", "ADMIN")).
                        status("Active").
                        build());
            }
        };
    }
}