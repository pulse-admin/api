package gov.ca.emsa.pulse.broker.saml;

import gov.ca.emsa.pulse.auth.permission.GrantedPermission;
import gov.ca.emsa.pulse.auth.user.CommonUser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.security.RandomIdentifierGenerationStrategy;

import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class SamlUtil{
	
	private static final String SAML_USER_ID_STRING = "uid";
	private static final String SAML_USERNAME_STRING = "username";
	private static final String SAML_EMAIL_STRING = "email";
	private static final String SAML_AUTHSOURCE_STRING = "auth_source";
	private static final String SAML_AUTHSOURCE_STRING_VALUE = "PULSE-US";
	private static final String SAML_FULL_NAME_STRING = "full_name";
	private static final String SAML_ORG_STRING = "urn:oasis:names:tc:xspa:1.0:subject:organization";
	private static final String SAML_ORG_STRING_VALUE = "PULSE-US";
	private static final String SAML_SUBJECTID_STRING = "urn:oasis:names:tc:xspa:1.0:subject:subject-id";
	private static final String SAML_ROLE_STRING = "urn:oasis:names:tc:xacml:2.0:subject:role";
	private static final String SAML_ROLE_STRING_VALUE = "<Role xmlns=\"urn:hl7-org:v3\" code=\"112247003\" codeSystem=\"2.16.840.1.113883.6.96\" codeSystemName=\"SNOMED_CT\" displayName=\"Medical Doctor\" />";
	private static final String SAML_PURPOSEOFUSE_STRING = "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse";
	private static final String SAML_PURPOSEOFUSE_VALUE = "<PurposeOfUse xmlns=\"urn:hl7-org:v3\" code=\"TREATMENT\" codeSystem=\"2.16.840.1.113883.3.18.7.1\" codeSystemName=\"nhin-purpose\" displayName=\"Treatment\" />";
	private static final String SAML_ORGID_STRING = "urn:oasis:names:tc:xspa:1.0:subject:organization-id";
	private static final String SAML_HOMECOMMUNITYID_STRING = "urn:nhin:names:saml:homeCommunityId";
	private static final String SAML_FORMAT_BASIC = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";
	
	@Value("${saml.issuer}")
	private static String samlIssuer;
	@Value("${saml.issuer.format}")
	private static String samlIssuerFormat;
	@Value("${saml.id.entityId}")
	private static String samlIdEntity;
	@Value("${pulseOID}")
	private static String pulseOID;
	@Value("${keystoreUsername}")
	private static String alias;
	@Value("${keystorePath}")
	private static String keystorePath;
	@Value("${keystorePassword}")
	private static String keystorePassword;
	@Value("${keystoreUsername}")
	private static String keystoreUsername;
	
    public static String generateSecureRandomId(){
        return new RandomIdentifierGenerationStrategy().generateIdentifier();
    }
    
	public static <T> T buildSAMLObject(final Class<T> clazz){
		try {
			InitializationService.initialize();
		} catch (InitializationException e1) {
			e1.printStackTrace();
		}
        T object = null;
        try {
            XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
            QName defaultElementName = (QName)clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            object = (T)builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }

        return object;
    }
	
	static BasicCredential createCredential(){
    	KeyStore keystore = null;
    	try {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
    	FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(keystorePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	try {
			keystore.load(inputStream, keystorePassword.toCharArray());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	String storePassword = keystorePassword;
        String storeUsername = keystoreUsername;
        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put(storeUsername, storePassword);
    	KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwords);
    	
    	Criterion criteria = new EntityIdCriterion(storeUsername);
    	CriteriaSet criteriaSet = new CriteriaSet();
    	criteriaSet.add(criteria);
    	
    	BasicCredential credential = null;
		try {
			credential = (BasicCredential)resolver.resolveSingle(criteriaSet);
		} catch (ResolverException e) {
			e.printStackTrace();
		}
    	
    	return credential;
    }
	
	private static KeyInfo getKeyInfo(BasicCredential basicCredential) throws Exception {

        final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();

        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        keyInfoGeneratorFactory.setEmitPublicKeyValue(true);
        final KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

        return keyInfoGenerator.generate(basicCredential);
    }
	
	public static String signAndBuildStringAssertion(CommonUser user){
		Assertion assertion = buildAssertion(user);
		signAssertion(assertion);
		String stringAssertion = null;
		try {
			stringAssertion = XMLHelper.nodeToString(XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion));
		} catch (MarshallingException e) {
			e.printStackTrace();
		}
		
		return stringAssertion;
	}
    
    public static void signAssertion(Assertion assertion) {
    	
    	BasicCredential basicCredential = null;
		basicCredential = createCredential();
        
    	Signature signature = buildSAMLObject(Signature.class);
        signature.setSigningCredential(basicCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        try {
			signature.setKeyInfo(getKeyInfo(basicCredential));
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
        assertion.setSignature(signature);
        
        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }

        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
        
    }

    public static Assertion buildAssertion(CommonUser user) {

        Assertion assertion = buildSAMLObject(Assertion.class);

        Issuer issuer = buildSAMLObject(Issuer.class);
        issuer.setValue(samlIssuer);
        issuer.setFormat(samlIssuerFormat);
        assertion.setIssuer(issuer);
        assertion.setIssueInstant(new DateTime());

        assertion.setID(generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);

        Subject subject = buildSAMLObject(Subject.class);
        assertion.setSubject(subject);

        NameID nameID = buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.EMAIL);
        nameID.setValue(user.getEmail());
        
        nameID.setSPNameQualifier(samlIdEntity);

        subject.setNameID(nameID);

        subject.getSubjectConfirmations().add(buildSubjectConfirmation());

        assertion.setConditions(buildConditions());

        assertion.getAttributeStatements().add(buildAttributeStatement(user));

        assertion.getAuthnStatements().add(buildAuthnStatement());

        return assertion;
    }
    
    private static AuthnStatement buildAuthnStatement() {
        AuthnStatement authnStatement = buildSAMLObject(AuthnStatement.class);
        AuthnContext authnContext = buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
    	authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
    	
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setSessionIndex(generateSecureRandomId());
        
        authnStatement.setAuthnInstant(new DateTime());
        authnStatement.setSessionNotOnOrAfter(new DateTime().plusMinutes(1));
        return authnStatement;
    }

    
    private static Conditions buildConditions() {
        Conditions conditions = buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime().minusMinutes(1));
        conditions.setNotOnOrAfter(new DateTime().plusMinutes(1));
        
        
        AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class);
        Audience audience = buildSAMLObject(Audience.class);
        audience.setAudienceURI(samlIdEntity);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }


    private static SubjectConfirmation buildSubjectConfirmation() {
    	
        SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_HOLDER_OF_KEY);

        SubjectConfirmationData subjectConfirmationData = buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setInResponseTo("Made up ID");
        subjectConfirmationData.setNotBefore(new DateTime().minusMinutes(1));
        subjectConfirmationData.setNotOnOrAfter(new DateTime().plusMinutes(1));
        try {
			subjectConfirmationData.getUnknownXMLObjects().add(getKeyInfo(createCredential()));
		} catch (Exception e) {
			e.printStackTrace();
		}

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        return subjectConfirmation;
    }
    
    private static AttributeStatement buildAttributeStatement(CommonUser user) {
        
    	AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);
        
        putAttributeInStatement(attributeStatement, SAML_USER_ID_STRING, user.getuser_id());
        putAttributeInStatement(attributeStatement, SAML_USERNAME_STRING, user.getUsername());
        putAttributeInStatement(attributeStatement, SAML_EMAIL_STRING, user.getEmail());
        putAttributeInStatement(attributeStatement, SAML_AUTHSOURCE_STRING, SAML_AUTHSOURCE_STRING_VALUE);
        putAttributeInStatement(attributeStatement, SAML_FULL_NAME_STRING, user.getfull_name());
        putAttributeInStatement(attributeStatement, SAML_ORG_STRING, SAML_ORG_STRING_VALUE);
        putAttributeInStatement(attributeStatement, SAML_SUBJECTID_STRING, user.getfull_name());
        putAttributeInStatement(attributeStatement, SAML_HOMECOMMUNITYID_STRING, pulseOID);
        putAttributeInStatement(attributeStatement, SAML_ORGID_STRING, pulseOID);
        
        for(GrantedPermission role : user.getAuthorities()){	
        	XSStringBuilder stringBuilder = (XSStringBuilder)XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
    		Attribute attributeObject = buildSAMLObject(Attribute.class);
        	XSString attrStrValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            attrStrValue.setValue(SAML_ROLE_STRING_VALUE);
        	attributeObject.getAttributeValues().add(attrStrValue);
        	attributeObject.setName(SAML_ROLE_STRING);
        	attributeStatement.getAttributes().add(attributeObject);
        }
        
        XSStringBuilder stringBuilder = (XSStringBuilder)XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
		Attribute attributeObject = buildSAMLObject(Attribute.class);
    	XSString attrStrValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrStrValue.setValue(SAML_PURPOSEOFUSE_VALUE);
    	attributeObject.getAttributeValues().add(attrStrValue);
    	attributeObject.setNameFormat(SAML_FORMAT_BASIC);
    	attributeObject.setName(SAML_PURPOSEOFUSE_STRING);
    	attributeStatement.getAttributes().add(attributeObject);
        
        return attributeStatement;
    }
    
    private static void putAttributeInStatement(AttributeStatement statement, String attrName, String attrValue){
    	XSStringBuilder stringBuilder = (XSStringBuilder)XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
    	Attribute attributeObject = buildSAMLObject(Attribute.class);
        XSString attrStrValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrStrValue.setValue(attrValue);
        attributeObject.getAttributeValues().add(attrStrValue);
        attributeObject.setName(attrName);
        attributeObject.setNameFormat(Attribute.BASIC);
        statement.getAttributes().add(attributeObject);
    }
    
    @Value("${saml.issuer}")
    public void setSamlIssuer(String issuerIn) {
        samlIssuer = issuerIn;
    }
	@Value("${saml.issuer.format}")
    public void setSamlIssuerFomat(String issuerFormatIn) {
        samlIssuerFormat = issuerFormatIn;
    }
	@Value("${saml.id.entityId}")
    public void setSamlEntityId(String entityIdIn) {
        samlIdEntity = entityIdIn;
    }
	@Value("${pulseOID}")
    public void setPulseOID(String pulseOIDIn) {
        pulseOID = pulseOIDIn;
    }
	@Value("${keystoreUsername}")
    public void setUsername(String keyStoreUsernameIn) {
        keystoreUsername = keyStoreUsernameIn;
    }
	@Value("${keystorePath}")
    public void setKeystorePath(String keystorePathIn) {
        keystorePath = keystorePathIn;
    }
	@Value("${keystorePassword}")
    public void setKeystorePassword(String keystorePassIn) {
        keystorePassword = keystorePassIn;
    }
    
}
