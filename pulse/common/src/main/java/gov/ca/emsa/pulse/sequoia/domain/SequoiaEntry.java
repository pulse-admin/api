package gov.ca.emsa.pulse.sequoia.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaEntry {
	private SequoiaOrganizationResource resource;
	private String fullUrl;
	public SequoiaOrganizationResource getResource() {
		return resource;
	}
	public void setResource(SequoiaOrganizationResource resource) {
		this.resource = resource;
	}
	public String getFullUrl() {
		return fullUrl;
	}
	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}
	@JsonProperty("fullUrl")
    private void unpackNestedFullUrl(Map<String,Object> fullUrl) {
        this.fullUrl = (String)fullUrl.get("value");
    }
}
