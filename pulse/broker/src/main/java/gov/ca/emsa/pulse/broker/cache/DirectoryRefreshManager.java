package gov.ca.emsa.pulse.broker.cache;

import gov.ca.emsa.pulse.broker.manager.EndpointManager;
import gov.ca.emsa.pulse.broker.manager.LocationManager;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.cten.CtenToPulseConverter;
import gov.ca.emsa.pulse.cten.domain.EndpointWrapper;
import gov.ca.emsa.pulse.cten.domain.LocationWrapper;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;

public class DirectoryRefreshManager extends TimerTask {
	private static final Logger logger = LogManager.getLogger(DirectoryRefreshManager.class);

	private LocationManager locationManager;
	private EndpointManager endpointManager;
	private String locationDirectoryUrl, endpointDirectoryUrl;
	private long expirationMillis;
	
	public void getLocationsAndEndpoints(){
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
		
		//each endpoint under "locations" only has the external id filled in
		//so we need to find it's match under "endpoints" and populate with all the data
		for(Location location : locations) {
			for(int locEndpointIdx = 0; locEndpointIdx < location.getEndpoints().size(); locEndpointIdx++) {
				Endpoint endpointMeta = location.getEndpoints().get(locEndpointIdx);
				String endpointExternalId = endpointMeta.getExternalId();
				for(Endpoint endpoint : endpoints) {
					Endpoint toInsert = endpoint;
					//look for the endpoint with the same externalId but
					//make sure to ignore any that are "test" URLs
					if(endpoint.getExternalId().equalsIgnoreCase(endpointExternalId)) {
						if(endpoint.getUrl().contains("test")) {
							toInsert = null;
						}
						
						location.getEndpoints().set(locEndpointIdx, toInsert);
					}
				}
			}
		}
		
		locationManager.updateLocations(locations);
	}

	@Override
	public void run() {
		try {
			getLocationsAndEndpoints();
		} catch(Exception ex) {
			logger.error("Error updating location cache", ex);
			ex.printStackTrace();
		}
	}

	public void setExpirationMillis(long directoryRefreshExpirationMillis) {
		this.expirationMillis = directoryRefreshExpirationMillis;
	}
	
	public long getExpirationMillis() {
		return expirationMillis;
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
}
