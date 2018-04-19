package gov.ca.emsa.pulse.broker.manager;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.QueryDTO;
import gov.ca.emsa.pulse.broker.dto.QueryEndpointMapDTO;
import gov.ca.emsa.pulse.common.domain.PatientSearch;

public interface QueryManager extends CachedDataManager {
	public QueryDTO getById(Long id);
	public QueryEndpointMapDTO getQueryEndpointMapById(Long id);
	public List<QueryEndpointMapDTO> getQueryEndpointMapByQueryAndEndpoint(Long queryId, Long endpointId);
	public List<QueryDTO> getAllQueriesForUser(String userKey);
	public List<QueryDTO> getOpenQueriesForUser(String userKey);
	public String getQueryStatus(Long queryId);
	public QueryDTO updateQuery(QueryDTO toUpdate);
	public QueryDTO createQuery(QueryDTO toCreate);
	public void close(Long queryId);
	public QueryDTO cancelQueryToEndpoint(Long queryId, Long endpointId);
	public QueryEndpointMapDTO createOrUpdateQueryEndpointMap(QueryEndpointMapDTO toUpdate);

	public PatientRecordDTO getPatientRecordById(Long patientRecordId);
	public void queryForPatientRecords(String assertion, PatientSearch toSearch, QueryDTO query, CommonUser user) throws JsonProcessingException;
	public Long requeryForPatientRecords(String assertion, Long queryId, Long endpointId, CommonUser user) throws JsonProcessingException, IOException;
	public PatientRecordDTO addPatientRecord(PatientRecordDTO record);
	public void removePatientRecord(Long prId);
	public QueryDTO updateQueryStatusFromEndpoints(Long queryId);
}
