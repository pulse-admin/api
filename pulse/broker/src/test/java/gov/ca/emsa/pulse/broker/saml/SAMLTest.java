package gov.ca.emsa.pulse.broker.saml;

import static org.junit.Assert.*;
import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BrokerApplicationTestConfig.class})
public class SAMLTest {
	
	@Test
	@Ignore
	public void testSAML() throws SignatureException{
		
		CommonUser user = new CommonUser();
		user.setEmail("test@test.com");
		user.setfull_name("Brian Lindsey");
		user.setusername("blindsey");
		user.setId(1L);
		
		Assertion assertion = SamlUtil.buildAssertion(user);
		SamlUtil.signAssertion(assertion);
		
		SignatureValidator.validate(assertion.getSignature(), SamlUtil.createCredential());
	}

}
