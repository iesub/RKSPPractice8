package xenophan.microservice.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xenophan.microservice.authservice.dto.UserDTO;
import xenophan.microservice.authservice.entity.User;
import xenophan.microservice.authservice.service.UserService;
@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        User user = userService.findUserByMail(mail);
        if (user == null){
            throw new UsernameNotFoundException("No user with such mail!");
        } else {
            return new UserDTO(user);
        }
    }
}
