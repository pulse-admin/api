package gov.ca.emsa.pulse.broker.adapter.service;

import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.saml.SamlUtil;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.common.token.SecurityTokenReference;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.common.*;
import org.apache.wss4j.dom.*;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.wss4j.dom.message.WSSecTimestamp;
import org.apache.wss4j.dom.saml.WSSecSignatureSAML;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.apache.commons.io.output.XmlStreamWriter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAMT201306UV02QueryByParameter;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.AssertionImpl;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class EHealthQueryProducerServiceImpl implements EHealthQueryProducerService{

	private static final Logger logger = LogManager.getLogger(EHealthQueryProducerServiceImpl.class);
	private Crypto crypto;
	
	@Value("${soapKeystorePass}")
	private String keystorepass;
	
	@Value("${soapKeystoreAlias}")
	private String keystorealias;
	
	@Value("${timeToLiveTimestamp}")
	private String ttl;
	
	class BinaryDataSource implements DataSource {
	    InputStream _is;

	    public BinaryDataSource(InputStream is) {
	        _is = is;
	    }
	    public String getContentType() { return "application/binary"; }
	    public InputStream getInputStream() throws IOException { return _is; }
	    public String getName() { return "some file"; }
	    public OutputStream getOutputStream() throws IOException {
	        throw new IOException("Cannot write to this file");
	    }
	}
	
	public WSSecHeader addSignedSecurityHeading(Document document, String samlID){
		WSSConfig.init();
		try {
			crypto = CryptoFactory.getInstance();
		} catch (WSSecurityException e1) {
			e1.printStackTrace();
		}
		WSSecSignature builder = new WSSecSignature();
		SecurityTokenReference str = new SecurityTokenReference(document);
		str.addTokenType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
		try {
			str.setKeyIdentifier("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID", samlID);
		} catch (WSSecurityException e1) {
			e1.printStackTrace();
		}
		builder.setSecurityTokenReference(str);
		builder.setUserInfo(keystorealias, keystorepass);
		
		WSSecHeader secHeader = new WSSecHeader(document);
		try {
			secHeader.insertSecurityHeader();
		} catch (WSSecurityException e2) {
			e2.printStackTrace();
		}
		
		WSSecTimestamp timestamp = new WSSecTimestamp();
		timestamp.setTimeToLive(Integer.parseInt(ttl));
		Document createdDoc = timestamp.build(document, secHeader);
		
		WSEncryptionPart encP =
				new WSEncryptionPart(
						"Timestamp",
						WSConstants.WSU_NS,
						"");
		builder.getParts().add(encP);
		
		try {
			Document signedDoc = builder.build(createdDoc, crypto, secHeader);
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Element secHeaderElement = secHeader.getSecurityHeader();
        Node timestampNode =
            secHeaderElement.getElementsByTagNameNS(WSConstants.WSU_NS, "Timestamp").item(0);
        ((Element)timestampNode).setAttributeNS(
            WSConstants.XMLNS_NS, "xmlns", WSConstants.WSU_NS
        );
        
        return secHeader;
	}
	
	private String createUUID(){
		return UUID.randomUUID().toString();
	}


	public String createSOAPFault(){
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage();
			soapMessage.getSOAPBody().addFault(SOAPConstants.SOAP_SENDER_FAULT, "There was no SAML security header in the SOAP message.");
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			soapMessage.writeTo(outputStream);
		} catch (SOAPException | IOException e) {
			
		}
		String fault = new String(outputStream.toByteArray());

		return fault;
	}
	
	public SOAPMessage addSemanticTextValues(SOAPMessage soapMessage) throws SOAPException{
		NodeList nodeList = soapMessage.getSOAPBody().getFirstChild().getLastChild().getChildNodes().item(1).getLastChild().getChildNodes();
		for(int i=0;i<nodeList.getLength();i++){
			Node node = nodeList.item(i);
			NodeList nodeList2 = node.getChildNodes();
			for(int j=0;j<nodeList2.getLength();j++){
				Node node2 = nodeList2.item(j);
				if(node2.getNodeName().equals("semanticsText")){
					switch(node2.getParentNode().getNodeName()){
					case "livingSubjectAdministrativeGender":
						node2.setTextContent("LivingSubject.administrativeGender");
						break;
					case "livingSubjectBirthTime":
						node2.setTextContent("LivingSubject.birthTime");
						break;
					case "livingSubjectId":
						node2.setTextContent("LivingSubject.id");
						break;
					case "livingSubjectName":
						node2.setTextContent("LivingSubject.name");
						break;
					case "patientAddress":
						node2.setTextContent("Patient.addr");
						break;
					case "patientTelecom":
						node2.setTextContent("Patient.telecom");
						break;
					}
				}
			}
		}
		return soapMessage;
	}

	private WSSecHeader createSecurityHeadingPatientDiscovery(EndpointDTO endpoint, SOAPMessage message, Document assertion) throws SOAPException, MarshallingException, SAMLException, WSSecurityException {
		SOAPEnvelope env = message.getSOAPPart().getEnvelope();
		
		SOAPHeaderElement header1 = message.getSOAPHeader()
				.addHeaderElement(env.createName("Action", "a", "http://www.w3.org/2005/08/addressing"));
		header1.setAttributeNS("http://www.w3.org/2003/05/soap-envelope", "env:mustUnderstand", "1");
		header1.setValue("urn:hl7-org:v3:PRPA_IN201305UV02:CrossGatewayPatientDiscovery");
		message.getSOAPHeader().addChildElement(header1);

		SOAPHeaderElement header2 = message.getSOAPHeader()
				.addHeaderElement(env.createName("MessageID", "a", "http://www.w3.org/2005/08/addressing"));
		header2.setValue(createUUID());
		message.getSOAPHeader().addChildElement(header2);
		
		SOAPHeaderElement header3 = message.getSOAPHeader()
				.addHeaderElement(env.createName("To", "a", "http://www.w3.org/2005/08/addressing"));
		header3.setAttributeNS("http://www.w3.org/2003/05/soap-envelope", "env:mustUnderstand", "1");
		header3.setValue(endpoint.getUrl());
		message.getSOAPHeader().addChildElement(header3);
		
		Document owner = header3.getOwnerDocument();
		Document assertionDoc = assertion;
		WSSecHeader secHeader = addSignedSecurityHeading(owner, assertionDoc.getFirstChild().getAttributes().getNamedItem("ID").getNodeValue());
		Node importedSamlElement = owner.importNode(assertionDoc.getFirstChild(), true);
		Element securityElement = WSSecurityUtil.findWsseSecurityHeaderBlock(owner, owner.getDocumentElement(), false);
		securityElement.appendChild(importedSamlElement);
		
		return secHeader;
	}
	
	private WSSecHeader createSecurityHeadingDocumentQuery(EndpointDTO endpoint, SOAPMessage message, Document assertion) throws SOAPException, DOMException, MarshallingException, SAMLException, WSSecurityException {
		SOAPEnvelope env = message.getSOAPPart().getEnvelope();
		
		SOAPHeaderElement header1 = message.getSOAPHeader()
				.addHeaderElement(env.createName("Action", "a", "http://www.w3.org/2005/08/addressing"));
		header1.setAttributeNS("http://www.w3.org/2003/05/soap-envelope", "env:mustUnderstand", "1");
		header1.setValue("urn:ihe:iti:2007:CrossGatewayQuery");
		message.getSOAPHeader().addChildElement(header1);

		SOAPHeaderElement header2 = message.getSOAPHeader()
				.addHeaderElement(env.createName("MessageID", "a", "http://www.w3.org/2005/08/addressing"));
		header2.setValue(createUUID());
		message.getSOAPHeader().addChildElement(header2);
		
		SOAPHeaderElement header3 = message.getSOAPHeader()
				.addHeaderElement(env.createName("To", "a", "http://www.w3.org/2005/08/addressing"));
		header3.setAttributeNS("http://www.w3.org/2003/05/soap-envelope", "env:mustUnderstand", "1");
		header3.setValue(endpoint.getUrl());
		message.getSOAPHeader().addChildElement(header3);
		
		Document owner = header3.getOwnerDocument();
		Document assertionDoc = assertion;
		WSSecHeader secHeader = addSignedSecurityHeading(owner, assertionDoc.getFirstChild().getAttributes().getNamedItem("ID").getNodeValue());
		Node importedSamlElement = owner.importNode(assertionDoc.getFirstChild(), true);
		Element securityElement = WSSecurityUtil.findWsseSecurityHeaderBlock(owner, owner.getDocumentElement(), false);
		securityElement.appendChild(importedSamlElement);
		
		return secHeader;
	}
	
	private WSSecHeader createSecurityHeadingDocumentRetrieve(EndpointDTO endpoint, SOAPMessage message, Document assertion) throws SOAPException, DOMException, MarshallingException, SAMLException, WSSecurityException {
		SOAPEnvelope env = message.getSOAPPart().getEnvelope();
		SOAPHeaderElement header1 = message.getSOAPHeader()
				.addHeaderElement(env.createName("Action", "a", "http://www.w3.org/2005/08/addressing"));
		header1.setAttributeNS("http://www.w3.org/2003/05/soap-envelope", "env:mustUnderstand", "1");
		header1.setValue("urn:ihe:iti:2007:CrossGatewayRetrieve");
		message.getSOAPHeader().addChildElement(header1);

		SOAPHeaderElement header2 = message.getSOAPHeader()
				.addHeaderElement(env.createName("MessageID", "a", "http://www.w3.org/2005/08/addressing"));
		header2.setValue(createUUID());
		message.getSOAPHeader().addChildElement(header2);
		
		SOAPHeaderElement header3 = message.getSOAPHeader()
				.addHeaderElement(env.createName("To", "a", "http://www.w3.org/2005/08/addressing"));
		header3.setAttributeNS("http://www.w3.org/2003/05/soap-envelope", "env:mustUnderstand", "1");
		header3.setValue(endpoint.getUrl());
		message.getSOAPHeader().addChildElement(header3);
		
		Document owner = header3.getOwnerDocument();
		Document assertionDoc = assertion;
		WSSecHeader secHeader = addSignedSecurityHeading(owner, assertionDoc.getFirstChild().getAttributes().getNamedItem("ID").getNodeValue());
		Node importedSamlElement = owner.importNode(assertionDoc.getFirstChild(), true);
		Element securityElement = WSSecurityUtil.findWsseSecurityHeaderBlock(owner, owner.getDocumentElement(), false);
		securityElement.appendChild(importedSamlElement);
		
		return secHeader;
	}
	
	public boolean checkSecurityHeading(SaajSoapMessage saajSoap){
		Iterator<SoapHeaderElement> security = saajSoap.getSoapHeader().examineAllHeaderElements();
		while(security.hasNext()){
			SoapHeaderElement headerElem = security.next();
			if(headerElem.getName().getLocalPart().equals("Security")){
				SoapHeaderElement wsse = headerElem;
				return true;
			}
		}
		return false;
	}
	
	public String marshallPatientDiscoveryRequest(EndpointDTO endpoint, String assertion, PRPAIN201305UV02 request) throws JAXBException, MarshallingException, SAMLException, SOAPException, WSSecurityException{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage();
		} catch (SOAPException e) {
			logger.error(e);
		}
		Document assertionDoc = unMarshallAssertionObject(assertion);
		WSSecHeader secHeader = null;
		try {
			secHeader = createSecurityHeadingPatientDiscovery(endpoint, soapMessage, assertionDoc);
		} catch(SOAPException soap) {
			logger.error(soap);
		}
		
		JAXBElement<PRPAIN201305UV02> je = new JAXBElement<PRPAIN201305UV02>(new QName("urn:hl7-org:v3","PRPA_IN201305UV02"), PRPAIN201305UV02.class, request);
		Document document = null;
		
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	        builderFactory.setNamespaceAware(true);
	        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
	        document = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Marshaller documentMarshaller = createMarshaller(createJAXBContext(request.getClass()));
		documentMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		documentMarshaller.marshal(je, document);
		try {
			soapMessage.getSOAPBody().addDocument(document);
		} catch (SOAPException e1) {
			e1.printStackTrace();
		}
		soapMessage = addSemanticTextValues(soapMessage);
		
		WSSecSignature wsSign = new WSSecSignature();
        wsSign.setUserInfo(keystorealias, keystorepass);
        wsSign.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);
        
        Document doc = soapMessage.getSOAPPart().getEnvelope().getOwnerDocument();
        
		Document signedDoc =  wsSign.build(doc, crypto, secHeader);
		String signedStringDoc = null;
		try {
			signedStringDoc = XMLUtils.prettyDocumentToString(signedDoc);
		} catch (IOException | TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return signedStringDoc;
	}
	
	public String marshallQueryByParameter(PRPAMT201306UV02QueryByParameter request) throws JAXBException{

		JAXBElement<PRPAMT201306UV02QueryByParameter> je = new JAXBElement<PRPAMT201306UV02QueryByParameter>(new QName("PRPAMT201306UV02QueryByParameter"), PRPAMT201306UV02QueryByParameter.class, request);
		Marshaller documentMarshaller = createMarshaller(createJAXBContext(PRPAMT201306UV02QueryByParameter.class));
		StringWriter sw = new StringWriter();
		documentMarshaller.marshal(je, sw);
		return sw.toString();
		
	}
	
	public String marshallDocumentQueryRequest(EndpointDTO endpoint, String assertion, AdhocQueryRequest request) throws JAXBException, DOMException, MarshallingException, SAMLException, WSSecurityException, SOAPException{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage();
		} catch (SOAPException e) {
			logger.error(e);
		}
		Document assertionDoc = unMarshallAssertionObject(assertion);
		WSSecHeader secHeader = null;
		try {
			secHeader = createSecurityHeadingDocumentQuery(endpoint, soapMessage, assertionDoc);
		} catch(SOAPException soap) {
			logger.error(soap);
		}
		
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Marshaller documentMarshaller = createMarshaller(createJAXBContext(request.getClass()));
		documentMarshaller.marshal(request, document);
		try {
			soapMessage.getSOAPBody().addDocument(document);
		} catch (SOAPException e1) {
			e1.printStackTrace();
		}
		WSSecSignature wsSign = new WSSecSignature();
        wsSign.setUserInfo(keystorealias, keystorepass);
        wsSign.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);
        
        Document doc = soapMessage.getSOAPPart().getEnvelope().getOwnerDocument();
        
		Document signedDoc =  wsSign.build(doc, crypto, secHeader);
		String signedStringDoc = null;
		try {
			signedStringDoc = XMLUtils.prettyDocumentToString(signedDoc);
		} catch (IOException | TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return signedStringDoc;
	}
	
	public String marshallDocumentSetRequest(EndpointDTO endpoint, String assertion, RetrieveDocumentSetRequestType request) throws JAXBException, DOMException, MarshallingException, SAMLException, WSSecurityException, SOAPException{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage();
		} catch (SOAPException e) {
			logger.error(e);
		}
		Document assertionDoc = unMarshallAssertionObject(assertion);
		WSSecHeader secHeader = null;
		try {
			secHeader = createSecurityHeadingDocumentRetrieve(endpoint, soapMessage, assertionDoc);
		} catch(SOAPException soap) {
			logger.error(soap);
		}
		
		JAXBElement<RetrieveDocumentSetRequestType> je = new JAXBElement<RetrieveDocumentSetRequestType>(new QName("urn:ihe:iti:xds-b:2007", "RetrieveDocumentSetRequest"), RetrieveDocumentSetRequestType.class, request);
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Marshaller documentMarshaller = createMarshaller(createJAXBContext(request.getClass()));
		documentMarshaller.marshal(je, document);
		try {
			soapMessage.getSOAPBody().addDocument(document);
		} catch (SOAPException e1) {
			e1.printStackTrace();
		}
		WSSecSignature wsSign = new WSSecSignature();
        wsSign.setUserInfo(keystorealias, keystorepass);
        wsSign.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);
        
        Document doc = soapMessage.getSOAPPart().getEnvelope().getOwnerDocument();
        
		Document signedDoc =  wsSign.build(doc, crypto, secHeader);
		String signedStringDoc = null;
		try {
			signedStringDoc = XMLUtils.prettyDocumentToString(signedDoc);
		} catch (IOException | TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return signedStringDoc;
	}
	
	public Marshaller createMarshaller(JAXBContext jc){
		// Create JAXB marshaller
		Marshaller marshaller = null;
		try {
			marshaller = jc.createMarshaller();
		}
		catch (Exception ex) {
			logger.error(ex);
		}
		return marshaller;
	}

	public JAXBContext createJAXBContext(Class<?> classObj){
		// Create a JAXB context
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(classObj);
		}
		catch (Exception ex) {
			logger.error(ex);
		}
		return jc;
	}

	public Unmarshaller createUnmarshaller(JAXBContext jc){
		// Create JAXB unmarshaller
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		}
		catch (Exception ex) {
			logger.error(ex);
		}
		return unmarshaller;
	}

	public AdhocQueryResponse unmarshallErrorQueryResponse(String xml) {
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);
		Source responseSource = saajSoap.getSoapBody().getPayloadSource();
		
		JAXBContext jc = createJAXBContext(AdhocQueryResponse.class);
		Unmarshaller unmarshaller = createUnmarshaller(jc);
		JAXBElement<?> responseObj = null;
		try {
			responseObj = (JAXBElement<?>) unmarshaller.unmarshal(responseSource, AdhocQueryResponse.class);
		} catch(Exception ex) {
			logger.error("Could not unmarshal response as AdHocQueryResponse error message.", ex);
		}	
		if(responseObj == null) {
			return null;
		}
		return (AdhocQueryResponse) responseObj.getValue();
	}
	
	public AdhocQueryResponse unmarshallErrorQueryResponseDocumentSet(MimeMultipart xml) {
		
		JAXBContext jc = createJAXBContext(AdhocQueryResponse.class);
		Unmarshaller unmarshaller = createUnmarshaller(jc);
		JAXBElement<?> responseObj = null;
		try {
			responseObj = (JAXBElement<?>) unmarshaller.unmarshal(new StreamSource(xml.getBodyPart(0).getInputStream()), AdhocQueryResponse.class);
		} catch(Exception ex) {
			logger.error("Could not unmarshal response as AdHocQueryResponse error message.", ex);
		}	
		if(responseObj == null) {
			return null;
		}
		return (AdhocQueryResponse) responseObj.getValue();
	}
	
	public PRPAIN201306UV02 unMarshallPatientDiscoveryResponseObject(String xml) 
			throws SOAPException, SAMLException, JAXBException {
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);
		Source requestSource = saajSoap.getSoapBody().getPayloadSource();

		//first try to unmarshal as the object
		JAXBContext jc = createJAXBContext(PRPAIN201306UV02.class);
		Unmarshaller unmarshaller = createUnmarshaller(jc);
		JAXBElement<?> requestObj = null;
		
		try {
			requestObj = (JAXBElement<?>) unmarshaller.unmarshal(requestSource, PRPAIN201306UV02.class);
		} catch (JAXBException e) {
			logger.error("Could not unmarshal response as PRPAIN201306UV02", e);
			throw e;
		}

		if(requestObj == null) {
			return null;
		}
		return (PRPAIN201306UV02) requestObj.getValue();
	}

	public AdhocQueryResponse unMarshallDocumentQueryResponseObject(String xml) 
			throws SAMLException,SOAPException, JAXBException 
	{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);
		Source requestSource = saajSoap.getSoapBody().getPayloadSource();

		// Create a JAXB context
		JAXBContext jc = createJAXBContext(AdhocQueryResponse.class);

		// Create JAXB unmarshaller
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		}
		catch (Exception ex) {
			logger.error(ex);
		}
		JAXBElement<?> requestObj = null;
		try {
			requestObj = (JAXBElement<?>) unmarshaller.unmarshal(requestSource, AdhocQueryResponse.class);
		} catch (JAXBException e) {
			logger.error(e);
			throw e;
		}
		
		if(requestObj == null) {
			return null;
		}
		return (AdhocQueryResponse) requestObj.getValue();
	}

	public RetrieveDocumentSetResponseType unMarshallDocumentSetRetrieveResponseObject(String xml) 
			throws SAMLException, JAXBException, SOAPException {
    	
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);
		Source requestSource = saajSoap.getSoapBody().getPayloadSource();
		
		// Create a JAXB context
		JAXBContext jc = createJAXBContext(RetrieveDocumentSetResponseType.class);
		
		// Create JAXB unmarshaller
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		}
		catch (Exception ex) {
			logger.error(ex);
		}
		JAXBElement<?> requestObj = null;
		try {
			requestObj = (JAXBElement<?>) unmarshaller.unmarshal(requestSource, RetrieveDocumentSetResponseType.class);
		} catch (JAXBException e) {
			logger.error(e);
		}

		if(requestObj == null) {
			return null;
		}
		return (RetrieveDocumentSetResponseType) requestObj.getValue();
	}

	public PRPAIN201305UV02 unMarshallPatientDiscoveryRequestObject(String xml) throws SOAPException, SAMLException{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);

		if(checkSecurityHeading(saajSoap)){

			Source requestSource = saajSoap.getSoapBody().getPayloadSource();

			// Create a JAXB context
			JAXBContext jc = createJAXBContext(PRPAIN201305UV02.class);

			// Create JAXB unmarshaller
			Unmarshaller unmarshaller = createUnmarshaller(jc);

			JAXBElement<?> requestObj = null;
			try {
				requestObj = (JAXBElement<?>) unmarshaller.unmarshal(requestSource, PRPAIN201305UV02.class);
			} catch (JAXBException e) {
				logger.error(e);
			}

			return (PRPAIN201305UV02) requestObj.getValue();
		}else{
			logger.error("SOAP message does not have a SAML header");
			throw new SAMLException();
		}
	}

	public AdhocQueryRequest unMarshallDocumentQueryRequestObject(String xml) throws SAMLException{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);

		if(checkSecurityHeading(saajSoap)){

			Source requestSource = saajSoap.getSoapBody().getPayloadSource();

			// Create a JAXB context
			JAXBContext jc = null;
			try {
				jc = JAXBContext.newInstance(AdhocQueryRequest.class);
			}
			catch (Exception ex) {
				logger.error(ex);
			}

			// Create JAXB unmarshaller
			Unmarshaller unmarshaller = null;
			try {
				unmarshaller = jc.createUnmarshaller();
			}
			catch (Exception ex) {
				logger.error(ex);
			}
			JAXBElement<?> requestObj = null;
			try {
				requestObj = (JAXBElement<?>) unmarshaller.unmarshal(requestSource, AdhocQueryRequest.class);
			} catch (JAXBException e) {
				logger.error(e);
			}
			return (AdhocQueryRequest) requestObj.getValue();
		}else{
			logger.error("SOAP message does not have a SAML header");
			throw new SAMLException();
		}
	}
	
	public Document unMarshallAssertionObject(String xml) throws SAMLException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document document = null;
		try  
		{  
			builder = factory.newDocumentBuilder();  
			document = builder.parse(new InputSource(new StringReader(xml)));  
		} catch (Exception e) {  
			e.printStackTrace();  
		} 

		return document;
	}

	public RetrieveDocumentSetRequestType unMarshallDocumentSetRetrieveRequestObject(String xml) throws SAMLException{
		MessageFactory factory = null;
		try {
			factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e1) {
			logger.error(e1);
		}
		SOAPMessage soapMessage = null;
		try {
			soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | SOAPException e) {
			logger.error(e);
		}
		SaajSoapMessage saajSoap = new SaajSoapMessage(soapMessage);

		if(checkSecurityHeading(saajSoap)){

			Source requestSource = saajSoap.getSoapBody().getPayloadSource();

			// Create a JAXB context
			JAXBContext jc = null;
			try {
				jc = JAXBContext.newInstance(RetrieveDocumentSetRequestType.class);
			}
			catch (Exception ex) {
				logger.error(ex);
			}

			// Create JAXB unmarshaller
			Unmarshaller unmarshaller = null;
			try {
				unmarshaller = jc.createUnmarshaller();
			}
			catch (Exception ex) {
				logger.error(ex);
			}
			JAXBElement<?> requestObj = null;
			try {
				requestObj = (JAXBElement<?>) unmarshaller.unmarshal(requestSource, RetrieveDocumentSetRequestType.class);
			} catch (JAXBException e) {
				logger.error(e);
			}

			return (RetrieveDocumentSetRequestType) requestObj.getValue();

		}else{
			logger.error("SOAP message does not have a SAML header");
			throw new SAMLException();
		}
	}
}
