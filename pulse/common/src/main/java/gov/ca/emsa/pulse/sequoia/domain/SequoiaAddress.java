package gov.ca.emsa.pulse.sequoia.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaAddress {
	private String type;
	private String text;
	private String line;
	private String city;
	private String state;
	private String country;
	private String postalCode;
	private String resourceType;
	private String use;
	private SequoiaPosition position;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public SequoiaPosition getPosition() {
		return position;
	}

	public void setPosition(SequoiaPosition position) {
		this.position = position;
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
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getUse() {
		return use;
	}
	public void setUse(String use) {
		this.use = use;
	}
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@JsonProperty("use")
    private void unpackNestedUse(Map<String,String> use) {
		this.use = use.get("value");
    }
	@JsonProperty("type")
    private void unpackNestedType(Map<String,String> type) {
		this.type = type.get("value");
    }
	@JsonProperty("line")
    private void unpackNestedLine(Map<String,String> line) {
		this.line = line.get("value");
    }
	@JsonProperty("city")
    private void unpackNestedCity(Map<String,String> city) {
		this.city = city.get("value");
    }
	@JsonProperty("state")
    private void unpackNestedState(Map<String,String> state) {
		this.state = state.get("value");
    }
	@JsonProperty("postalCode")
    private void unpackNestedPostalCode(Map<String,String> postalCode) {
		this.postalCode = postalCode.get("value");
    }
	@JsonProperty("country")
    private void unpackNestedCountry(Map<String,String> country) {
		this.country = country.get("value");
    }
	@JsonProperty("position")
    private void unpackNestedType(SequoiaPosition position) {
		this.position = position;
    }
}
