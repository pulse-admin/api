package gov.ca.emsa.pulse.broker.cache;

import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.dto.LocationEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.EndpointManager;
import gov.ca.emsa.pulse.broker.manager.LocationManager;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.cten.CtenToPulseConverter;
import gov.ca.emsa.pulse.cten.domain.EndpointWrapper;
import gov.ca.emsa.pulse.cten.domain.LocationWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;

public class CtenDirectoryRefreshManager {
	private static final Logger logger = LogManager.getLogger(CtenDirectoryRefreshManager.class);

	private LocationManager locationManager;
	private EndpointManager endpointManager;
	private String locationDirectoryUrl, endpointDirectoryUrl;
	private String status;
	
	public void getCtenLocationsAndEndpoints(){
		//query locations
		logger.info("Querying the locations from " + locationDirectoryUrl);
		RestTemplate restTemplate = new RestTemplate();
		LocationWrapper remoteLocations = restTemplate.getForObject(locationDirectoryUrl, LocationWrapper.class);
		//convert to our internal location object
		List<Location> locations = CtenToPulseConverter.convertLocations(remoteLocations);
		logger.debug("Found " + locations.size() + " locations from " + locationDirectoryUrl);;
		locationManager.updateLocations(locations);
		
		//query the endpoints
		logger.info("Querying the endpoints from " + endpointDirectoryUrl);
		restTemplate = new RestTemplate();
		EndpointWrapper remoteEndpoints = restTemplate.getForObject(endpointDirectoryUrl, EndpointWrapper.class);
		//convert to our internal endpoint object
		List<Endpoint> endpoints = CtenToPulseConverter.convertEndpoints(remoteEndpoints);
		logger.debug("Found " + endpoints.size() + " endpoints from " + endpointDirectoryUrl);;
		endpointManager.updateEndpoints(endpoints);
		
		//now update the mappings between locations and endpoints
		//these mappings aren't referenced by other tables so it should be ok if any need to get deleted
		List<LocationEndpointMapDTO> locationEndpointMappings = new ArrayList<LocationEndpointMapDTO>();
		for(Location ctenLocation : locations) {
			for(Endpoint ctenEndpoint : ctenLocation.getEndpoints()) {
				
				//find location with same external id
				LocationDTO locationToMap = locationManager.getByExternalId(ctenLocation.getExternalId());
				//find the endpoint with the same external id
				EndpointDTO endpointToMap = endpointManager.getByExternalId(ctenEndpoint.getExternalId());
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

	public String getLocationDirectoryUrl() {
		return locationDirectoryUrl;
	}

	public void setLocationDirectoryUrl(String locationDirectoryUrl) {
		this.locationDirectoryUrl = locationDirectoryUrl;
	}

	public String getEndpointDirectoryUrl() {
		return endpointDirectoryUrl;
	}

	public void setEndpointDirectoryUrl(String endpointDirectoryUrl) {
		this.endpointDirectoryUrl = endpointDirectoryUrl;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
