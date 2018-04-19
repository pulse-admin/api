package gov.ca.emsa.pulse.broker.adapter;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.DocumentQueryResults;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordResults;
import gov.ca.emsa.pulse.common.domain.PatientSearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.List;

import javax.mail.MessagingException;
import javax.xml.soap.SOAPException;

import org.opensaml.common.SAMLException;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.DOMException;

public interface Adapter {
	public PatientRecordResults queryPatients(CommonUser user, EndpointDTO endpoint, PatientSearch toSearch, String assertion) throws Exception;
	public DocumentQueryResults queryDocuments(CommonUser user, EndpointDTO endpoint, PatientEndpointMapDTO patientLocationMap, String assertion) throws UnknownHostException, UnsupportedEncodingException, DOMException, MarshallingException, SAMLException, SOAPException;
	public void retrieveDocumentsContents(CommonUser user, EndpointDTO endpoint, List<DocumentDTO> documents, PatientEndpointMapDTO patientLocationMap, String assertion) 
			throws UnknownHostException, UnsupportedEncodingException, IheErrorException, MessagingException, IOException, Exception;
}
