package xenophan.microservice.authservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import xenophan.microservice.authservice.entity.TokenEntity;
import xenophan.microservice.authservice.model.JwtAuthenticationModel;
import xenophan.microservice.authservice.model.ValidationResponse;
import xenophan.microservice.authservice.service.RedisTokenService;
import xenophan.microservice.authservice.util.Utilities;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@RequiredArgsConstructor
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private ObjectMapper mapper=new ObjectMapper();

    private final RedisTokenService tokensRedisService;

    @Value("${jwt.token.secret}")
    private String secret;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            JwtAuthenticationModel authModel = mapper.readValue(request.getInputStream(), JwtAuthenticationModel.class);
            Authentication authentication = new UsernamePasswordAuthenticationToken(authModel.getMail(), authModel.getPassword());
            return authenticationManager.authenticate(authentication);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException{

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("my8329characterAultraCsecuredandRultraKlongLsecret"));

        String token = Jwts.builder()
                .setSubject(authResult.getName())
                .claim("authorities", authResult.getAuthorities())
                .claim("principal", authResult.getPrincipal())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC)))
                .signWith(key)
                .compact();

        TokenEntity tokensEntity = TokenEntity.builder().id(Utilities.generateUuid()).authenticationToken(token)
                .mail(authResult.getName())
                .createdBy("SYSTEM").createdOn(LocalDateTime.now())
                .modifiedBy("SYSTEM").modifiedOn(LocalDateTime.now())
                .build();
        tokensEntity = tokensRedisService.save(tokensEntity);
        response.addHeader("BearerId", String.format(tokensEntity.getId()));
        response.addHeader("Expiration", String.valueOf(30*24*60*60));
        response.addHeader("Access-Control-Expose-Headers", "BearerId, Expiration");

        ValidationResponse respModel = ValidationResponse.builder().status(HttpStatus.OK.name()).token(String.format("Bearer %s", tokensEntity.getId())).methodType(HttpMethod.GET.name()).isAuthenticated(true).build();
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(mapper.writeValueAsBytes(respModel));
    }
}
