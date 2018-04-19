package gov.ca.emsa.pulse.sequoia.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = {"extension", "managingOrganization"})
public class SequoiaEndpointResource {
	private String fullUrl;
	private String name;
	private String payloadMimeFormat;
	private String payloadType;
	private String connectionType;
	private String id;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFullUrl() {
		return fullUrl;
	}
	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}
	public String getPayloadMimeFormat() {
		return payloadMimeFormat;
	}
	public void setPayloadMimeFormat(String payloadMimeFormat) {
		this.payloadMimeFormat = payloadMimeFormat;
	}
	public String getPayloadType() {
		return payloadType;
	}
	public void setPayloadType(String payloadType) {
		this.payloadType = payloadType;
	}
	public String getConnectionType() {
		return connectionType;
	}
	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("address")
    private void unpackNestedFullUrl(Map<String,String> address) {
		this.fullUrl = address.get("value");
	}

	@JsonProperty("name")
    private void unpackNestedName(Map<String,String> name) {
		this.name = name.get("value");
	}
	
	@JsonProperty("payloadMimeFormat")
    private void unpackNestedPayloadMimeFormat(Map<String,String> payloadMimeFormat) {
		this.payloadMimeFormat = payloadMimeFormat.get("value");
	}

	@JsonProperty("payloadType")
    private void unpackNestedPayloadType(Map<String,Object> payloadType) {
		Map<String, Object> coding = (Map<String, Object>) payloadType.get("coding");
		Map<String, String> system = (Map<String, String>)coding.get("system");
		this.payloadType = system.get("value");
	}

	@JsonProperty("connectionType")
    private void unpackNestedConnectionType(Map<String,Object> connectionType) {
		Map<String, String> code = (Map<String, String>) connectionType.get("code");
		this.connectionType = code.get("value");
	}
	@JsonProperty("identifier")
    private void unpackNestedId(List<Object> identifier) {
		Map<String, Object> idMap = (Map<String, Object>) identifier.get(0);
		Map<String, String> idMap2 = (Map<String, String>) idMap.get("value");
		this.id = idMap2.get("value");
	}
}
