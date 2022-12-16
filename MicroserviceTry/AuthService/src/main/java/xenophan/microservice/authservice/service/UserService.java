package xenophan.microservice.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import xenophan.microservice.authservice.entity.User;
import xenophan.microservice.authservice.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<User> getUsersList() {
        List<User> userLst = new ArrayList<User>();
        userRepository.findAll().forEach(user -> userLst.add(user));
        return userLst;
    }

    public User findUserByMail(String mail){
        return userRepository.findUserByMail(mail);
    }

    public boolean saveUser(User user){
        if (userRepository.findUserByMail(user.getMail()) != null){
            return false;
        }
        List<String> userRoles = new ArrayList<>();
        userRoles.add("USER");
        user.setRoles(userRoles);
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }
}
