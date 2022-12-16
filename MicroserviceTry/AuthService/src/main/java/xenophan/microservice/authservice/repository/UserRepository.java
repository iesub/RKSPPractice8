package xenophan.microservice.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xenophan.microservice.authservice.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByMail(String mail);
}