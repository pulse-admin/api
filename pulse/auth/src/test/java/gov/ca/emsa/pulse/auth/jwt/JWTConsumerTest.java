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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = { gov.ca.emsa.pulse.auth.TestConfig.class })
public class JWTConsumerTest {

	@Autowired
	private JWTAuthor jwtAuthor;

	@Autowired
	private JWTConsumer jwtConsumer;

	@Test
	public void consumerIsNotNull(){
		assertNotNull(jwtConsumer);
	}

    @Test
    public void testConsumer() throws Exception {

		Map<String, List<String> > claims = new HashMap<String, List<String> >();
		List<String> authorities = new ArrayList<String>();
		authorities.add("ROLE_SUPERSTAR");

		claims.put("Authorities", authorities);
		String jwt = jwtAuthor.createJWT("testsubject" ,claims);

		Map<String, Object> claimObjects = jwtConsumer.consume(jwt);

		List<String> recoveredAuthorities = (List<String>) claimObjects.get("Authorities");
		assertEquals(authorities.get(0), recoveredAuthorities.get(0));
    }
}
