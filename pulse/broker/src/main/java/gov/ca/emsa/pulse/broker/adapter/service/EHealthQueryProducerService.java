package gov.ca.emsa.pulse.broker.adapter.service;


import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;

import javax.mail.internet.MimeMultipart;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAMT201306UV02QueryByParameter;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

public interface EHealthQueryProducerService {
	public AdhocQueryResponse unmarshallErrorQueryResponse(String searchResults);
	public PRPAIN201306UV02 unMarshallPatientDiscoveryResponseObject(String xml) throws SOAPException, SAMLException, JAXBException;
	public AdhocQueryResponse unMarshallDocumentQueryResponseObject(String xml) throws SAMLException, SOAPException, JAXBException;
	public RetrieveDocumentSetResponseType unMarshallDocumentSetRetrieveResponseObject(String searchResults) throws SAMLException, JAXBException, SOAPException;
	public String marshallPatientDiscoveryRequest(EndpointDTO endpoint, String assertion, PRPAIN201305UV02 response) throws JAXBException, MarshallingException, SAMLException, SOAPException, WSSecurityException;
	public String createSOAPFault();
	public String marshallDocumentQueryRequest(EndpointDTO endpoint, String assertion, AdhocQueryRequest request) throws JAXBException, DOMException, MarshallingException, SAMLException, WSSecurityException, SOAPException;
	public String marshallDocumentSetRequest(EndpointDTO endpoint, String assertion, RetrieveDocumentSetRequestType requestObj) throws JAXBException, DOMException, MarshallingException, SAMLException, WSSecurityException, SOAPException;
	public PRPAIN201305UV02 unMarshallPatientDiscoveryRequestObject(String xml) throws SOAPException, SAMLException;
	public AdhocQueryRequest unMarshallDocumentQueryRequestObject(String xml) throws SAMLException;
	public RetrieveDocumentSetRequestType unMarshallDocumentSetRetrieveRequestObject(String xml) throws SAMLException;
	public String marshallQueryByParameter(PRPAMT201306UV02QueryByParameter request) throws JAXBException;
	public WSSecHeader addSignedSecurityHeading(Document document, String samlID);
}