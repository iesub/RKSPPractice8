package xenophan.microservice.gameenginecontrollerservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import xenophan.microservice.gameenginecontrollerservice.model.User;
import xenophan.microservice.gameenginecontrollerservice.utils.Utilities;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JWTVerifierFilter extends OncePerRequestFilter {
    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if(!Utilities.validString(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        User user = getUserObjectFromJwt(httpServletRequest.getHeader("auth-token"));
        String authoritiesStr = httpServletRequest.getHeader("authorities");
        Set<SimpleGrantedAuthority> simpleGrantedAuthorities = new HashSet<>();
        if(Utilities.validString(authoritiesStr)) {
            simpleGrantedAuthorities=Arrays.stream(authoritiesStr.split(",")).distinct()
                    .filter(Utilities::validString).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());;
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, simpleGrantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }

    private static User getUserObjectFromJwt(String jwt) throws Exception {

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("my8329characterAultraCsecuredandRultraKlongLsecret"));
        Jws<Claims> authClaim = Jwts.parser().setSigningKey(key)
                .parseClaimsJws(jwt);

        HashMap<String, Object> userData = ((HashMap<String, HashMap>) authClaim.getBody().get("principal")).get("userObject");
        return User.builder()
                .id(Long.valueOf(String.valueOf(userData.get("id"))))
                .mail((String) userData.get("mail"))
                .username((String) userData.get("username"))
                .build();
    }
}
