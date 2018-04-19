package gov.ca.emsa.pulse.common.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Endpoint {
	
	private Long id;
	private String externalId;
	private EndpointType endpointType;
	private EndpointStatus endpointStatus;
	private String managingOrganization;
	private String organizationId;
	private String adapter;
	private List<EndpointMimeType> mimeTypes;
	private String payloadType;
	private String publicKey;
	private String url;
	private Date externalLastUpdateDate;
	private List<Location> locations;
	private Date creationDate;
	private Date lastModifiedDate;
	
	public Endpoint(){
		locations = new ArrayList<Location>();
		mimeTypes = new ArrayList<EndpointMimeType>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public EndpointType getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(EndpointType endpointType) {
		this.endpointType = endpointType;
	}

	public EndpointStatus getEndpointStatus() {
		return endpointStatus;
	}

	public void setEndpointStatus(EndpointStatus endpointStatus) {
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

	public String getManagingOrganization() {
		return managingOrganization;
	}

	public void setManagingOrganization(String managingOrganization) {
		this.managingOrganization = managingOrganization;
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

	public Date getExternalLastUpdateDate() {
		return externalLastUpdateDate;
	}

	public void setExternalLastUpdateDate(Date externalLastUpdateDate) {
		this.externalLastUpdateDate = externalLastUpdateDate;
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

	public List<EndpointMimeType> getMimeTypes() {
		return mimeTypes;
	}

	public void setMimeTypes(List<EndpointMimeType> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public String toString() {
		return "Endpoint [id=" + id + ", externalId=" + externalId
				+ ", endpointType=" + endpointType.toString() + ", endpointStatus="
				+ endpointStatus + ", managingOrganization="
				+ organizationId + ", adapter=" + adapter
				+ ", mimeTypes=" + mimeTypes + ", payloadType=" + payloadType
				+ ", publicKey=" + publicKey + ", url=" + url
				+ ", externalLastUpdateDate=" + externalLastUpdateDate
				+ ", locations=" + locations + ", creationDate=" + creationDate
				+ ", lastModifiedDate=" + lastModifiedDate + "]";
	}
	
	
	
}
