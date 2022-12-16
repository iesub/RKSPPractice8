package witcher.rpg.project;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import witcher.rpg.project.filter.JWTVerifierFilter;

@Configuration
@EnableWebSecurity
@EnableJpaRepositories
@EnableTransactionManagement
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private EurekaClient eurekaClient;

    private JWTVerifierFilter jwtVerifierFilter;

    @Override
    protected void configure(HttpSecurity http)
            throws Exception {
        jwtVerifierFilter = JWTVerifierFilter.builder().eurekaClient(eurekaClient).build();
        http.csrf().disable().cors().and()
                .headers().frameOptions().disable()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtVerifierFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/open/**", "/ws/**", "/actuator/**").permitAll()
                .anyRequest()
                .authenticated()
                .and().httpBasic();

    }
}
