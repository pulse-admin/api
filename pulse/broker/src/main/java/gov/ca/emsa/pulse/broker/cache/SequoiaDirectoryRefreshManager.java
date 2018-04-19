package gov.ca.emsa.pulse.broker.cache;

import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.dto.LocationEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.EndpointManager;
import gov.ca.emsa.pulse.broker.manager.LocationManager;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.sequoia.SequoiaToPulseConverter;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaBundle;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

public class SequoiaDirectoryRefreshManager {
	private static final Logger logger = LogManager.getLogger(SequoiaDirectoryRefreshManager.class);

	private LocationManager locationManager;
	private EndpointManager endpointManager;
	private String sequoiaCareQualityOrganizationDirectoryUrl;
	private String sequoiaEHexOrganizationDirectoryUrl;
	private String status;
	private static final String CAREQUALITY = "carequality";
	private static final String EHEALTH = "eHealth";
	
	public void getSequoiaLocationsAndEndpoints(){
		//query locations
		logger.info("Querying the organizations from " + sequoiaCareQualityOrganizationDirectoryUrl);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request,body);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        });
		SequoiaBundle cqOrganizations = restTemplate.getForObject(sequoiaCareQualityOrganizationDirectoryUrl, SequoiaBundle.class);
		SequoiaBundle eHexOrganizations = restTemplate.getForObject(sequoiaEHexOrganizationDirectoryUrl, SequoiaBundle.class);
		//convert from sequoia bundle to internal location and endpoint objects
		List<Location> cqLocations = SequoiaToPulseConverter.convertSequoiaBundleToLocations(cqOrganizations);
		List<Endpoint> cqEndpoints = SequoiaToPulseConverter.convertSequoiaBundleToEndpoints(cqOrganizations, CAREQUALITY);
		List<Location> eHexLocations = SequoiaToPulseConverter.convertSequoiaBundleToLocations(eHexOrganizations);
		List<Endpoint> eHexEndpoints = SequoiaToPulseConverter.convertSequoiaBundleToEndpoints(eHexOrganizations, EHEALTH);
		logger.info("Found " + cqLocations.size() + " locations from " + sequoiaCareQualityOrganizationDirectoryUrl);
		logger.info("Found " + cqEndpoints.size() + " endpoints from " + sequoiaCareQualityOrganizationDirectoryUrl);
		logger.info("Found " + eHexLocations.size() + " locations from " + sequoiaEHexOrganizationDirectoryUrl);
		logger.info("Found " + eHexEndpoints.size() + " endpoints from " + sequoiaEHexOrganizationDirectoryUrl);
		cqLocations.addAll(eHexLocations);
		cqEndpoints.addAll(eHexEndpoints);
		locationManager.updateLocations(cqLocations);
		endpointManager.updateEndpoints(cqEndpoints);
		
		//now update the mappings between locations and endpoints
		//these mappings aren't referenced by other tables so it should be ok if any need to get deleted
		List<LocationEndpointMapDTO> locationEndpointMappings = new ArrayList<LocationEndpointMapDTO>();
		for(Location seqLocation : cqLocations) {
			for(Endpoint seqEndpoint : seqLocation.getEndpoints()) {
				//find location with same external id
				LocationDTO locationToMap = locationManager.getByExternalId(seqLocation.getExternalId());
				//find the endpoint with the same external id
				EndpointDTO endpointToMap = endpointManager.getByExternalId(seqEndpoint.getExternalId());
				if(locationToMap != null && endpointToMap != null) {
					LocationEndpointMapDTO toMap = new LocationEndpointMapDTO();
					toMap.setEndpointId(endpointToMap.getId());
					toMap.setLocationId(locationToMap.getId());
					locationEndpointMappings.add(toMap);
				}
			}
		}
		endpointManager.updateEndpointLocationMappings(locationEndpointMappings);
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public EndpointManager getEndpointManager() {
		return endpointManager;
	}

	public void setEndpointManager(EndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	public String getSequoiaCareQualityOrganizationDirectoryUrl() {
		return sequoiaCareQualityOrganizationDirectoryUrl;
	}

	public void setSequoiaCareQualityOrganizationDirectoryUrl(
			String sequoiaOrganizationDirectoryUrl) {
		this.sequoiaCareQualityOrganizationDirectoryUrl = sequoiaOrganizationDirectoryUrl;
	}
	
	public String getSequoiaEHexOrganizationDirectoryUrl() {
		return sequoiaEHexOrganizationDirectoryUrl;
	}

	public void setSequoiaEHexOrganizationDirectoryUrl(
			String sequoiaEHexOrganizationDirectoryUrl) {
		this.sequoiaEHexOrganizationDirectoryUrl = sequoiaEHexOrganizationDirectoryUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
