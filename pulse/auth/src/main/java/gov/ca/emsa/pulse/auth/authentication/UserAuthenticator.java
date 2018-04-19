package gov.ca.emsa.pulse.auth.authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.ca.emsa.pulse.auth.jwt.JWTAuthor;
import gov.ca.emsa.pulse.auth.jwt.JWTConsumer;
import gov.ca.emsa.pulse.auth.jwt.JWTCreationException;

@Service
public class UserAuthenticator implements Authenticator {
    private static final Logger logger = LogManager.getLogger(UserAuthenticator.class);

    @Autowired
    private JWTAuthor jwtAuthor;

    @Autowired
    private JWTConsumer jwtConsumer;

    @Override
    public String refreshJWT(String oldJwt) throws JWTCreationException {
        String jwt = null;

        // Parse old Jwt
        Map<String, Object> claims = jwtConsumer.consume(oldJwt);
        List<String> authorityInfo = (List<String>) claims.get(JWTUserConverter.AUTHORITIES);
        List<String> identityInfo = (List<String>) claims.get(JWTUserConverter.IDENTITY);
        Object orgs = claims.get(JWTUserConverter.ORGANIZATIONS);
        Map<String, Object> jwtClaims = new HashMap<String, Object>();
        jwtClaims.put("Authorities", authorityInfo);
        jwtClaims.put("Identity", identityInfo);
        jwtClaims.put("Identity", orgs);

        // Create new jwt
        jwt = jwtAuthor.createJWT(identityInfo.get(2), jwtClaims);

        return jwt;
    }

    public JWTAuthor getJwtAuthor() {
        return jwtAuthor;
    }

    public void setJwtAuthor(JWTAuthor jwtAuthor) {
        this.jwtAuthor = jwtAuthor;
    }

}
