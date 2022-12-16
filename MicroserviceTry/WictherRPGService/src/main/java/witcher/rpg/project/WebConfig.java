package witcher.rpg.project;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**").allowedHeaders("content-type", "authorization")
                .allowedMethods("GET", "POST", "DELETE", "PUT").allowedOrigins("*");
    }
}
