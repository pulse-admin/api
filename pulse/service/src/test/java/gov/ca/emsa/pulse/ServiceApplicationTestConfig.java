package gov.ca.emsa.pulse;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@PropertySource("classpath:/application-test.properties")
@SpringBootApplication(scanBasePackages = {
        "gov.ca.emsa.pulse.service", "gov.ca.emsa.pulse.common.soap"
})
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class ServiceApplicationTestConfig {
}
