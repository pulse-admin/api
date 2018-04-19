package gov.ca.emsa.pulse.auth.authentication;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import gov.ca.emsa.pulse.auth.jwt.JWTAuthorRsaJoseJImpl;
import gov.ca.emsa.pulse.auth.jwt.JWTCreationException;
import gov.ca.emsa.pulse.auth.jwt.JWTValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.ca.emsa.pulse.auth.TestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
public class UserAuthenticatorTest {

    @Autowired
    private Environment env;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private JWTAuthorRsaJoseJImpl jwtAuthor;

    @Test
    public void testRefreshJWT() throws JWTCreationException, JWTValidationException {

        String userId = "email";
        Map<String, Object> jwtClaims = new HashMap<String, Object>();
        List<String> auths = new ArrayList<String>();
        auths.add("ROLE_USER");
        jwtClaims.put(JWTUserConverter.AUTHORITIES, auths);
        List<String> ids = new ArrayList<String>();
        ids.add("First name");
        ids.add("Last name");
        ids.add("email");
        jwtClaims.put(JWTUserConverter.IDENTITY, ids);
        Map<String, Long> orgs = new HashMap<String, Long>();
        orgs.put("pulse_us", 1L);
        orgs.put("pulse_va", 2L);
        orgs.put("va_acf", 3L);
        jwtClaims.put(JWTUserConverter.ORGANIZATIONS, orgs);

        String oldJwt = jwtAuthor.createJWT(userId, jwtClaims);

        String jwt = authenticator.refreshJWT(oldJwt);
        assertNotNull(jwt);

        assertNotEquals(jwt, oldJwt);
    }
}
