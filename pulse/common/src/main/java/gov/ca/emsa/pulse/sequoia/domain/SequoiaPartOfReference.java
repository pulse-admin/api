package gov.ca.emsa.pulse.sequoia.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaPartOfReference {
	
	private SequoiaIdentifier identifier;

	public SequoiaIdentifier getIdentifier() {
		return identifier;
	}
	
	@JsonProperty("identifier")
	public void setIdentifier(SequoiaIdentifier identifier) {
		this.identifier = identifier;
	}
}
