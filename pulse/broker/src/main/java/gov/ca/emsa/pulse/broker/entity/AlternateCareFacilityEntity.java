package gov.ca.emsa.pulse.broker.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="alternate_care_facility")
public class AlternateCareFacilityEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@Column(name = "liferay_state_id")
	private Long liferayStateId;
	
	@Column(name = "liferay_acf_id")
	private Long liferayAcfId;

	@Column(name="identifier")
	private String identifier;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
 	@OneToMany( fetch = FetchType.EAGER, mappedBy = "acfId"  )
	@Column( name = "alternate_care_facility_id", nullable = false  )
	private List<AlternateCareFacilityAddressLineEntity> lines = new ArrayList<AlternateCareFacilityAddressLineEntity>();
 	
 	@Column(name = "city")
	private String city;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "zipcode")
	private String zipcode;
	
	@Column(name = "last_read_date")
	private Date lastReadDate;
	
	@Column( name = "creation_date", insertable = false, updatable = false)
	private Date creationDate;
	
	@Column( name = "last_modified_date", insertable = false, updatable = false)
	private Date lastModifiedDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long long1) {
		this.id = long1;
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

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Date getLastReadDate() {
		return lastReadDate;
	}

	public void setLastReadDate(Date lastReadDate) {
		this.lastReadDate = lastReadDate;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public List<AlternateCareFacilityAddressLineEntity> getLines() {
		return lines;
	}

	public void setLines(List<AlternateCareFacilityAddressLineEntity> lines) {
		this.lines = lines;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLiferayStateId() {
		return liferayStateId;
	}

	public void setLiferayStateId(Long liferayStateId) {
		this.liferayStateId = liferayStateId;
	}

	public Long getLiferayAcfId() {
		return liferayAcfId;
	}

	public void setLiferayAcfId(Long liferayAcfId) {
		this.liferayAcfId = liferayAcfId;
	}
	
}
