package gov.ca.emsa.pulse.broker.manager;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.saml.SAMLInput;

import java.sql.SQLException;
import java.util.List;

public interface DocumentManager {
	public DocumentDTO create(DocumentDTO toCreate);
	public void queryForDocuments(CommonUser user, SAMLInput samlMessage, PatientEndpointMapDTO dto);
	public void queryForDocumentContents(CommonUser user, SAMLInput samlInput, 
			EndpointDTO endpoint, List<DocumentDTO> docsFromEndpoints, PatientEndpointMapDTO dto);
	public List<DocumentDTO> getDocumentsForPatient(Long patientId);
	public String getDocumentById(CommonUser user, SAMLInput samlInput, Long documentId) throws SQLException;
	DocumentDTO getDocumentObjById(Long documentId) throws SQLException;
}
