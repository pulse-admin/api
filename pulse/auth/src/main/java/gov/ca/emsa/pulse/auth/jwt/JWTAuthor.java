package gov.ca.emsa.pulse.auth.jwt;

import java.util.Map;

public interface JWTAuthor {

    // public String createJWT(String subject, Map<String, List<String> >
    // claims) ;

    public String createJWT(String subject, Map<String, Object> claims);

}
