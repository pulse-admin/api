package gov.ca.emsa.pulse.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.ca.emsa.pulse.auth.permission.GrantedPermission;
import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.auth.user.User;

public class UserUtil {

    public static JWTAuthenticatedUser getJWTUser() {
        return (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
    }

    public static CommonUser getCurrentUser() {
        CommonUser user = new CommonUser();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof User) {
            User jwtAuth = (User) auth;
            user.setuser_id(jwtAuth.getuser_id());
            user.setSubjectName(jwtAuth.getSubjectName());
            user.setusername(jwtAuth.getUsername());
            user.setEmail(jwtAuth.getEmail());
            user.setauth_source("username/password");
            user.setfull_name(jwtAuth.getfull_name());
            user.setorganization("pulse");
            user.setpurpose_for_use("treatment");
            user.setrole("provider");
            user.setFirstName(jwtAuth.getFirstName());
            user.setLastName(jwtAuth.getLastName());
            user.setAcf(jwtAuth.getAcf());
            for (GrantedPermission p : jwtAuth.getAuthorities()) {
                user.addPermission(p);
            }
        }

        if (auth instanceof JWTAuthenticatedUser) {
            JWTAuthenticatedUser jwtAuth = (JWTAuthenticatedUser) auth;
            user.setLiferayAcfId(jwtAuth.getLiferayAcfId());
            user.setLiferayStateId(jwtAuth.getLiferayStateId());
        }

        return user;
    }
}
