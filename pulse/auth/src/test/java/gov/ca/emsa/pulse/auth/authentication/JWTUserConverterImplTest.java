package gov.ca.emsa.pulse.auth.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.ca.emsa.pulse.auth.jwt.JWTAuthorRsaJoseJImpl;
import gov.ca.emsa.pulse.auth.jwt.JWTCreationException;
import gov.ca.emsa.pulse.auth.jwt.JWTValidationException;
import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.auth.user.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.ca.emsa.pulse.auth.TestConfig.class
})
public class JWTUserConverterImplTest {

    @Autowired
    private JWTUserConverterImpl converter;

    @Autowired
    private JWTAuthorRsaJoseJImpl jwtAuthor;

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(JWTUserConverterImplTest.class);

    @Test
    public void converterConvertsJWTToUser() throws JWTCreationException, JWTValidationException {

        String userId = "user";
        Map<String, Object> jwtAuthorities = new HashMap<String, Object>();
        List<String> auths = new ArrayList<String>();
        HashMap<String, Long> orgsInfo = new HashMap<String, Long>();
        List<String> ids = new ArrayList<String>();
        auths.add("ROLE_USER");
        ids.add("user_id");
        ids.add("username");
        ids.add("full_name");
        ids.add("role");
        orgsInfo.put("pulse-ca", 1L);
        orgsInfo.put("acf-1", 2L);
        jwtAuthorities.put(JWTUserConverter.AUTHORITIES, auths);
        jwtAuthorities.put(JWTUserConverter.IDENTITY, ids);
        jwtAuthorities.put(JWTUserConverter.ORGANIZATIONS, orgsInfo);

        String jwt = jwtAuthor.createJWT(userId, jwtAuthorities);
        JWTAuthenticatedUser user = converter.getAuthenticatedUser(jwt);

        assertNotNull(user);

        assertEquals("user_id", user.getuser_id());
        assertEquals("full_name", user.getfull_name());
        assertEquals(1L, user.getLiferayStateId().longValue());
        assertEquals(2L, user.getLiferayAcfId().longValue());
    }

    @Test
    public void converterConvertsRejectsInvalidStringForJWTToUser() {

        String garbage = "Garbage In";

        Boolean throwsException = false;

        try {
            User user = converter.getAuthenticatedUser(garbage);
        } catch (JWTValidationException e) {
            LOG.info("JWT Validation Exception", e);
            LOG.debug("JWT Validation Exception", e);
            throwsException = true;
        }
        assertTrue(throwsException);
    }
}
