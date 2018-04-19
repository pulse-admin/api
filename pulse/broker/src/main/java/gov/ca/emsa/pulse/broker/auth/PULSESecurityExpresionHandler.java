package gov.ca.emsa.pulse.broker.auth;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;

@Component
public class PULSESecurityExpresionHandler extends DefaultMethodSecurityExpressionHandler {
    @Autowired
    @Lazy
    private AlternateCareFacilityManager acfManager;
    @Autowired
    @Lazy
    private PatientManager patientManager;

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
            MethodInvocation invocation) {
        PULSEBrokerSecurityExpressionRoot root = new PULSEBrokerSecurityExpressionRoot(authentication, acfManager,
                patientManager);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(new AuthenticationTrustResolverImpl());
        root.setRoleHierarchy(getRoleHierarchy());
        return root;
    }
}
