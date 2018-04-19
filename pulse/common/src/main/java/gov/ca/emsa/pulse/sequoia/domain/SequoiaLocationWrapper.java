package gov.ca.emsa.pulse.sequoia.domain;

import java.util.ArrayList;
import java.util.List;

public class SequoiaLocationWrapper {
	private String type;
	private Integer total;
	private String resourceType;
	private List<SequoiaLink> link;
	private List<SequoiaLocation> entry;
	
	public SequoiaLocationWrapper() {
		link = new ArrayList<SequoiaLink>();
		entry = new ArrayList<SequoiaLocation>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public List<SequoiaLink> getLink() {
		return link;
	}

	public void setLink(List<SequoiaLink> link) {
		this.link = link;
	}

	public List<SequoiaLocation> getEntry() {
		return entry;
	}

	public void setEntry(List<SequoiaLocation> entry) {
		this.entry = entry;
	}
}
