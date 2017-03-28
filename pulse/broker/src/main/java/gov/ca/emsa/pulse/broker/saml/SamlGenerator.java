package gov.ca.emsa.pulse.broker.saml;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;

public interface SamlGenerator {

	public XMLObjectBuilderFactory getSAMLBuilder() throws ConfigurationException;
	
	public String createSAML(SAMLInput input) throws MarshallingException;
	public org.w3c.dom.Element createSAMLElement(SAMLInput input) throws MarshallingException;
	/**
	 * Builds a SAML Attribute of type String
	 * @param name
	 * @param value
	 * @param builderFactory
	 * @return
	 * @throws ConfigurationException
	 */
	public Attribute buildStringAttribute(String name, String value, XMLObjectBuilderFactory builderFactory) throws ConfigurationException;

	/**
	 * Helper method which includes some basic SAML fields which are part of almost every SAML Assertion.
	 *
	 * @param input
	 * @return
	 */
	public Assertion buildDefaultAssertion(SAMLInput input);

	

}
