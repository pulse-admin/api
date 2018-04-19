package gov.ca.emsa.pulse.sequoia.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaPartOf {
	
	private SequoiaPartOfReference reference;

	public SequoiaPartOfReference getReference() {
		return reference;
	}
	
	@JsonProperty("Reference")
	public void setReference(SequoiaPartOfReference reference) {
		this.reference = reference;
	}

}
