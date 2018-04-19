package gov.ca.emsa.pulse.auth.authentication;

import gov.ca.emsa.pulse.auth.jwt.JWTCreationException;

public interface Authenticator {

    public String refreshJWT(String jwt) throws JWTCreationException;

}
