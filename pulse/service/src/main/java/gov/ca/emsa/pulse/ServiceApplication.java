package gov.ca.emsa.pulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@PropertySource("classpath:/application.properties")
@SpringBootApplication
@ComponentScan(basePackages = {
        "gov.ca.emsa.pulse.**", "gov.ca.emsa.pulse.service.**", "gov.ca.emsa.pulse.config.**",
        "gov.ca.emsa.pulse.health.**"
})

public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api-docs").allowedOrigins("http://localhost:3000");
            }
        };
    }

}
