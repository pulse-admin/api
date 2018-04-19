package gov.ca.emsa.pulse.auth.authentication;

import gov.ca.emsa.pulse.auth.jwt.JWTValidationException;
import gov.ca.emsa.pulse.auth.user.User;

public interface JWTUserConverter {
    public static final String PULSE_PFX = "pulse-";
    public static final String PULSE_US = "pulse-us";
    public static final String ORGANIZATIONS = "Orgs";
    public static final String AUTHORITIES = "Authorities";
    public static final String IDENTITY = "Identity";

    // Check JWT and build a user given a JWT
    public User getAuthenticatedUser(String jwt) throws JWTValidationException;

}
