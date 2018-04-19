package gov.ca.emsa.pulse.broker.manager.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.adapter.AdapterFactory;
import gov.ca.emsa.pulse.broker.dao.DocumentDAO;
import gov.ca.emsa.pulse.broker.dao.EndpointDAO;
import gov.ca.emsa.pulse.broker.dao.PatientDAO;
import gov.ca.emsa.pulse.broker.domain.EndpointStatusEnum;
import gov.ca.emsa.pulse.broker.domain.EndpointTypeEnum;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.util.QueryableEndpointStatusUtil;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import gov.ca.emsa.pulse.common.domain.QueryStatus;

@Service
public class DocumentManagerImpl implements DocumentManager {
	private static final Logger logger = LogManager.getLogger(DocumentManagerImpl.class);

	@Autowired private PatientManager patientManager;
	@Autowired private AlternateCareFacilityManager acfManager;
	@Autowired private DocumentDAO docDao;
	@Autowired private PatientDAO patientDao;
	@Autowired private EndpointDAO endpointDao;
	@Autowired private AdapterFactory adapterFactory;
	@Autowired private QueryableEndpointStatusUtil endpointStatusesForQuery;

	private final ExecutorService pool;

	public DocumentManagerImpl() {
		pool = Executors.newFixedThreadPool(100);
	}
	
	@Override
	@Transactional
	public DocumentDTO create(DocumentDTO toCreate) {
		return docDao.create(toCreate);
	}
	
	@Override
	@Transactional
	public DocumentDTO update(DocumentDTO toCreate) {
		return docDao.update(toCreate);
	}
	
	@Override
	public void queryForDocuments(CommonUser user, String assertion, PatientEndpointMapDTO dto) {
		DocumentQueryService service = getDocumentQueryService();
		service.setAssertion(assertion);
		service.setPatientEndpointMap(dto);
		service.setEndpoint(dto.getEndpoint());
		service.setUser(user);
		pool.execute(service);
	}
	
	@Override
	public List<DocumentDTO> getDocumentsForPatient(Long patientId) {
		List<QueryEndpointStatus> openStatuses = new ArrayList<QueryEndpointStatus>();
		openStatuses.add(QueryEndpointStatus.Active);
		openStatuses.add(QueryEndpointStatus.Successful);
		openStatuses.add(QueryEndpointStatus.Failed);
		openStatuses.add(QueryEndpointStatus.Cancelled);

		return docDao.getDocumentsWithStatusForPatient(patientId, openStatuses);
	}
	
	@Override
	@Transactional
	public void queryForDocumentContents(String assertion, CommonUser user, EndpointDTO endpoint, DocumentDTO document, PatientEndpointMapDTO patientEndpointMap) {
		DocumentRetrievalService service = getDocumentRetrievalService();
		service.setEndpoint(endpoint);
		service.setAssertion(assertion);
		service.setPatientEndpointMap(patientEndpointMap);
		service.setDocument(document);
		service.setUser(user);
		pool.execute(service);
	}
	
	@Override
	@Transactional
	public synchronized DocumentDTO cancelDocumentContentQuery(Long documentId, Long patientId) {
		DocumentDTO documentToCancel = docDao.getById(documentId);
		documentToCancel.setStatus(QueryEndpointStatus.Cancelled);
		docDao.update(documentToCancel);
		
		return docDao.getById(documentId);
	}
	
	@Override
	@Transactional
	public DocumentDTO getDocumentById(CommonUser user, String assertion, Long documentId) throws SQLException {		
		DocumentDTO resultDoc = docDao.getById(documentId);
		if(resultDoc == null) {
			logger.error("Could not find the document with id " + documentId);
			return null;
		}
		
		//update patient last read time when document is cached or viewed
		PatientEndpointMapDTO patientEndpointMap = patientDao.getPatientEndpointMapById(resultDoc.getPatientEndpointMapId());
		PatientDTO patient = patientDao.getById(patientEndpointMap.getPatientId());
		patient.setLastReadDate(new Date());
		patientManager.update(patient);
		if(patient.getAcf() != null) {
			patient.getAcf().setLastReadDate(new Date());
			acfManager.updateLastModifiedDate(patient.getAcf().getId());
		}
		
		if(StringUtils.isEmpty(resultDoc.getContents())) {
			EndpointDTO documentContentsEndpoint = null;
			EndpointDTO documentDiscoveryEndpoint = endpointDao.findById(patientEndpointMap.getEndpointId());
			if(documentDiscoveryEndpoint != null && documentDiscoveryEndpoint.getManagingOrganization() != null) {
				String managingOrganizationName = documentDiscoveryEndpoint.getManagingOrganization();
				documentContentsEndpoint = endpointDao.findByManagingOrganizationAndType(managingOrganizationName, endpointStatusesForQuery.getStatuses(), EndpointTypeEnum.DOCUMENT_RETRIEVE);
			}else{
				String organizationId = documentDiscoveryEndpoint.getOrganizationId();
				documentContentsEndpoint = endpointDao.findByOrganizationIdAndType(organizationId, endpointStatusesForQuery.getStatuses(), EndpointTypeEnum.DOCUMENT_RETRIEVE);
			}
			
			if(documentContentsEndpoint != null) {
				List<DocumentDTO> docsToGet = new ArrayList<DocumentDTO>();
				if(resultDoc.getStatus() != null && 
					(resultDoc.getStatus() == QueryEndpointStatus.Cancelled ||
					resultDoc.getStatus() == QueryEndpointStatus.Failed)) {
					
					resultDoc.setStatus(QueryEndpointStatus.Closed);
					docDao.update(resultDoc);
					
					DocumentDTO newDocRequest = new DocumentDTO(resultDoc);
					newDocRequest.setStatus(QueryEndpointStatus.Active);
					resultDoc = docDao.create(newDocRequest);
					docsToGet.add(resultDoc);
				} else {
					docsToGet.add(resultDoc);
				}
				
				queryForDocumentContents(assertion, user, documentContentsEndpoint, resultDoc, patientEndpointMap);
			}
		}
		return resultDoc;
	}
	
	@Override
	public DocumentDTO getDocumentById(Long documentId) throws SQLException{
		return docDao.getById(documentId);
	}
	
	@Lookup
	public DocumentQueryService getDocumentQueryService(){
		//spring will override this method
		return null;
	}
	
	@Lookup
	public DocumentRetrievalService getDocumentRetrievalService() {
		//spring will override this method
		return null;
	}
}
