package gov.ca.emsa.pulse.broker.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="patient")
public class PatientEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@Column(name = "full_name_enc")
	@ColumnTransformer(
		read = "pgp_pub_decrypt(full_name_enc, dearmor((SELECT * from private_key())))", 
		write = "pgp_pub_encrypt(?, dearmor((SELECT * from public_key())))")
	private String fullName;
	
	@Column(name = "friendly_name_enc")
	@ColumnTransformer(
			read = "pgp_pub_decrypt(friendly_name_enc, dearmor((SELECT * from private_key())))", 
			write = "pgp_pub_encrypt(?, dearmor((SELECT * from public_key())))")
	private String friendlyName;
	
	@Column(name = "dob_enc")
	@ColumnTransformer(
			read = "pgp_pub_decrypt(dob_enc, dearmor((SELECT * from private_key())))", 
			write = "pgp_pub_encrypt(?, dearmor((SELECT * from public_key())))")
	private String dateOfBirth;
	
	@Column(name = "ssn_enc")
	@ColumnTransformer(
			read = "pgp_pub_decrypt(ssn_enc, dearmor((SELECT * from private_key())))", 
			write = "pgp_pub_encrypt(?, dearmor((SELECT * from public_key())))")
	private String ssn;
	
	@Column(name = "gender_enc")
	@ColumnTransformer(
			read = "pgp_pub_decrypt(gender_enc, dearmor((SELECT * from private_key())))", 
			write = "pgp_pub_encrypt(?, dearmor((SELECT * from public_key())))")
	private String gender;
	
	@Column(name = "alternate_care_facility_id")
	private Long acfId;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "alternate_care_facility_id", unique=true, nullable = true, insertable=false, updatable= false)
	private AlternateCareFacilityEntity acf;
	
	@Column( name = "creation_date", insertable = false, updatable = false)
	private Date creationDate;
	
	@Column( name = "last_modified_date", insertable = false, updatable = false)
	private Date lastModifiedDate;
	
	@Column(name = "last_read_date")
	private Date lastReadDate;
	
	@OneToMany( fetch = FetchType.LAZY, mappedBy = "patientId"  )
	@Fetch(FetchMode.JOIN)
	@Column( name = "patient_id", nullable = false  )
	private Set<PatientEndpointMapEntity> endpointMaps = new HashSet<PatientEndpointMapEntity>();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
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

	public Long getAcfId() {
		return acfId;
	}

	public void setAcfId(Long acfId) {
		this.acfId = acfId;
	}

	public AlternateCareFacilityEntity getAcf() {
		return acf;
	}

	public void setAcf(AlternateCareFacilityEntity acf) {
		this.acf = acf;
	}

	public Date getLastReadDate() {
		return lastReadDate;
	}

	public void setLastReadDate(Date lastReadDate) {
		this.lastReadDate = lastReadDate;
	}

	public Set<PatientEndpointMapEntity> getEndpointMaps() {
		return endpointMaps;
	}

	public void setEndpointMaps(Set<PatientEndpointMapEntity> endpointMaps) {
		this.endpointMaps = endpointMaps;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
}