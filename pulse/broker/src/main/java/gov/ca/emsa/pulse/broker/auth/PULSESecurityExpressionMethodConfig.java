package gov.ca.emsa.pulse.broker.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class PULSESecurityExpressionMethodConfig {
    @Autowired
    private PULSESecurityExpresionHandler customMethodSecurityExpressionHandler;

    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return customMethodSecurityExpressionHandler;
    }
}
