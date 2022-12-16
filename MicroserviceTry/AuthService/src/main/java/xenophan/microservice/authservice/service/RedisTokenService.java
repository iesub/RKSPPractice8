package xenophan.microservice.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xenophan.microservice.authservice.entity.TokenEntity;
import xenophan.microservice.authservice.repository.RedisTokenRepository;

import java.util.Optional;

@Service
public class RedisTokenService {
    @Autowired
    private RedisTokenRepository tokensRedisRepository;

    public TokenEntity save(TokenEntity entity) {
        return tokensRedisRepository.save(entity);
    }


    public Optional<TokenEntity> findById(String id) {
        return tokensRedisRepository.findById(id);
    }

    public Iterable<TokenEntity> findAll() {
        return tokensRedisRepository.findAll();
    }

    public void deleteToken(String id){
        tokensRedisRepository.deleteById(id);
    }
}
