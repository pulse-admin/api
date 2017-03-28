package gov.ca.emsa.pulse.broker.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

@Entity
@Table(name="patient_record_name")
public class PatientRecordNameEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column( name = "id", nullable = false )
	private Long id;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "patientRecordNameId")
	@Column(name = "patient_record_name_id")
	private Set<GivenNameEntity> givenNames = new HashSet<GivenNameEntity>();
	
	@Column(name = "patient_record_id")
	private Long patientRecordId;
	
	@Column(name = "name_type_id")
	private Long nameTypeId;
	
	@OneToOne
	@JoinColumn(name="id")
	private NameTypeEntity nameType;
	
	@Column(name="family_name")
	private String familyName;
	
	@Column(name = "name_representation_id")
	private Long nameRepresentationId;
	
	@OneToOne
	@JoinColumn(name="id")
	private NameRepresentationEntity nameRepresentation;
	
	@Column(name = "name_assembly_id")
	private Long nameAssemblyId;
	
	@OneToOne
	@JoinColumn(name="id")
	private NameAssemblyEntity nameAssembly;
	
	@Column(name="suffix")
	private String suffix;
	
	@Column(name="prefix")
	private String prefix;
	
	@Column(name = "prof_suffix")
	private String profSuffix;
	
	@Column(name="effective_date")
	private Date effectiveDate;
	
	@Column(name="expiration_date")
	private Date expirationDate;
	
	@Column( name = "last_read_date", insertable = false, updatable = false)
	private Date lastReadDate;
	
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
	
	public Long getPatientRecordId(){
		return patientRecordId;
	}
	
	public void setPatientRecordId(Long patientRecordId){
		this.patientRecordId = patientRecordId;
	}
	
	public Set<GivenNameEntity> getGivenNames() {
		return givenNames;
	}

	public void setGivenNames(Set<GivenNameEntity> givenNames) {
		this.givenNames = givenNames;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public NameTypeEntity getNameType() {
		return nameType;
	}

	public void setNameType(NameTypeEntity nameTypeCode) {
		this.nameType = nameTypeCode;
	}

	public NameRepresentationEntity getNameRepresentation() {
		return nameRepresentation;
	}

	public void setNameRepresentation(NameRepresentationEntity nameRepresentation) {
		this.nameRepresentation = nameRepresentation;
	}

	public NameAssemblyEntity getNameAssembly() {
		return nameAssembly;
	}

	public void setNameAssembly(NameAssemblyEntity nameAssembly) {
		this.nameAssembly = nameAssembly;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Date getLastReadDate() {
		return lastReadDate;
	}

	public void setLastReadDate(Date lastReadDate) {
		this.lastReadDate = lastReadDate;
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

	public String getProfSuffix() {
		return profSuffix;
	}

	public void setProfSuffix(String profSuffix) {
		this.profSuffix = profSuffix;
	}

	public Long getNameTypeId() {
		return nameTypeId;
	}

	public void setNameTypeId(Long nameTypeId) {
		this.nameTypeId = nameTypeId;
	}

	public Long getNameRepresentationId() {
		return nameRepresentationId;
	}

	public void setNameRepresentationId(Long nameRepresentationId) {
		this.nameRepresentationId = nameRepresentationId;
	}

	public Long getNameAssemblyId() {
		return nameAssemblyId;
	}

	public void setNameAssemblyId(Long nameAssemblyId) {
		this.nameAssemblyId = nameAssemblyId;
	}
	
}
