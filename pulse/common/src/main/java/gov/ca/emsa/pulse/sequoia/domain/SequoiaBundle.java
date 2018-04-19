package gov.ca.emsa.pulse.sequoia.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"xmlns", "id", "meta", "resourceType"})
public class SequoiaBundle {
	public SequoiaOrganizationWrapper Bundle;

	public SequoiaOrganizationWrapper getBundle() {
		return Bundle;
	}

	public void setBundle(SequoiaOrganizationWrapper bundle) {
		Bundle = bundle;
	}
}
