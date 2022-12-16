package witcher.rpg.project.filter;

import com.netflix.discovery.EurekaClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import witcher.rpg.project.UtilMethods;
import witcher.rpg.project.utils.Utilities;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@Component
@Setter
@Builder
@AllArgsConstructor
public class JWTVerifierFilter extends OncePerRequestFilter {

    List<String> excludedUrls = new ArrayList<>();
    List<String> internalUrls = new ArrayList<>();
    @LoadBalanced
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private EurekaClient eurekaClient;


    public JWTVerifierFilter(){
        excludedUrls.add("/login");
        excludedUrls.add("/registration");

        internalUrls.add("/api/internal");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if(!Utilities.validString(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        Authentication authentication = UtilMethods.getUserAuth(restTemplate, eurekaClient, authHeader);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    public Predicate<ServerHttpRequest> isSecured = request -> excludedUrls.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
    public Predicate<ServerHttpRequest> internalAddress = request -> internalUrls.stream().anyMatch(uri -> request.getURI().getPath().contains(uri));
}
