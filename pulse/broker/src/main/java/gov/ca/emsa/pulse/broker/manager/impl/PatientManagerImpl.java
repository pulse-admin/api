package gov.ca.emsa.pulse.broker.manager.impl;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.adapter.EHealthAdapter;
import gov.ca.emsa.pulse.broker.cache.CacheCleanupException;
import gov.ca.emsa.pulse.broker.dao.EndpointDAO;
import gov.ca.emsa.pulse.broker.dao.PatientDAO;
import gov.ca.emsa.pulse.broker.dao.QueryDAO;
import gov.ca.emsa.pulse.broker.domain.EndpointTypeEnum;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.PulseUserDTO;
import gov.ca.emsa.pulse.broker.dto.QueryEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.manager.QueryManager;
import gov.ca.emsa.pulse.broker.saml.SamlUtil;
import gov.ca.emsa.pulse.broker.util.QueryableEndpointStatusUtil;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import gov.ca.emsa.pulse.service.UserUtil;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientManagerImpl implements PatientManager {
	private static final Logger logger = LogManager.getLogger(PatientManagerImpl.class);

	@Autowired private PatientDAO patientDao;
	@Autowired private EndpointDAO endpointDao;
	@Autowired private QueryManager queryManager;
	@Autowired private AuditEventManager auditManager;
	@Autowired private AlternateCareFacilityManager acfManager;
	@Autowired private DocumentManager docManager;
	@Autowired QueryableEndpointStatusUtil endpointStatusesForQuery;
	@Autowired private QueryDAO queryDao;
	
	public PatientManagerImpl() {
	}
	
	@Override
	@Transactional	
	public PatientDTO getPatientById(Long patientId) {
		PatientDTO result = patientDao.getById(patientId);
		return result;
	}
	
	@Override
	@Transactional	
	public List<PatientDTO> getPatientsAtAcf(Long acfId) {
		List<QueryEndpointStatus> documentOpenStatuses = new ArrayList<QueryEndpointStatus>();
		documentOpenStatuses.add(QueryEndpointStatus.Active);
		documentOpenStatuses.add(QueryEndpointStatus.Successful);
		documentOpenStatuses.add(QueryEndpointStatus.Failed);
		documentOpenStatuses.add(QueryEndpointStatus.Cancelled);
		
		List<PatientDTO> results = patientDao.getPatientsAtAcf(acfId, documentOpenStatuses);
		return results;
	}
	
	@Override
	@Transactional	
	public List<PatientEndpointMapDTO> getPatientEndpointMaps(Long patientId, Long endpointId) {
		List<PatientEndpointMapDTO> results = patientDao.getPatientEndpointMaps(patientId, endpointId);
		return results;
	}
	
	@Override
	@Transactional	
	public PatientEndpointMapDTO getPatientEndpointMapById(Long id) {
		return patientDao.getPatientEndpointMapById(id);
	}
	
	@Override
	@Transactional
	public PatientDTO create(PatientDTO toCreate) throws SQLException {
		PatientDTO result = patientDao.create(toCreate);
		
		if(toCreate.getAcf() != null) {
			toCreate.getAcf().setLastReadDate(new Date());
			acfManager.updateLastModifiedDate(toCreate.getAcf().getId());
		}
		return result;
	}
	
	@Override
	@Transactional
	public PatientDTO update(PatientDTO toUpdate) throws SQLException {
		
		toUpdate.setLastReadDate(new Date());
		return patientDao.update(toUpdate);
	}
	
	@Override
	@Transactional
	public void delete(Long patientId) throws SQLException {
		patientDao.delete(patientId);
	}
	
	@Override
	@Transactional
	public void cleanupCache(Date oldestAllowedPatient) throws CacheCleanupException {
		try {
			patientDao.deleteItemsOlderThan(oldestAllowedPatient);
		} catch(SQLException sql) {
			throw new CacheCleanupException("Error cleaning up old patients in the database. Message is: " + sql.getMessage());
		} catch(Exception ex) {
			throw new CacheCleanupException(ex.getMessage());
		}
	}

	@Override
	@Transactional
	public synchronized PatientEndpointMapDTO updatePatientEndpointMap(PatientEndpointMapDTO toUpdate)
			throws SQLException{
		return patientDao.updatePatientEndpointMap(toUpdate);
	}

	@Override
	@Transactional
	public PatientEndpointMapDTO createEndpointMapForDocumentDiscovery(PatientDTO patient, Long patientRecordId)
			throws SQLException {
		PatientEndpointMapDTO result = null;
		PatientRecordDTO patientRecord = queryManager.getPatientRecordById(patientRecordId);
		
		if(patientRecord != null) {
			EndpointDTO documentDiscoveryEndpoint = null;
			PatientEndpointMapDTO patientEndpointMapToCreate = new PatientEndpointMapDTO();
			patientEndpointMapToCreate.setPatientId(patient.getId());
			patientEndpointMapToCreate.setExternalPatientRecordId(patientRecord.getEndpointPatientRecordId());
			//get the endpoint that was queried for the patient discovery
			QueryEndpointMapDTO queryEndpointMapDto = queryDao.findQueryEndpointMapById(patientRecord.getQueryEndpointId());
			Long patientDiscoveryEndpointId = queryEndpointMapDto.getEndpointId();
			//figure out which location this endpoint came from
			//we assume that any locations which share this patient discovery endpoint
			//will also have the same document query and document retrieve endpoints
			EndpointDTO patientDiscoveryEndpoint = endpointDao.findById(patientDiscoveryEndpointId);
			documentDiscoveryEndpoint = endpointDao.findByManagingOrganizationAndType(patientDiscoveryEndpoint.getManagingOrganization(), 
					endpointStatusesForQuery.getStatuses(), EndpointTypeEnum.DOCUMENT_DISCOVERY);
			
			if(documentDiscoveryEndpoint != null) {
				patientEndpointMapToCreate.setEndpointId(documentDiscoveryEndpoint.getId());	
				result = patientDao.createPatientEndpointMap(patientEndpointMapToCreate);
				result.setEndpoint(documentDiscoveryEndpoint);
			}else{
				documentDiscoveryEndpoint = endpointDao.findByOrganizationIdAndType(patientDiscoveryEndpoint.getOrganizationId(), 
						endpointStatusesForQuery.getStatuses(), EndpointTypeEnum.DOCUMENT_DISCOVERY);
				patientEndpointMapToCreate.setEndpointId(documentDiscoveryEndpoint.getId());	
				result = patientDao.createPatientEndpointMap(patientEndpointMapToCreate);
				result.setEndpoint(documentDiscoveryEndpoint);
			}
		}
		return result;
	}
	
	@Override
	@Transactional
	public synchronized PatientDTO cancelQueryForDocuments(Long patientId, Long endpointId) throws SQLException {
		EndpointDTO endpoint = endpointDao.findById(endpointId);
		String endpointUrl = null;
		if(endpoint != null) {
			endpointUrl = endpoint.getUrl();
		}
		
		List<PatientEndpointMapDTO> patientEndpointMaps = patientDao.getPatientEndpointMaps(patientId, endpointId);
		for(PatientEndpointMapDTO patientEndpointMapToCancel : patientEndpointMaps) {
			if(patientEndpointMapToCancel.getDocumentsQueryStatus() == QueryEndpointStatus.Active) {
				patientEndpointMapToCancel.setDocumentsQueryStatus(QueryEndpointStatus.Cancelled);
				patientDao.updatePatientEndpointMap(patientEndpointMapToCancel);
			}
		}

		try {
			auditManager.createAuditEventIG("CANCELLED" , UserUtil.getCurrentUser(), endpointUrl, "", EHealthAdapter.HOME_COMMUNITY_ID);
		} catch(UnsupportedEncodingException ex) {
			logger.warn("Could not add audit record for cancelling document list request to endpoint " + endpointUrl + " for patient " + patientId + ": " + ex.getMessage(), ex);
		} catch(UnknownHostException ex) {
			logger.warn("Could not add audit record for cancelling document list request to endpoint " + endpointUrl + " for patient " + patientId + ": " + ex.getMessage(), ex);
		} catch(Exception ex) {
			logger.warn("Could not add audit record for cancelleing document listrequest to endpoint " +endpointUrl + " for patient " + patientId + ": " + ex.getMessage(), ex);
		}

		return getPatientById(patientId);
	}
	
	@Override
	@Transactional
	public PatientDTO requeryForDocuments(Long patientId, Long endpointId, CommonUser user) throws SQLException {
		PatientDTO patient = patientDao.getById(patientId);
		
		//set all document list requests for this patient+endpoint to closed
		String externalPatientRecordId = "";
		List<PatientEndpointMapDTO> patientEndpointMaps = patientDao.getPatientEndpointMaps(patientId, endpointId);
		for(PatientEndpointMapDTO patientEndpointMapToCancel : patientEndpointMaps) {
			externalPatientRecordId = patientEndpointMapToCancel.getExternalPatientRecordId();
			patientEndpointMapToCancel.setDocumentsQueryStatus(QueryEndpointStatus.Closed);
			patientDao.updatePatientEndpointMap(patientEndpointMapToCancel);
		}

		//make the new request
		PatientEndpointMapDTO patientEndpointMapForRequery = new PatientEndpointMapDTO();
		patientEndpointMapForRequery.setPatientId(patientId);
		patientEndpointMapForRequery.setExternalPatientRecordId(externalPatientRecordId);
		patientEndpointMapForRequery.setEndpointId(endpointId);
		patientEndpointMapForRequery = patientDao.createPatientEndpointMap(patientEndpointMapForRequery);
		EndpointDTO endpointForRequery = endpointDao.findById(endpointId);
		patientEndpointMapForRequery.setEndpoint(endpointForRequery);

		if(patientEndpointMapForRequery != null) {
			String assertion = SamlUtil.signAndBuildStringAssertion(user);
			patient.getEndpointMaps().add(patientEndpointMapForRequery);
            docManager.queryForDocuments(user, assertion,  patientEndpointMapForRequery);
		}
		
		return getPatientById(patientId);
	}
}
