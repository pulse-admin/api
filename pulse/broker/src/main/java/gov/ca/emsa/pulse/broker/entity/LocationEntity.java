package gov.ca.emsa.pulse.broker.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="location")
public class LocationEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "external_id", nullable = false )
	private Long externalId;
	
	@Column(name="location_status_id")
	private Long locationStatusId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "location_status_id", unique=true, nullable = true, insertable=false, updatable= false)
	private LocationStatusEntity locationStatus;
	
	@Column(name = "parent_organization_name")
	private String parentOrganizationName;
	
	@Column( name = "name", nullable = false )
	private String name;
	
	@Column( name = "description", nullable = false )
	private String description;
	
	@Column(name = "location_type")
	private String type;
	
	@OneToMany( fetch = FetchType.LAZY, mappedBy = "locationId"  )
	@Column( name = "location_id", nullable = false  )
	private List<LocationAddressLineEntity> lines = new ArrayList<LocationAddressLineEntity>();
 	
	@Column(name = "city")
	private String city;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "zipcode")
	private String zipcode;
	
	@Column(name = "country")
	private String country;
	
	@Column(name = "location_last_updated")
	private Date externalLastUpdatedDate;
	
	@Column( name = "creation_date", insertable = false, updatable = false)
	private Date creationDate;
	
	@Column( name = "last_modified_date", insertable = false, updatable = false)
	private Date lastModifiedDate;
	
	@OneToMany( fetch = FetchType.LAZY, mappedBy = "locationId"  )
	@Column( name = "location_id", nullable = false  )
	private List<LocationEndpointEntity> endpoints = new ArrayList<LocationEndpointEntity>();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Long getLocationStatusId() {
		return locationStatusId;
	}

	public void setLocationStatusId(Long locationStatusId) {
		this.locationStatusId = locationStatusId;
	}

	public LocationStatusEntity getLocationStatus() {
		return locationStatus;
	}

	public void setLocationStatus(LocationStatusEntity locationStatus) {
		this.locationStatus = locationStatus;
	}

	public String getParentOrganizationName() {
		return parentOrganizationName;
	}

	public void setParentOrganizationName(String parentOrganizationName) {
		this.parentOrganizationName = parentOrganizationName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getExternalLastUpdatedDate() {
		return externalLastUpdatedDate;
	}

	public void setExternalLastUpdatedDate(Date externalLastUpdatedDate) {
		this.externalLastUpdatedDate = externalLastUpdatedDate;
	}

	public List<LocationAddressLineEntity> getLines() {
		return lines;
	}

	public void setLines(List<LocationAddressLineEntity> lines) {
		this.lines = lines;
	}

	public List<LocationEndpointEntity> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<LocationEndpointEntity> endpoints) {
		this.endpoints = endpoints;
	}

	
	
}