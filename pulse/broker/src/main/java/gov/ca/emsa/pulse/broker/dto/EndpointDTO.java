package gov.ca.emsa.pulse.broker.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.ca.emsa.pulse.broker.entity.EndpointEntity;
import gov.ca.emsa.pulse.broker.entity.EndpointMimeTypeEntity;
import gov.ca.emsa.pulse.broker.entity.LocationEndpointMapEntity;

public class EndpointDTO {
	
	private Long id;
	private String externalId;
	private EndpointTypeDTO endpointType;
	private EndpointStatusDTO endpointStatus;
	private String managingOrganization;
	private String organizationId;
	private String adapter;
	private List<EndpointMimeTypeDTO> mimeTypes;
	private String payloadType;
	private String publicKey;
	private String url;
	private Date externalLastUpdateDate;
	private Date creationDate;
	private Date lastModifiedDate;
	private List<LocationDTO> locations;
	
	public EndpointDTO(){
		locations = new ArrayList<LocationDTO>();
		mimeTypes = new ArrayList<EndpointMimeTypeDTO>();
	}
	
	public EndpointDTO(EndpointEntity entity){
		this();
		
		this.id = entity.getId();
		this.externalId = entity.getExternalId();
		if(entity.getEndpointType() != null) {
			this.endpointType = new EndpointTypeDTO(entity.getEndpointType());
		} else {
			this.endpointType = new EndpointTypeDTO();
			this.endpointType.setId(entity.getEndpointTypeId());
		}
		
		if(entity.getEndpointStatus() != null) {
			this.endpointStatus = new EndpointStatusDTO(entity.getEndpointStatus());
		} else {
			this.endpointStatus = new EndpointStatusDTO();
			this.endpointStatus.setId(entity.getEndpointStatusId());
		}
		this.managingOrganization = entity.getManagingOrganization();
		this.organizationId = entity.getOrganizationId();
		this.adapter = entity.getAdapter();
		if(entity.getMimeTypes() != null && entity.getMimeTypes().size() > 0) {
			for(EndpointMimeTypeEntity entityMimeType : entity.getMimeTypes()) {
				EndpointMimeTypeDTO dtoMimeType = new EndpointMimeTypeDTO(entityMimeType);
				this.mimeTypes.add(dtoMimeType);
			}
		}
		
		if(entity.getLocationEndpointMaps() != null && entity.getLocationEndpointMaps().size() > 0) {
			for(LocationEndpointMapEntity locMapEntity : entity.getLocationEndpointMaps()) {
				if(locMapEntity.getLocation() != null) {
					this.locations.add(new LocationDTO(locMapEntity.getLocation()));
				}
			}
		}
		
		this.payloadType = entity.getPayloadType();
		this.publicKey = entity.getPublicKey();
		this.url = entity.getUrl();
		this.externalLastUpdateDate = entity.getExternalLastUpdateDate();
		this.creationDate = entity.getCreationDate();
		this.lastModifiedDate = entity.getLastModifiedDate();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	} 

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public Date getExternalLastUpdateDate() {
		return externalLastUpdateDate;
	}

	public void setExternalLastUpdateDate(Date externalLastUpdateDate) {
		this.externalLastUpdateDate = externalLastUpdateDate;
	}

	public EndpointTypeDTO getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(EndpointTypeDTO endpointType) {
		this.endpointType = endpointType;
	}

	public EndpointStatusDTO getEndpointStatus() {
		return endpointStatus;
	}

	public void setEndpointStatus(EndpointStatusDTO endpointStatus) {
		this.endpointStatus = endpointStatus;
	}

	public String getAdapter() {
		return adapter;
	}

	public void setAdapter(String adapter) {
		this.adapter = adapter;
	}

	public String getPayloadType() {
		return payloadType;
	}

	public void setPayloadType(String payloadType) {
		this.payloadType = payloadType;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<EndpointMimeTypeDTO> getMimeTypes() {
		return mimeTypes;
	}

	public void setMimeTypes(List<EndpointMimeTypeDTO> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public List<LocationDTO> getLocations() {
		return locations;
	}

	public void setLocations(List<LocationDTO> locations) {
		this.locations = locations;
	}

	public String getManagingOrganization() {
		return managingOrganization;
	}

	public void setManagingOrganization(String managingOrganization) {
		this.managingOrganization = managingOrganization;
	}
}
