package gov.ca.emsa.pulse.auth.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;

import gov.ca.emsa.pulse.auth.authentication.JWTUserConverter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.ca.emsa.pulse.auth.TestConfig.class
})
public class JWTConsumerTest {

    @Autowired
    private JWTAuthor jwtAuthor;

    @Autowired
    private JWTConsumer jwtConsumer;

    @Autowired
    @Qualifier("RsaJose4JWebKey")
    JSONWebKey jwk;

    @Autowired
    JWTUserConverter jwtUserConverter;

    @Autowired
    Environment env;
    @Autowired
    JSONWebKey jsonWebKey;

    @Test
    public void consumerIsNotNull() {
        assertNotNull(jwtConsumer);
    }

    @Test
    public void testConsumer() throws Exception {

        Map<String, Object> claims = new HashMap<String, Object>();
        List<String> authorities = new ArrayList<String>();
        authorities.add("ROLE_SUPERSTAR");

        claims.put(JWTUserConverter.AUTHORITIES, authorities);
        Map<String, Long> orgs = new HashMap<String, Long>();
        orgs.put("pulse_us", 1L);
        orgs.put("pulse_va", 2L);
        orgs.put("va_acf", 3L);
        claims.put(JWTUserConverter.ORGANIZATIONS, orgs);

        String jwt = jwtAuthor.createJWT("testsubject", claims);
        Map<String, Object> claimObjects = jwtConsumer.consume(jwt);

        List<String> recoveredAuthorities = (List<String>) claimObjects.get(JWTUserConverter.AUTHORITIES);
        assertEquals(authorities.get(0), recoveredAuthorities.get(0));

        Map<String, Object> recoveredOrgs = (Map<String, Object>) claimObjects.get(JWTUserConverter.ORGANIZATIONS);
        assertEquals(orgs.get(0), recoveredOrgs.get(0));
        assertEquals(orgs.get(1), recoveredOrgs.get(1));
        assertEquals(orgs.get(2), recoveredOrgs.get(2));
    }

}
