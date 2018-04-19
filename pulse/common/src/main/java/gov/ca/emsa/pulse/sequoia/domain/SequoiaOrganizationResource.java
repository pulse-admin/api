package gov.ca.emsa.pulse.sequoia.domain;

import gov.ca.emsa.pulse.sequoia.domain.SequoiaEndpoint;
import gov.ca.emsa.pulse.sequoia.domain.SequoiaResourceMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaOrganizationResource {
	private SequoiaOrganization organization;

	public SequoiaOrganization getOrganization() {
		return organization;
	}
	
	@JsonProperty("Organization")
	public void setOrganization(SequoiaOrganization organization) {
		this.organization = organization;
	}
}
