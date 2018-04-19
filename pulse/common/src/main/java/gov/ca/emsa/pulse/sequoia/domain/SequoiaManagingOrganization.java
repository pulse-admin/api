package gov.ca.emsa.pulse.sequoia.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaManagingOrganization {
	private String display;
	private String reference;
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	@SuppressWarnings("unchecked")
	@JsonProperty("reference")
    private void unpackNestedAddress(Map<String, String> reference) {
		this.reference = reference.get("value");
	}
}
