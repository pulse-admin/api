package gov.ca.emsa.pulse.sequoia.domain;

import java.util.ArrayList;
import java.util.List;

public class SequoiaLocationResource {
	private String id;
	private String status;
	private String name;
	private String mode;
	private String description;
	private SequoiaResourceType type;
	private SequoiaResourceType physicalType;
	private SequoiaAddress address;
	private SequoiaManagingOrganization managingOrganization;
	private SequoiaResourceMetadata meta;
	private List<SequoiaEndpointMetadata> endpoint;
	private List<SequoiaLocationIdentifier> identifier;
	private String resourceType;
	
	public SequoiaLocationResource() {
		endpoint = new ArrayList<SequoiaEndpointMetadata>();
		identifier = new ArrayList<SequoiaLocationIdentifier>();
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public SequoiaResourceType getType() {
		return type;
	}
	public void setType(SequoiaResourceType type) {
		this.type = type;
	}
	public SequoiaResourceType getPhysicalType() {
		return physicalType;
	}
	public void setPhysicalType(SequoiaResourceType physicalType) {
		this.physicalType = physicalType;
	}
	public SequoiaAddress getAddress() {
		return address;
	}
	public void setAddress(SequoiaAddress address) {
		this.address = address;
	}
	public SequoiaManagingOrganization getManagingOrganization() {
		return managingOrganization;
	}
	public void setManagingOrganization(SequoiaManagingOrganization managingOrganization) {
		this.managingOrganization = managingOrganization;
	}
	public SequoiaResourceMetadata getMeta() {
		return meta;
	}
	public void setMeta(SequoiaResourceMetadata meta) {
		this.meta = meta;
	}
	public List<SequoiaEndpointMetadata> getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(List<SequoiaEndpointMetadata> endpoint) {
		this.endpoint = endpoint;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<SequoiaLocationIdentifier> getIdentifier() {
		return identifier;
	}

	public void setIdentifier(List<SequoiaLocationIdentifier> identifier) {
		this.identifier = identifier;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
}
