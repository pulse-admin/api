package gov.ca.emsa.pulse.sequoia;

import gov.ca.emsa.pulse.common.domain.Address;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.EndpointMimeType;
import gov.ca.emsa.pulse.common.domain.EndpointStatus;
import gov.ca.emsa.pulse.common.domain.EndpointType;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.common.domain.LocationStatus;
import gov.ca.emsa.pulse.cten.domain.EndpointMetadata;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaBundle;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaEndpoint;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaEndpointResource;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaEntry;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaLocationResource;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaOrganization;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaOrganizationResource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

public class SequoiaToPulseConverter {
	private static String lastUpdatedDateFormat="yyyy-MM-dd'T'HH:mm:ss";
	
	public static List<Location> convertSequoiaBundleToLocations(SequoiaBundle bundle) {
		if(bundle == null || bundle.getBundle().getEntry() == null || bundle.getBundle().getEntry().size() == 0) {
			return new ArrayList<Location>();
		}
		List<Location> result = new ArrayList<Location>(bundle.getBundle().getEntry().size());
		for(SequoiaEntry entry : bundle.getBundle().getEntry()) {
			result.add(convertLocation(entry));
		}
		return result;
	}
	
	public static Location convertLocation(SequoiaEntry entry) {
		if(entry.getResource() == null) {
			return null;
		}
		
		Location result = new Location();
		SequoiaOrganization sequoiaResource = entry.getResource().getOrganization();
		result.setExternalId(sequoiaResource.getId());
		result.setName(sequoiaResource.getName());
		if(sequoiaResource.getManagingOrg() != null) {
			result.setParentOrgName(sequoiaResource.getManagingOrg().getReference());
		}
		LocationStatus resultStatus = new LocationStatus();
		resultStatus.setName(sequoiaResource.getActive());
		result.setStatus(resultStatus);
		/*if(sequoiaResource.getAddress() != null) {
			Address locAddr = new Address();
			if(sequoiaResource.getAddress().getLine() != null) {
				locAddr.getLines().add(sequoiaResource.getAddress().getLine());
			}
			locAddr.setCity(sequoiaResource.getAddress().getCity());
			locAddr.setState(sequoiaResource.getAddress().getState());
			locAddr.setZipcode(sequoiaResource.getAddress().getPostalCode());
			locAddr.setCountry(sequoiaResource.getAddress().getCountry());
			result.setAddress(locAddr);
		}*/
		result.setDescription(sequoiaResource.getDescription());
		if(!StringUtils.isEmpty(sequoiaResource.getMeta().getLastUpdated())) {
			SimpleDateFormat formatter = new SimpleDateFormat(lastUpdatedDateFormat);
			try {
				result.setExternalLastUpdateDate(formatter.parse(sequoiaResource.getMeta().getLastUpdated()));		
			} catch(ParseException ex) {
				System.err.println("Parse exception parsing " + sequoiaResource.getMeta().getLastUpdated() + " with format " + lastUpdatedDateFormat);;
			}
		}
		if(sequoiaResource.getEndpoint() != null && sequoiaResource.getEndpoint().size() > 0) {
			for(SequoiaEndpoint sequoiaEndpoint : sequoiaResource.getEndpoint()) {
					Endpoint endpoint = new Endpoint();
					endpoint.setExternalId(sequoiaEndpoint.getEndpoint().getId());
					result.getEndpoints().add(endpoint);
			}
		}
		result.setName(sequoiaResource.getName());
		
		return result;
	}
	
	public static List<Endpoint> convertSequoiaBundleToEndpoints(SequoiaBundle bundle, String adapter) {
		if(bundle.getBundle() == null || bundle.getBundle().getEntry() == null || bundle.getBundle().getEntry().size() == 0) {
			return new ArrayList<Endpoint>();
		}
		List<Endpoint> result = new ArrayList<Endpoint>(bundle.getBundle().getEntry().size());
		for(SequoiaEntry entry : bundle.getBundle().getEntry()) {
			if(entry.getResource().getOrganization().getEndpoint() != null){
				for(SequoiaEndpoint endpoint : entry.getResource().getOrganization().getEndpoint()){
					result.add(convertEndpoints(entry, endpoint, adapter));
				}
			}
		}
		return result;
	}
	
	public static Endpoint convertEndpoints(SequoiaEntry entry, SequoiaEndpoint sequoiaEndpoint, String adapter) {
		if(sequoiaEndpoint == null) {
			return null;
		}
		SequoiaOrganization org = entry.getResource().getOrganization();
		
		Endpoint result = new Endpoint();
		SequoiaEndpointResource sequoiaResource = sequoiaEndpoint.getEndpoint();
		result.setExternalId(sequoiaResource.getId());
		result.setAdapter(adapter);
		EndpointType resultType = new EndpointType();
		resultType.setName(sequoiaResource.getName());
		resultType.setCode(sequoiaResource.getConnectionType());
		result.setEndpointType(resultType);
		result.setExternalId(sequoiaResource.getId());
		result.setOrganizationId(org.getId()); // instead of managing org we are setting just the org here
		if(!StringUtils.isEmpty(org.getMeta().getLastUpdated())) {
			SimpleDateFormat formatter = new SimpleDateFormat(lastUpdatedDateFormat);
			try {
				result.setExternalLastUpdateDate(formatter.parse(org.getMeta().getLastUpdated()));		
			} catch(ParseException ex) {
				System.err.println("Parse exception parsing " + org.getMeta().getLastUpdated() + " with format " + lastUpdatedDateFormat);;
			}
		}
		if(sequoiaResource.getPayloadMimeFormat() != null) {
				EndpointMimeType mtDomain = new EndpointMimeType();
				mtDomain.setMimeType(sequoiaResource.getPayloadMimeFormat());
				result.getMimeTypes().add(mtDomain);
		}
		if(sequoiaResource.getPayloadType() != null) {
			result.setPayloadType(sequoiaResource.getPayloadType());
		}
		result.setUrl(sequoiaResource.getFullUrl());
		EndpointStatus endpointStatus = new EndpointStatus();
		endpointStatus.setName(org.getActive());
		result.setEndpointStatus(endpointStatus);
		return result;
	}

}
