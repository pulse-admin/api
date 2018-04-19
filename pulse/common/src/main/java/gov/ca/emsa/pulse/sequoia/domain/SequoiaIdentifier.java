package gov.ca.emsa.pulse.sequoia.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaIdentifier {
	private String use;
	private String system;
	private String value;
	private String type;
	
	public SequoiaIdentifier() {
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	@JsonProperty("use")
    private void unpackNestedUse(Map<String,String> use) {
		this.use = use.get("value");
	}
	@SuppressWarnings("unchecked")
	@JsonProperty("system")
    private void unpackNestedSystem(Map<String,String> system) {
		this.system = system.get("value");
	}
	@SuppressWarnings("unchecked")
	@JsonProperty("value")
    private void unpackNestedValue(Map<String,String> value) {
		this.value = value.get("value");
	}
	@SuppressWarnings("unchecked")
	@JsonProperty("type")
    private void unpackNestedType(Map<String,String> type) {
		this.type = type.get("value");
	}
}
