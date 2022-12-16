package xenophan.microservice.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xenophan.microservice.apigateway.model.Authorities;
import xenophan.microservice.apigateway.model.ValidationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
public class AuthenticationPrefilter extends AbstractGatewayFilterFactory<AuthenticationPrefilter.Config> {

    List<String> excludedUrls = new ArrayList<>();
    List<String> internalUrls = new ArrayList<>();
    private final WebClient.Builder webClientBuilder;

    public AuthenticationPrefilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder=webClientBuilder;
        excludedUrls.add("/login");
        excludedUrls.add("/registration");
        excludedUrls.add("/api/open");

        internalUrls.add("/api/internal");
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Primary
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String bearerToken = request.getHeaders().getFirst("Authorization");

            if (internalAddress.test(request)){
                HttpStatus errorCode = null;
                String errorMsg = "";
                errorCode = HttpStatus.UNAUTHORIZED;
                errorMsg = HttpStatus.UNAUTHORIZED.getReasonPhrase();
                return onError(exchange, String.valueOf(errorCode.value()) ,errorMsg, "Access error", errorCode);
            }

            if(isSecured.test(request)) {
                return webClientBuilder.build().get()
                        .uri("lb://AUTH-SERVICE/api/internal/validateToken")
                        .header("Authorization", bearerToken)
                        .retrieve().bodyToMono(ValidationResponse.class)
                        .map(response -> {
                            exchange.getRequest().mutate().header("username", response.getUsername());
                            exchange.getRequest().mutate().header("authorities", response.getAuthorities().stream().map(Authorities::getAuthority).reduce("", (a, b) -> a + "," + b));
                            exchange.getRequest().mutate().header("auth-token", response.getToken());
                            return exchange;
                        }).flatMap(chain::filter).onErrorResume(error -> {
                            HttpStatus errorCode = null;
                            String errorMsg = "";
                            if (error instanceof WebClientResponseException) {
                                WebClientResponseException webCLientException = (WebClientResponseException) error;
                                errorCode = webCLientException.getStatusCode();
                                errorMsg = webCLientException.getStatusText();

                            } else {
                                errorCode = HttpStatus.BAD_GATEWAY;
                                errorMsg = HttpStatus.BAD_GATEWAY.getReasonPhrase();
                            }
//                            AuthorizationFilter.AUTH_FAILED_CODE
                            return onError(exchange, String.valueOf(errorCode.value()) ,errorMsg, "JWT Authentication Failed", errorCode);
                        });
            }

            return chain.filter(exchange);
        };
    }

    public Predicate<ServerHttpRequest> isSecured = request -> excludedUrls.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
    public Predicate<ServerHttpRequest> internalAddress = request -> internalUrls.stream().anyMatch(uri -> request.getURI().getPath().contains(uri));
    private Mono<Void> onError(ServerWebExchange exchange, String errCode, String err, String errDetails, HttpStatus httpStatus) {
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
//        ObjectMapper objMapper = new ObjectMapper();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @NoArgsConstructor
    public static class Config{

    }
}
