package gov.ca.emsa.pulse.common.domain;

import java.util.Date;

public class AlternateCareFacility {
	private Long id;
	private Long liferayStateId;
	private Long liferayAcfId;
	private String identifier;
	private String name;
	private String phoneNumber;
	private Address address;
	private Date lastRead;
	
	public AlternateCareFacility() {}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address acfAddr) {
		this.address = acfAddr;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setLastRead(Date lastRead) {
		this.lastRead = lastRead;
	}

	public Date getLastRead() {
		return lastRead;
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
