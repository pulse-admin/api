package gov.ca.emsa.pulse.sequoia.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = {"id", "contact"})
public class SequoiaOrganization {
	
	private SequoiaResourceMetadata meta;
	private String active;
	private SequoiaResourceType type;
	private String name;
	private List<SequoiaEndpoint> endpoint;
	//private SequoiaAddress address;
	private String id;
	private String description;
	private SequoiaPartOf partOf;
	private SequoiaManagingOrganization managingOrg;
	
	public SequoiaResourceMetadata getMeta() {
		return meta;
	}

	public void setMeta(SequoiaResourceMetadata meta) {
		this.meta = meta;
	}
	
	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public SequoiaResourceType getType() {
		return type;
	}

	public void setType(SequoiaResourceType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SequoiaEndpoint> getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(List<SequoiaEndpoint> endpoint) {
		this.endpoint = endpoint;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public SequoiaPartOf getPartOf() {
		return partOf;
	}
	
	public void setPartOf(SequoiaPartOf partOf) {
		this.partOf = partOf;
	}

	public SequoiaManagingOrganization getManagingOrg() {
		return managingOrg;
	}

	public void setManagingOrg(SequoiaManagingOrganization managingOrg) {
		this.managingOrg = managingOrg;
	}

	@SuppressWarnings("unchecked")
	@JsonProperty("description")
    private void unpackNestedDescription(Map<String,Object> description) {
		this.description =  (String) description.get("value");
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("identifier")
    private void unpackNestedId(Map<String,Object> identifier) {
		Map<String, String> idMap =  (Map<String, String>) identifier.get("value");
		this.id = idMap.get("value");
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("active")
    private void unpackNestedActive(Map<String,String> active) {
		this.active = active.get("value");
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("name")
    private void unpackNestedName(Map<String,String> name) {
		this.name = name.get("value");
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("meta")
    private void unpackNestedMeta(SequoiaResourceMetadata meta) {
		this.meta = meta;
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("contained")
    private void unpackNestedEndpoint(List<SequoiaEndpoint> endpoints) {
        this.endpoint = endpoints;
    }
	
	@SuppressWarnings("unchecked")
	@JsonProperty("partOf")
    private void unpackNestedPartOf(SequoiaPartOf partOf) {
        this.partOf = partOf; 
    }
	
	@SuppressWarnings("unchecked")
	@JsonProperty("managingOrg")
    private void unpackNestedEndpoint(SequoiaManagingOrganization managingOrg) {
        this.managingOrg = managingOrg;
    }

}
