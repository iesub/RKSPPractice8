package xenophan.microservice.authservice.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import xenophan.microservice.authservice.filter.JWTAuthenticationFilter;
import xenophan.microservice.authservice.filter.JWTVerifierFilter;
import xenophan.microservice.authservice.repository.RedisTokenRepository;
import xenophan.microservice.authservice.service.CustomUserDetailService;
import xenophan.microservice.authservice.service.LogoutService;
import xenophan.microservice.authservice.service.RedisTokenService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${api.gateway.ip}")
    private String apiGatewayIp;
    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private RedisTokenService redisTokenService;

    @Autowired
    LogoutService logoutService;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), redisTokenService))
                .addFilterAfter(new JWTVerifierFilter(redisTokenService), JWTAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/registration").permitAll()
                .anyRequest()
                .authenticated()
                .and().httpBasic()
                .and().logout().logoutUrl("/logout")
                .addLogoutHandler(logoutService)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(encoder);
        authenticationProvider.setUserDetailsService(customUserDetailService);

        return authenticationProvider;
    }
}
