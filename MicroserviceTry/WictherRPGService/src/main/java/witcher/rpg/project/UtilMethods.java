package witcher.rpg.project;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import witcher.rpg.project.model.*;
import witcher.rpg.project.utils.Utilities;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UtilMethods {

    public static Authentication getUserAuth(RestTemplate restTemplate, EurekaClient eurekaClient, String bearer){
        Random random = new Random();
        Application application = eurekaClient.getApplication("AUTH-SERVICE");
        InstanceInfo instanceInfo = application.getInstances().get(random.nextInt(application.getInstances().size()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearer);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<ValidationResponse> response = restTemplate.exchange(instanceInfo.getHomePageUrl() + "api/internal/validateToken",
                HttpMethod.GET, request, ValidationResponse.class);

        String token= Objects.requireNonNull(response.getBody()).getToken();
        User user;
        try {
            user = UtilMethods.getUserObjectFromJwt(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> authorities = response.getBody().getAuthorities().stream().map(Authorities::getAuthority).toList();
        String authoritiesStr = String.join(",", authorities);
        Set<SimpleGrantedAuthority> simpleGrantedAuthorities = new HashSet<>();
        if(Utilities.validString(authoritiesStr)) {
            simpleGrantedAuthorities=Arrays.stream(authoritiesStr.split(",")).distinct()
                    .filter(Utilities::validString).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());;
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, simpleGrantedAuthorities);
        return authentication;
    }

    @SneakyThrows
    public static int createService(String podName, EurekaClient eurekaClient, int retries){
        Random random = new Random();
        Application application = eurekaClient.getApplication("GAME-ENGINE-CONTROLLER-SERVICE");
        InstanceInfo instanceInfo = application.getInstances().get(random.nextInt(application.getInstances().size()));

        RestTemplate restTemplate = new RestTemplate();
        CreateServiceMessage createServiceMessage = CreateServiceMessage.builder().podName(podName).build();
        HttpEntity request = new HttpEntity(createServiceMessage);
        ResponseEntity<ServiceCreatedMessage> response = restTemplate.exchange(instanceInfo.getHomePageUrl() + "api/internal/service/create",
                HttpMethod.POST, request, ServiceCreatedMessage.class);
        ServiceCreatedMessage result = response.getBody();
        if (result.getPort() == 0){
            if (retries == 3) return 0;
            Thread.sleep(30 * 1000);
            UtilMethods.createService(podName, eurekaClient, retries + 1);
        }
        return result.getPort();
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
