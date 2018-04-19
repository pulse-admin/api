package gov.ca.emsa.pulse.broker.entity;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

@Entity
@Table(name="endpoint")
public class EndpointEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "external_id", nullable = false )
	private String externalId;
	
	@Column(name="endpoint_type_id")
	private Long endpointTypeId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "endpoint_type_id", unique=true, nullable = true, insertable=false, updatable= false)
	private EndpointTypeEntity endpointType;
	
	@Column(name="endpoint_status_id")
	private Long endpointStatusId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "endpoint_status_id", unique=true, nullable = true, insertable=false, updatable= false)
	private EndpointStatusEntity endpointStatus;
	
	@Column(name = "managing_organization_name")
	private String managingOrganization;
	
	@Column(name = "organization_id")
	private String organizationId;
	
	@Column(name = "adapter")
	private String adapter;
	
	@Column(name = "payload_type")
	private String payloadType;
	
	@Column(name = "public_key")
	private String publicKey;
	
	@Column(name = "endpoint_url")
	private String url;
	
	@Column(name = "endpoint_last_updated")
	private Date externalLastUpdateDate;
	
	@OneToMany( fetch = FetchType.LAZY, mappedBy = "endpointId")
	@Column( name = "endpoint_id", nullable = false)
	private Set<EndpointMimeTypeEntity> mimeTypes = new LinkedHashSet<EndpointMimeTypeEntity>();
	
	@OneToMany( fetch = FetchType.LAZY, mappedBy = "endpointId" )
	@Column( name = "endpoint_id", nullable = false)
	private Set<LocationEndpointMapEntity> locationEndpointMaps = new LinkedHashSet<LocationEndpointMapEntity>();
	
	@Column( name = "creation_date", insertable = false, updatable = false)
	private Date creationDate;
	
	@Column( name = "last_modified_date", insertable = false, updatable = false)
	private Date lastModifiedDate;
	
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getEndpointTypeId() {
		return endpointTypeId;
	}

	public void setEndpointTypeId(Long endpointTypeId) {
		this.endpointTypeId = endpointTypeId;
	}

	public EndpointTypeEntity getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(EndpointTypeEntity endpointType) {
		this.endpointType = endpointType;
	}

	public Long getEndpointStatusId() {
		return endpointStatusId;
	}

	public void setEndpointStatusId(Long endpointStatusId) {
		this.endpointStatusId = endpointStatusId;
	}

	public EndpointStatusEntity getEndpointStatus() {
		return endpointStatus;
	}

	public void setEndpointStatus(EndpointStatusEntity endpointStatus) {
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

	public Date getExternalLastUpdateDate() {
		return externalLastUpdateDate;
	}

	public void setExternalLastUpdateDate(Date externalLastUpdateDate) {
		this.externalLastUpdateDate = externalLastUpdateDate;
	}
	
	public boolean hasRequiredFields() {
		return !StringUtils.isEmpty(this.getExternalId()) && 
				this.getEndpointTypeId() != null && this.getEndpointStatusId() != null;
	}

	public Set<EndpointMimeTypeEntity> getMimeTypes() {
		return mimeTypes;
	}

	public void setMimeTypes(Set<EndpointMimeTypeEntity> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public Set<LocationEndpointMapEntity> getLocationEndpointMaps() {
		return locationEndpointMaps;
	}

	public void setLocationEndpointMaps(Set<LocationEndpointMapEntity> locationEndpointMaps) {
		this.locationEndpointMaps = locationEndpointMaps;
	}

	public String getManagingOrganization() {
		return managingOrganization;
	}

	public void setManagingOrganization(String managingOrganization) {
		this.managingOrganization = managingOrganization;
	}
}
