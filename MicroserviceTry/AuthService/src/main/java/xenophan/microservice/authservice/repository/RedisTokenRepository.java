package xenophan.microservice.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import xenophan.microservice.authservice.entity.TokenEntity;

public interface RedisTokenRepository extends CrudRepository<TokenEntity, String> {
}
