package gov.ca.emsa.pulse.sequoia.domain;

import java.util.ArrayList;
import java.util.List;

public class SequoiaLocation {
	private SequoiaLocationResource resource;
	private String fullUrl;
	private SequoiaSearch search;
	
	public SequoiaLocation() {
	}
	
	public SequoiaLocationResource getResource() {
		return resource;
	}
	public void setResource(SequoiaLocationResource resource) {
		this.resource = resource;
	}
	public String getFullUrl() {
		return fullUrl;
	}
	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}
	public SequoiaSearch getSearch() {
		return search;
	}
	public void setSearch(SequoiaSearch search) {
		this.search = search;
	}
}
