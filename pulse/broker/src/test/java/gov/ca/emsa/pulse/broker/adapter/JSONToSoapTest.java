package gov.ca.emsa.pulse.broker.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gov.ca.emsa.pulse.auth.jwt.JWTValidationException;
import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.broker.adapter.service.EHealthQueryProducerService;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.DocumentQueryResults;
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.DocumentIdentifier;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.PatientRecord;
import gov.ca.emsa.pulse.common.domain.PatientSearch;
import gov.ca.emsa.pulse.common.domain.PatientSearchAddress;
import gov.ca.emsa.pulse.common.domain.PatientSearchName;
import gov.ca.emsa.pulse.common.soap.JSONToSOAPService;
import gov.ca.emsa.pulse.common.soap.SOAPToJSONService;
import gov.ca.emsa.pulse.cten.IheStatus;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;

import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.apache.wss4j.dom.str.STRParser.REFERENCE_TYPE;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.common.SAMLException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Assert;
import org.w3c.dom.DOMException;
import org.xml.sax.InputSource;
import org.yaml.snakeyaml.reader.StreamReader;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = BrokerApplicationTestConfig.class)
public class JSONToSoapTest {

	@Autowired JSONToSOAPService service;
	@Autowired SOAPToJSONService reverseService;
	@Autowired EHealthQueryProducerService ehealthService;
	@Autowired private ResourceLoader resourceLoader;
	@Autowired EHealthAdapter eHealthAdapter;
	@Autowired EHealthQueryProducerService queryProducer;
	
	 private WSSecurityEngine secEngine = new WSSecurityEngine();
	 private Crypto crypto;
	
	@Value("${pulseOID}")
	private String PULSE_ID;
	
	public static final String HOME_COMMUNITY_ID = "2.16.840.1.113883.9.224";
	private static final String UCDAVIS_MANAGE_ORG = "UC Davis Health";
	private static final String SCHIE_MANAGE_ORG = "Santa Cruz Health Information Exchange";
	private static final String OCPRHIO_MANAGE_ORG = "OCPRHIO";
	@Value("${ocprhioOID}")
	private String ocprhioOID;
	
	@Value("${schieOID}")
	private String santaCruzOID;
	
	@Value("${ucdavisOID}")
	private String ucdavisOID;
	
	public static final String SAMPLE_SOAP_MSG = 
	        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
	        + "<SOAP-ENV:Envelope "
	        +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
	        +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
	        +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" 
	        +   "<SOAP-ENV:Body>" 
	        +       "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">" 
	        +           "<value xmlns=\"\">15</value>" 
	        +       "</add>" 
	        +   "</SOAP-ENV:Body>" 
	        + "</SOAP-ENV:Envelope>";
	
	
	public String getAssertion() throws IOException, ConfigurationException{
		Resource pdFile = resourceLoader.getResource("classpath:assertion.xml");
		return Resources.toString(pdFile.getURL(), Charsets.UTF_8);
	}
	
	 private WSHandlerResult verify(org.w3c.dom.Document doc) throws Exception {
		 try {
				crypto = CryptoFactory.getInstance();
			} catch (WSSecurityException e1) {
				e1.printStackTrace();
			}
		 return secEngine.processSecurityHeader(doc,null, null, crypto);
	}
	
	public static org.w3c.dom.Document toSOAPPart(String xml) throws Exception {
		InputSource in = new InputSource(new StringReader(xml));
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        return (org.w3c.dom.Document) docBuilder.parse(in);
    }
	
	@Test
	@Ignore
	public void testSignedHeader() throws Exception {
		org.w3c.dom.Document document = toSOAPPart(SAMPLE_SOAP_MSG);
		queryProducer.addSignedSecurityHeading(document, "1");
		String outputString = XMLUtils.prettyDocumentToString(document);
		System.out.println(outputString);
		//verify(document);
	}

	@Test
	@Ignore
	public void testUnmarshallPatientDiscoveryExampleRequest() throws IOException, SAMLException, SOAPException {
		Resource pdFile = resourceLoader.getResource("classpath:NHINPatientDiscoveryRequest.xml");
		String pdRequestStr = Resources.toString(pdFile.getURL(), Charsets.UTF_8);
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(pdRequestStr);
		assertNotNull(unmarshalledRequest);
	}

	@Test
	@Ignore
	public void testUnmarshallDocumentDiscoveryExampleRequest() throws IOException, SAMLException, SOAPException {
		Resource pdFile = resourceLoader.getResource("classpath:NHINQueryForDocumentsRequest.xml");
		String pdRequestStr = Resources.toString(pdFile.getURL(), Charsets.UTF_8);
		AdhocQueryRequest unmarshalledRequest = ehealthService.unMarshallDocumentQueryRequestObject(pdRequestStr);
		assertNotNull(unmarshalledRequest);
	}

	@Test
	@Ignore
	public void testCreatePatientDiscoveryRequest() throws Exception {
		PatientSearch ps = new PatientSearch();
		PatientSearchName toCreate1 = new PatientSearchName();
		toCreate1.setFamilyName("Lindsey");
		toCreate1.setPrefix("Mr.");
		toCreate1.setSuffix("MDS");
		List<PatientSearchAddress> addresses = new ArrayList<PatientSearchAddress>();
		PatientSearchAddress psa = new PatientSearchAddress();
		psa.setCity(null);
		psa.setState(null);
		psa.setZipcode(null);
		psa.setLines(new ArrayList<String>());
		addresses.add(psa);
		ps.setAddresses(addresses);

		ArrayList<String> givens = new ArrayList<String>();
		givens.add("Brian");
		givens.add("Bryan");
		givens.add("Briaann");
		toCreate1.setGivenName(givens);
		ps.setDob("19830205");
		ps.setGender("F");
		ps.setSsn("123456789");
		ps.setTelephone("4439871013");
		ArrayList<PatientSearchName> names = new ArrayList<PatientSearchName>();
		names.add(toCreate1);
		ps.setPatientNames(names);
		ps.getPatientNames().get(0).getGivenName().add("Brian");

		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");
		endpoint.setManagingOrganization(OCPRHIO_MANAGE_ORG);
		String orgOID = eHealthAdapter.getOrganizationOID(endpoint.getManagingOrganization());
		assertEquals(orgOID,ocprhioOID);
		PRPAIN201305UV02 request = service.convertFromPatientSearch(ps,PULSE_ID, orgOID);
		String requestXml = ehealthService.marshallPatientDiscoveryRequest(endpoint, getAssertion(), request);
		//verify(toSOAPPart(requestXml));
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
		PatientSearch unmarshalledSearch = reverseService.convertToPatientSearch(unmarshalledRequest);
		assertNotNull(unmarshalledSearch);
		assertEquals(ps.getPatientNames().get(0).getFamilyName(), unmarshalledSearch.getPatientNames().get(0).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getSsn(), unmarshalledSearch.getSsn());
		assertEquals(ps.getTelephone(), unmarshalledSearch.getTelephone());
		assertEquals(ps.getPatientNames().get(0).getPrefix(), unmarshalledSearch.getPatientNames().get(0).getPrefix());
		assertEquals(ps.getPatientNames().get(0).getSuffix(), unmarshalledSearch.getPatientNames().get(0).getSuffix());
		assertEquals(ps.getPatientNames().get(0).getGivenName(), unmarshalledSearch.getPatientNames().get(0).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
	}
	
	@Test
	@Ignore
	public void testCreatePatientDiscoveryXDSToolsRequest() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, MarshallingException, ConfigurationException, WSSecurityException {
		PatientSearch ps = new PatientSearch();
		PatientSearchName toCreate1 = new PatientSearchName();
		toCreate1.setFamilyName("Lindsey");
		
		PatientSearchAddress psa = new PatientSearchAddress();
		psa.setCity("Bel AIr");
		psa.setState("MD");
		psa.setZipcode("21015");
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("406 Main Street");
		lines.add("Apt 6B");
		psa.setLines(lines);
		ArrayList<PatientSearchAddress> addresses = new ArrayList<PatientSearchAddress>();
		addresses.add(psa);
		ps.setAddresses(addresses);

		ArrayList<String> givens = new ArrayList<String>();
		givens.add("Brian");
		toCreate1.setGivenName(givens);
		ps.setDob("19830205");
		ps.setGender("F");
		ps.setSsn("123456789");
		ps.setTelephone("4439871013");
		ArrayList<PatientSearchName> names = new ArrayList<PatientSearchName>();
		names.add(toCreate1);
		ps.setPatientNames(names);
		ps.getPatientNames().get(0).getGivenName().add("Brian");
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");
		customAttributes.put("PatientFamilyName", ps.getPatientNames().get(0).getFamilyName());
		customAttributes.put("PatientDOB", ps.getDob());
		customAttributes.put("PatientGender", ps.getGender());
		customAttributes.put("PatientHomeZip", ps.getZip());
		customAttributes.put("PatientSSN", ps.getSsn());
		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");
		endpoint.setManagingOrganization(SCHIE_MANAGE_ORG);
		String orgOID = eHealthAdapter.getOrganizationOID(endpoint.getManagingOrganization());
		assertEquals(orgOID,santaCruzOID);
		PRPAIN201305UV02 request = service.convertFromPatientSearch(ps, PULSE_ID, orgOID);
		String requestXml = ehealthService.marshallPatientDiscoveryRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
		PatientSearch unmarshalledSearch = reverseService.convertToPatientSearch(unmarshalledRequest);
		assertNotNull(unmarshalledSearch);
		assertEquals(ps.getPatientNames().get(0).getFamilyName(), unmarshalledSearch.getPatientNames().get(0).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getSsn(), unmarshalledSearch.getSsn());
		assertEquals(ps.getTelephone(), unmarshalledSearch.getTelephone());
		assertEquals(ps.getPatientNames().get(0).getPrefix(), unmarshalledSearch.getPatientNames().get(0).getPrefix());
		assertEquals(ps.getPatientNames().get(0).getSuffix(), unmarshalledSearch.getPatientNames().get(0).getSuffix());
		assertEquals(ps.getPatientNames().get(0).getGivenName(), unmarshalledSearch.getPatientNames().get(0).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
	}

	@Test
	@Ignore
	public void testCreatePatientDiscoveryRequestWithAddress() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, MarshallingException, ConfigurationException, WSSecurityException {
		PatientSearch ps = new PatientSearch();
		PatientSearchName toCreate1 = new PatientSearchName();
		toCreate1.setFamilyName("Lindsey");
		toCreate1.setPrefix("Mr.");
		toCreate1.setSuffix("MDS");

		PatientSearchAddress psa = new PatientSearchAddress();
		psa.setCity("Bel AIr");
		psa.setState("MD");
		psa.setZipcode("21015");
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("406 Main Street");
		lines.add("Apt 6B");
		psa.setLines(lines);
		ArrayList<PatientSearchAddress> addresses = new ArrayList<PatientSearchAddress>();
		addresses.add(psa);
		ps.setAddresses(addresses);

		ArrayList<String> givens = new ArrayList<String>();
		givens.add("Brian");
		givens.add("Bryan");
		givens.add("Briaann");
		toCreate1.setGivenName(givens);
		ps.setDob("19830205");
		ps.setGender("F");
		ps.setSsn("123456789");
		ps.setTelephone("4439871013");
		ArrayList<PatientSearchName> names = new ArrayList<PatientSearchName>();
		names.add(toCreate1);
		ps.setPatientNames(names);
		ps.getPatientNames().get(0).getGivenName().add("Brian");
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");
		customAttributes.put("PatientFamilyName", ps.getPatientNames().get(0).getFamilyName());
		customAttributes.put("PatientDOB", ps.getDob());
		customAttributes.put("PatientGender", ps.getGender());
		customAttributes.put("PatientHomeZip", ps.getZip());
		customAttributes.put("PatientSSN", ps.getSsn());
		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");
		endpoint.setManagingOrganization(UCDAVIS_MANAGE_ORG);
		String orgOID = eHealthAdapter.getOrganizationOID(endpoint.getManagingOrganization());
		assertEquals(orgOID,ucdavisOID);
		PRPAIN201305UV02 request = service.convertFromPatientSearch(ps, PULSE_ID, orgOID);
		String requestXml = ehealthService.marshallPatientDiscoveryRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
		PatientSearch unmarshalledSearch = reverseService.convertToPatientSearch(unmarshalledRequest);
		assertNotNull(unmarshalledSearch);
		assertEquals(ps.getPatientNames().get(0).getFamilyName(), unmarshalledSearch.getPatientNames().get(0).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getSsn(), unmarshalledSearch.getSsn());
		assertEquals(ps.getTelephone(), unmarshalledSearch.getTelephone());
		assertEquals(ps.getAddresses().get(0).getCity(), unmarshalledSearch.getAddresses().get(0).getCity());
		assertEquals(ps.getAddresses().get(0).getState(), unmarshalledSearch.getAddresses().get(0).getState());
		assertEquals(ps.getAddresses().get(0).getZipcode(), unmarshalledSearch.getAddresses().get(0).getZipcode());
		assertEquals(ps.getAddresses().get(0).getLines().get(0), unmarshalledSearch.getAddresses().get(0).getLines().get(0));
		assertEquals(ps.getAddresses().get(0).getLines().get(1), unmarshalledSearch.getAddresses().get(0).getLines().get(1));
		assertEquals(ps.getPatientNames().get(0).getPrefix(), unmarshalledSearch.getPatientNames().get(0).getPrefix());
		assertEquals(ps.getPatientNames().get(0).getSuffix(), unmarshalledSearch.getPatientNames().get(0).getSuffix());
		assertEquals(ps.getPatientNames().get(0).getGivenName(), unmarshalledSearch.getPatientNames().get(0).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
	}

	@Test
	@Ignore
	public void testCreatePatientDiscoveryRequestWithMultipleAddress() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, MarshallingException, ConfigurationException, WSSecurityException {
		PatientSearch ps = new PatientSearch();
		PatientSearchName toCreate1 = new PatientSearchName();
		toCreate1.setFamilyName("Lindsey");
		toCreate1.setPrefix("Mr.");
		toCreate1.setSuffix("MDS");

		PatientSearchAddress psa = new PatientSearchAddress();
		psa.setCity("Bel AIr");
		psa.setState("MD");
		psa.setZipcode("21015");
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("406 Main Street");
		lines.add("Apt 6B");
		psa.setLines(lines);
		ArrayList<PatientSearchAddress> addresses = new ArrayList<PatientSearchAddress>();
		addresses.add(psa);

		PatientSearchAddress psa2 = new PatientSearchAddress();
		psa2.setCity("Baltimore");
		psa2.setState("MD");
		psa2.setZipcode("21230");
		ArrayList<String> lines2 = new ArrayList<String>();
		lines2.add("1300 Light Street");
		lines2.add("Apt 1B");
		psa2.setLines(lines2);
		addresses.add(psa2);
		ps.setAddresses(addresses);

		ArrayList<String> givens = new ArrayList<String>();
		givens.add("Brian");
		givens.add("Bryan");
		givens.add("Briaann");
		toCreate1.setGivenName(givens);
		ps.setDob("19830205");
		ps.setGender("F");
		ps.setSsn("123456789");
		ps.setTelephone("4439871013");
		ArrayList<PatientSearchName> names = new ArrayList<PatientSearchName>();
		names.add(toCreate1);
		ps.setPatientNames(names);
		ps.getPatientNames().get(0).getGivenName().add("Brian");
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");
		customAttributes.put("PatientFamilyName", ps.getPatientNames().get(0).getFamilyName());
		customAttributes.put("PatientDOB", ps.getDob());
		customAttributes.put("PatientGender", ps.getGender());
		customAttributes.put("PatientHomeZip", ps.getZip());
		customAttributes.put("PatientSSN", ps.getSsn());
		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");
		endpoint.setManagingOrganization("Sutter Health");
		String orgOID = eHealthAdapter.getOrganizationOID(endpoint.getManagingOrganization());
		assertEquals(orgOID,HOME_COMMUNITY_ID);
		PRPAIN201305UV02 request = service.convertFromPatientSearch(ps, PULSE_ID, PULSE_ID);
		String requestXml = ehealthService.marshallPatientDiscoveryRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
		PatientSearch unmarshalledSearch = reverseService.convertToPatientSearch(unmarshalledRequest);
		assertNotNull(unmarshalledSearch);
		assertEquals(ps.getPatientNames().get(0).getFamilyName(), unmarshalledSearch.getPatientNames().get(0).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getSsn(), unmarshalledSearch.getSsn());
		assertEquals(ps.getTelephone(), unmarshalledSearch.getTelephone());
		assertEquals(ps.getAddresses().get(0).getCity(), unmarshalledSearch.getAddresses().get(0).getCity());
		assertEquals(ps.getAddresses().get(0).getState(), unmarshalledSearch.getAddresses().get(0).getState());
		assertEquals(ps.getAddresses().get(0).getZipcode(), unmarshalledSearch.getAddresses().get(0).getZipcode());
		assertEquals(ps.getAddresses().get(0).getLines().get(0), unmarshalledSearch.getAddresses().get(0).getLines().get(0));
		assertEquals(ps.getAddresses().get(0).getLines().get(1), unmarshalledSearch.getAddresses().get(0).getLines().get(1));
		assertEquals(ps.getAddresses().get(1).getCity(), unmarshalledSearch.getAddresses().get(1).getCity());
		assertEquals(ps.getAddresses().get(1).getState(), unmarshalledSearch.getAddresses().get(1).getState());
		assertEquals(ps.getAddresses().get(1).getZipcode(), unmarshalledSearch.getAddresses().get(1).getZipcode());
		assertEquals(ps.getAddresses().get(1).getLines().get(0), unmarshalledSearch.getAddresses().get(1).getLines().get(0));
		assertEquals(ps.getAddresses().get(1).getLines().get(1), unmarshalledSearch.getAddresses().get(1).getLines().get(1));
		assertEquals(ps.getPatientNames().get(0).getPrefix(), unmarshalledSearch.getPatientNames().get(0).getPrefix());
		assertEquals(ps.getPatientNames().get(0).getSuffix(), unmarshalledSearch.getPatientNames().get(0).getSuffix());
		assertEquals(ps.getPatientNames().get(0).getGivenName(), unmarshalledSearch.getPatientNames().get(0).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
	}

	@Test
	@Ignore
	public void testCreatePatientDiscoveryRequestWithoutOptionalparams() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, MarshallingException, ConfigurationException, WSSecurityException {
		PatientSearch ps = new PatientSearch();
		PatientSearchName toCreate1 = new PatientSearchName();
		toCreate1.setFamilyName("Lindsey");
		toCreate1.setPrefix("Mr.");
		toCreate1.setSuffix("MDS");
		List<PatientSearchAddress> addresses = new ArrayList<PatientSearchAddress>();
		PatientSearchAddress psa = new PatientSearchAddress();
		psa.setCity(null);
		psa.setState(null);
		psa.setZipcode(null);
		psa.setLines(new ArrayList<String>());
		addresses.add(psa);
		ps.setAddresses(addresses);

		ArrayList<String> givens = new ArrayList<String>();
		givens.add("Brian");
		givens.add("Bryan");
		givens.add("Briaann");
		toCreate1.setGivenName(givens);
		ps.setDob("19830205");
		ps.setGender("F");
		ArrayList<PatientSearchName> names = new ArrayList<PatientSearchName>();
		names.add(toCreate1);
		ps.setPatientNames(names);
		ps.getPatientNames().get(0).getGivenName().add("Brian");
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");
		customAttributes.put("PatientFamilyName", ps.getPatientNames().get(0).getFamilyName());
		customAttributes.put("PatientDOB", ps.getDob());
		customAttributes.put("PatientGender", ps.getGender());
		customAttributes.put("PatientHomeZip", ps.getZip());
		customAttributes.put("PatientSSN", ps.getSsn());
		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");

		PRPAIN201305UV02 request = service.convertFromPatientSearch(ps, PULSE_ID, PULSE_ID);
		String requestXml = ehealthService.marshallPatientDiscoveryRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
		PatientSearch unmarshalledSearch = reverseService.convertToPatientSearch(unmarshalledRequest);
		assertNotNull(unmarshalledSearch);
		assertEquals(ps.getPatientNames().get(0).getFamilyName(), unmarshalledSearch.getPatientNames().get(0).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getPatientNames().get(0).getPrefix(), unmarshalledSearch.getPatientNames().get(0).getPrefix());
		assertEquals(ps.getPatientNames().get(0).getSuffix(), unmarshalledSearch.getPatientNames().get(0).getSuffix());
		assertEquals(ps.getPatientNames().get(0).getGivenName(), unmarshalledSearch.getPatientNames().get(0).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
	}

	@Test
	@Ignore
	public void testCreatePatientDiscoveryRequestMultipleNames() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, MarshallingException, ConfigurationException, WSSecurityException {
		PatientSearch ps = new PatientSearch();
		List<PatientSearchAddress> addresses = new ArrayList<PatientSearchAddress>();
		PatientSearchAddress psa = new PatientSearchAddress();
		psa.setCity(null);
		psa.setState(null);
		psa.setZipcode(null);
		psa.setLines(new ArrayList<String>());
		addresses.add(psa);
		ps.setAddresses(addresses);
		PatientSearchName toCreate1 = new PatientSearchName();
		toCreate1.setFamilyName("Lindsey");
		toCreate1.setPrefix("Mr.");
		toCreate1.setSuffix("MDS");
		PatientSearchName toCreate2 = new PatientSearchName();
		toCreate2.setFamilyName("Doe");
		toCreate2.setPrefix("Sgt.");
		toCreate2.setSuffix("MD");

		ArrayList<String> givens = new ArrayList<String>();
		givens.add("Brian");
		givens.add("Bryan");
		givens.add("Briaann");
		toCreate1.setGivenName(givens);

		ArrayList<String> givens2 = new ArrayList<String>();
		givens2.add("Jon");
		givens2.add("John");
		givens2.add("Jahn");
		toCreate1.setGivenName(givens2);

		ps.setDob("19830205");
		ps.setSsn("123456789");
		ps.setTelephone("4439871013");
		ps.setGender("F");
		ArrayList<PatientSearchName> names = new ArrayList<PatientSearchName>();
		names.add(toCreate1);
		names.add(toCreate2);
		ps.setPatientNames(names);
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");
		customAttributes.put("PatientFamilyName", ps.getPatientNames().get(0).getFamilyName());
		customAttributes.put("PatientDOB", ps.getDob());
		customAttributes.put("PatientGender", ps.getGender());
		customAttributes.put("PatientHomeZip", ps.getZip());
		customAttributes.put("PatientSSN", ps.getSsn());
		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");

		PRPAIN201305UV02 request = service.convertFromPatientSearch(ps, PULSE_ID, PULSE_ID);
		String requestXml = ehealthService.marshallPatientDiscoveryRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		PRPAIN201305UV02 unmarshalledRequest = ehealthService.unMarshallPatientDiscoveryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
		PatientSearch unmarshalledSearch = reverseService.convertToPatientSearch(unmarshalledRequest);
		assertNotNull(unmarshalledSearch);
		assertEquals(ps.getPatientNames().get(0).getFamilyName(), unmarshalledSearch.getPatientNames().get(0).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getPatientNames().get(0).getPrefix(), unmarshalledSearch.getPatientNames().get(0).getPrefix());
		assertEquals(ps.getPatientNames().get(0).getSuffix(), unmarshalledSearch.getPatientNames().get(0).getSuffix());
		assertEquals(ps.getPatientNames().get(0).getGivenName(), unmarshalledSearch.getPatientNames().get(0).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());

		assertEquals(ps.getPatientNames().get(1).getFamilyName(), unmarshalledSearch.getPatientNames().get(1).getFamilyName());
		assertEquals(ps.getDob(), unmarshalledSearch.getDob());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
		assertEquals(ps.getPatientNames().get(1).getPrefix(), unmarshalledSearch.getPatientNames().get(1).getPrefix());
		assertEquals(ps.getPatientNames().get(1).getSuffix(), unmarshalledSearch.getPatientNames().get(1).getSuffix());
		assertEquals(ps.getPatientNames().get(1).getGivenName(), unmarshalledSearch.getPatientNames().get(1).getGivenName());
		assertEquals(ps.getGender(), unmarshalledSearch.getGender());
	}

	@Test
	@Ignore
	public void testParsePatientDiscoveryResponse() throws SAMLException, SOAPException, JAXBException, IOException {
		Resource pdFile = resourceLoader.getResource("classpath:NHINPatientDiscoveryResponse.xml");
		String pdResponseStr = Resources.toString(pdFile.getURL(), Charsets.UTF_8);
		PRPAIN201306UV02 resultObj = ehealthService.unMarshallPatientDiscoveryResponseObject(pdResponseStr);
		assertNotNull(resultObj);
		List<PatientRecord> patientRecords = reverseService.convertToPatientRecords(resultObj);
		assertNotNull(patientRecords);
		assertEquals(2, patientRecords.size());

		PatientRecord firstPatient = patientRecords.get(0);
		assertEquals("James", firstPatient.getPatientRecordName().get(0).getGivenName().get(0));
		assertEquals("Jones", firstPatient.getPatientRecordName().get(0).getFamilyName());
		assertEquals("tel:+1-481-555-7684;ext=2342", firstPatient.getPhoneNumber());
		assertEquals("19630804", firstPatient.getDateOfBirth());
		assertEquals("M", firstPatient.getGender().getCode());
		//PatientRecordAddress firstPatientAddress = firstPatient.getAddress();

		//assertNotNull(firstPatientAddress);
		//assertEquals("3443 North Arctic Avenue", firstPatientAddress.getStreet1());
		//assertNull(firstPatientAddress.getStreet2());
		//assertEquals("Some City", firstPatientAddress.getCity());
		//assertEquals("IL", firstPatientAddress.getState());
	}

	@Test
	@Ignore
	public void testCreateDocumentQueryRequest() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, ConfigurationException, DOMException, MarshallingException, WSSecurityException {
		Patient patient = new Patient();
		patient.setExternalPatientId("11.5.4.4.6667.110");
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");
		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");

		AdhocQueryRequest request = service.convertToDocumentRequest("1");
		String requestXml = ehealthService.marshallDocumentQueryRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
		AdhocQueryRequest unmarshalledRequest = ehealthService.unMarshallDocumentQueryRequestObject(requestXml);
		assertNotNull(unmarshalledRequest);
	}

	// TODO
	//@Test
	public void testDocumentQueryRequestXDSTools() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, DOMException, MarshallingException, IOException, ConfigurationException {
		final String XCQEndpoint = "http://localhost:8080/xdstools-4.3.4/sim/default__rg_mock_hie/rg/xcq";
		final String patientId = "P20170327093045.2^^^&1.3.6.1.4.1.21367.2005.13.20.1000&ISO";
		PatientEndpointMapDTO plmDto = new PatientEndpointMapDTO();
		plmDto.setExternalPatientRecordId(patientId);
		HashMap<String, String> customAttributes = new HashMap<String,String>();
		customAttributes.put("RequesterFirstName", "Katy");
		customAttributes.put("RequestReason", "Patient is bleeding.");

		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl(XCQEndpoint);

		CommonUser user = new CommonUser();
		user.setEmail("blindey@ainq.com");
		user.setSubjectName("Brian");
		user.setusername("blindsey");
		user.setfull_name("Brian Lindsey");
		user.setrole("USER");


		// CommonUser user, EndpointDTO endpoint, PatientLocationMapDTO locationMapDTO, SAMLInput samlInput
		DocumentQueryResults dqr = null;
		try {
			dqr = eHealthAdapter.queryDocuments(user, endpoint, plmDto, getAssertion());
		} catch (UnknownHostException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Assert.notNull(dqr);
		Assert.notNull(dqr.getResults());
		for(DocumentDTO docDto : dqr.getResults()){
			Assert.notNull(docDto.getHomeCommunityId());
			Assert.notNull(docDto.getDocumentUniqueId());
			Assert.notNull(docDto.getRepositoryUniqueId());
		}
	}

	// TODO
	//@Test
	public void testDocumentRetrieveRequestXDSTools() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException {
		final String XCQEndpoint = "http://localhost:8080/xdstools-4.3.4/sim/default__rg_mock_hie/rg/xcr";
		final String patientId = "P20170327093045.2^^^&1.3.6.1.4.1.21367.2005.13.20.1000&ISO";
		final String homeCommunityId = "urn:oid:1.1.4567334.1.1";
		final String repositoryUniqueId = "1.1.4567332.1.2";
		final String documentUniqueId = "1.42.20170329160551.2";
		
		List<DocumentDTO> docs = new ArrayList<DocumentDTO>();
		Document doc = new Document();
		DocumentIdentifier docId = new DocumentIdentifier();
		docId.setHomeCommunityId(homeCommunityId);
		docId.setRepositoryUniqueId(repositoryUniqueId);
		docId.setDocumentUniqueId(documentUniqueId);
		doc.setIdentifier(docId);
		docs.add(DomainToDtoConverter.convert(doc));
		
		PatientEndpointMapDTO plmDto = new PatientEndpointMapDTO();
		plmDto.setExternalPatientRecordId(patientId);

		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl(XCQEndpoint);

		CommonUser user = new CommonUser();
		user.setEmail("blindey@ainq.com");
		user.setSubjectName("Brian");
		user.setusername("blindsey");
		user.setfull_name("Brian Lindsey");
		user.setrole("USER");


		//CommonUser user, EndpointDTO endpoint, PatientLocationMapDTO locationMapDTO, SAMLInput samlInput
		try {
			eHealthAdapter.retrieveDocumentsContents(user, endpoint, docs, plmDto, getAssertion());
		} catch (UnknownHostException | UnsupportedEncodingException | IheErrorException | MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.notNull(docs.get(0).getContents());
	}

	// TODO
	//@Test
	public void unMarshallDocumentSetRetrieve() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException {
		Resource pdFile = resourceLoader.getResource("classpath:pulse_document_set_request_multipart_response.txt");
		String request = Resources.toString(pdFile.getURL(), Charsets.UTF_8);
		RetrieveDocumentSetResponseType resultObj = queryProducer.unMarshallDocumentSetRetrieveResponseObject(request);
		Assert.notNull(resultObj);
	}

	@Test
	@Ignore
	public void testParseDocumentQueryResponse() throws SAMLException, SOAPException, JAXBException, IOException {
		Resource dqFile = resourceLoader.getResource("classpath:NHINQueryForDocumentsResponse.xml");
		String dqResponseStr = Resources.toString(dqFile.getURL(), Charsets.UTF_8);
		AdhocQueryResponse resultObj = ehealthService.unMarshallDocumentQueryResponseObject(dqResponseStr);
		assertNotNull(resultObj);
		List<Document> docs = reverseService.convertToDocumentQueryResponse(resultObj);
		assertNotNull(docs);
		assertEquals(1, docs.size());
		Document doc = docs.get(0);
		assertEquals("20080516", doc.getCreationTime());
		assertEquals("urn:oid:2.16.840.1.113883.3.166", doc.getIdentifier().getHomeCommunityId());
		assertEquals("2.16.840.1.113883.3.166.3.1", doc.getIdentifier().getRepositoryUniqueId());
		assertEquals("35452", doc.getSize());
		assertEquals("Physical Test", doc.getName());
		assertNull(doc.getDescription());
		assertEquals("SUMMARIZATION OF EPISODE NOTE", doc.getClassName());
		assertEquals("Normal", doc.getConfidentiality());
		assertEquals("HL7 CCD Document", doc.getFormat());
		assertEquals("129.6.58.92.147", doc.getIdentifier().getDocumentUniqueId());
	}

	@Test
	@Ignore
	public void testCreateDocumentRetrieveRequest() throws JAXBException, 
	SAMLException, SOAPException, JWTValidationException, IOException, ConfigurationException, DOMException, MarshallingException, WSSecurityException {
		List<Document> docs = new ArrayList<Document>();
		Document doc = new Document();
		DocumentIdentifier docId = new DocumentIdentifier();
		docId.setHomeCommunityId("1.1.1.1");
		docId.setRepositoryUniqueId("2.2.2.2.2");
		docId.setDocumentUniqueId("3.3.3.3.3");
		doc.setIdentifier(docId);
		docs.add(doc);

		EndpointDTO endpoint = new EndpointDTO();
		endpoint.setUrl("http://someihe/endpointTransaction");

		RetrieveDocumentSetRequestType request = service.convertToRetrieveDocumentSetRequest(docs);
		String requestXml = ehealthService.marshallDocumentSetRequest(endpoint, getAssertion(), request);
		Assert.notNull(requestXml);
		System.out.println(requestXml);
	}

	@Test
	@Ignore
	public void testParseErrorResponse() throws SAMLException, SOAPException, JAXBException, IOException {
		Resource errFile = resourceLoader.getResource("classpath:NHINErrorResponse.xml");
		String errResponseStr = Resources.toString(errFile.getURL(), Charsets.UTF_8);
		AdhocQueryResponse resultObj = ehealthService.unmarshallErrorQueryResponse(errResponseStr);
		assertNotNull(resultObj);
		String namespacedStatus = resultObj.getStatus();
		int lastColonIndex = namespacedStatus.lastIndexOf(':');
		if(lastColonIndex < 0) {
			lastColonIndex = 0;
		}
		String status = namespacedStatus.substring(lastColonIndex+1);
		assertEquals(IheStatus.Failure.name(), status);
	}
}
