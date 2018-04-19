package gov.ca.emsa.pulse.sequoia.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaEndpoint {
	
	private SequoiaEndpointResource endpoint;
	
	public SequoiaEndpointResource getEndpoint() {
		return endpoint;
	}
	@JsonProperty("Endpoint")
	public void setEndpoint(SequoiaEndpointResource endpoint) {
		this.endpoint = endpoint;
	}
}
