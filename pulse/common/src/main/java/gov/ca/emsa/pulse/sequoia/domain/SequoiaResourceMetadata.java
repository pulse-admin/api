package gov.ca.emsa.pulse.sequoia.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaResourceMetadata {
	private String lastUpdated;
	private String versionId;
	public String getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public String getVersionId() {
		return versionId;
	}
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
	@SuppressWarnings("unchecked")
	@JsonProperty("lastUpdated")
    private void unpackNestedUpdated(Map<String, String> lastUpdated) {
		this.lastUpdated = lastUpdated.get("value");
	}
	@SuppressWarnings("unchecked")
	@JsonProperty("versionId")
    private void unpackNestedVersionId(Map<String, String> versionId) {
		this.versionId = versionId.get("value");
	}
}
