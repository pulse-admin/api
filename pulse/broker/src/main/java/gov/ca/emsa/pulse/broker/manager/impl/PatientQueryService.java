package gov.ca.emsa.pulse.broker.manager.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.adapter.Adapter;
import gov.ca.emsa.pulse.broker.adapter.AdapterFactory;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordResults;
import gov.ca.emsa.pulse.broker.dto.QueryEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.LocationManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.manager.QueryManager;
import gov.ca.emsa.pulse.common.domain.PatientSearch;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import gov.ca.emsa.pulse.cten.IheStatus;

@Component
public class PatientQueryService implements Runnable {
	

	private static final Logger logger = LogManager.getLogger(PatientQueryService.class);

	private QueryEndpointMapDTO queryEndpointMap;
	private EndpointDTO endpoint;
	@Autowired private QueryManager queryManager;
	@Autowired private PatientManager patientManager;
	@Autowired private LocationManager locationManager;
	@Autowired private AdapterFactory adapterFactory;
	private PatientSearch toSearch;
	private String assertion;
	private CommonUser user;
	
	@Override
	public void run() {
		boolean queryError = false;
		//query this organization directly for patient matches
		PatientRecordResults searchResults = null;
		
		if(endpoint == null) {
			logger.error("There is no active patient discovery endpoint.");
			queryError = true;
		} else {
			Adapter adapter = adapterFactory.getAdapter(endpoint);
			if(adapter != null) {
				logger.info("Starting query to endpoint with external id '" + endpoint.getExternalId() + "'");
				try {
					searchResults = adapter.queryPatients(user, endpoint, toSearch, assertion);
					logger.info("Successfully queried endpoint with external id '" + endpoint.getExternalId() + "'");
				} catch(Exception ex) {
					logger.error("Exception thrown in adapter " + adapter.getClass() + ": " + ex.getMessage(), ex);
					queryError = true;
				}
			}
			//store the patients returned so we can retrieve them later when all orgs have finished querying
			if(searchResults != null && searchResults.getStatus() == IheStatus.Success) {
				List<PatientRecordDTO> patientResults = searchResults.getResults();
				if(patientResults != null && patientResults.size() > 0) {
					logger.info("Found " + patientResults.size() + " results for endpoint with external id '" + endpoint.getExternalId() + "'");
					if(patientResults != null) {
						for(PatientRecordDTO patient : patientResults) {
							patient.setQueryEndpointId(queryEndpointMap.getId());
								
							//save the search results
							queryManager.addPatientRecord(patient);
							logger.info("Added patient record to the orgStatus " + queryEndpointMap.getId());
						}
					}
				} else {
					logger.info("Found 0 results for endpoint with external id '" + endpoint.getExternalId() + "'");
				}
			} else if(searchResults != null && searchResults.getStatus() == IheStatus.Failure) { 
				queryError = true;
			} else {
				logger.info("Search results were null, cannot determine status from : '" + endpoint.getExternalId() + "'");
				queryError = true;
			}
			logger.info("Completed query to endpoint with external id '" + 
					endpoint.getExternalId());
		}
		
		//this has to be synchronized on a object that will be the same across all threads.
		//we tried synchronizing on the query id but that is a Long that, while it would have 
		//the same value, occupies a different address in memory (not the same object) across
		//difference instances of a Query or QueryLocation. queryManager works well because
		//it's initialized by Spring and is a singleton.
		synchronized(queryManager) {
			queryEndpointMap.setStatus(queryError ? QueryEndpointStatus.Failed : QueryEndpointStatus.Successful);
			queryEndpointMap.setEndDate(new Date());
			queryManager.createOrUpdateQueryEndpointMap(queryEndpointMap);
			queryManager.updateQueryStatusFromEndpoints(queryEndpointMap.getQueryId());
		}
	}
	
	
	
	public String getAssertion() {
		return assertion;
	}



	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}



	public CommonUser getUser() {
		return user;
	}

	public void setUser(CommonUser user) {
		this.user = user;
	}
	
	public QueryManager getQueryManager() {
		return queryManager;
	}

	public void setQueryManager(QueryManager queryManager) {
		this.queryManager = queryManager;
	}

	public PatientManager getPatientManager() {
		return patientManager;
	}

	public void setPatientManager(PatientManager patientManager) {
		this.patientManager = patientManager;
	}

	public AdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	public void setAdapterFactory(AdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}

	public PatientSearch getToSearch() {
		return toSearch;
	}

	public void setToSearch(PatientSearch toSearch) {
		this.toSearch = toSearch;
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public QueryEndpointMapDTO getQueryEndpointMap() {
		return queryEndpointMap;
	}

	public void setQueryEndpointMap(QueryEndpointMapDTO queryEndpointMap) {
		this.queryEndpointMap = queryEndpointMap;
	}

	public EndpointDTO getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(EndpointDTO endpoint) {
		this.endpoint = endpoint;
	}
}