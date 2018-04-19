package gov.ca.emsa.pulse.broker.manager;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;

import java.sql.SQLException;
import java.util.List;

public interface DocumentManager {
	public DocumentDTO create(DocumentDTO toCreate);
	public DocumentDTO update(DocumentDTO toUpdate);
	public void queryForDocuments(CommonUser user, String assertion,  PatientEndpointMapDTO dto);
	public void queryForDocumentContents(String assertion, CommonUser user,  
			EndpointDTO endpoint, DocumentDTO document, PatientEndpointMapDTO dto);
	public DocumentDTO cancelDocumentContentQuery(Long documentId, Long patientId);
	public List<DocumentDTO> getDocumentsForPatient(Long patientId);
	public DocumentDTO getDocumentById(CommonUser user, String assertion,  Long documentId) throws SQLException;
	public DocumentDTO getDocumentById(Long documentId) throws SQLException;
}
