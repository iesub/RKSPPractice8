package xenophan.microservice.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class LogoutService implements LogoutHandler {

    @Autowired
    RedisTokenService redisTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String bearerToken = request.getHeader("Authorization");
        String authToken = bearerToken.replace("Bearer ", "");
        redisTokenService.deleteToken(authToken);
    }
}
