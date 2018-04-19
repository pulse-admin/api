package gov.ca.emsa.pulse.broker.entity;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

@Entity
@Table(name="location")
public class LocationEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "external_id", nullable = false )
	private String externalId;
	
	@Column(name="location_status_id")
	private Long locationStatusId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "location_status_id", nullable = false, insertable = false, updatable = false)
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
	private Set<LocationAddressLineEntity> lines = new LinkedHashSet<LocationAddressLineEntity>();
 	
	@Column(name = "city")
	private String city;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "zipcode")
	private String zipcode;
	
	@Column(name = "location_last_updated")
	private Date externalLastUpdatedDate;
	
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public Date getExternalLastUpdatedDate() {
		return externalLastUpdatedDate;
	}

	public void setExternalLastUpdatedDate(Date externalLastUpdatedDate) {
		this.externalLastUpdatedDate = externalLastUpdatedDate;
	}

	public Set<LocationAddressLineEntity> getLines() {
		return lines;
	}

	public void setLines(Set<LocationAddressLineEntity> lines) {
		this.lines = lines;
	}

	public boolean hasRequiredFields() {
		return !StringUtils.isEmpty(this.getName()) && 
				!StringUtils.isEmpty(this.getExternalId()) &&
				this.getLocationStatusId() != null;
	}
}
