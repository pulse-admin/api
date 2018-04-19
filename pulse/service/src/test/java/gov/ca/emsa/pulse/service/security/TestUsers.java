package gov.ca.emsa.pulse.service.security;

import org.springframework.security.core.context.SecurityContextHolder;

import gov.ca.emsa.pulse.auth.permission.GrantedPermission;
import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;

public class TestUsers {
    public static void setCurrentUser(String permission, Long liferayStateId, Long liferayAcfId) {
        JWTAuthenticatedUser jwt = new JWTAuthenticatedUser();
        jwt.setId(1L);
        jwt.setAuthenticated(true);
        jwt.addPermission(new GrantedPermission(permission));
        jwt.setLiferayStateId(liferayStateId);
        jwt.setLiferayAcfId(liferayAcfId);
        SecurityContextHolder.getContext().setAuthentication(jwt);
    }

    public static JWTAuthenticatedUser getCurrentUser() {
        return (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
    }
}
