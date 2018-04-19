package gov.ca.emsa.pulse.sequoia.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = {"xmlns", "id", "meta", "resourceType"})
public class SequoiaOrganizationWrapper {
	private String type;
	private Integer total;
	private List<SequoiaLink> link;
	private List<SequoiaEntry> entry;
	
	public SequoiaOrganizationWrapper() {
		link = new ArrayList<SequoiaLink>();
		entry = new ArrayList<SequoiaEntry>();
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
	public List<SequoiaLink> getLink() {
		return link;
	}

	public void setLink(List<SequoiaLink> link) {
		this.link = link;
	}

	public List<SequoiaEntry> getEntry() {
		return entry;
	}

	public void setEntry(List<SequoiaEntry> entry) {
		this.entry = entry;
	}
	
	@JsonProperty("type")
    private void unpackNestedType(Map<String,Object> type) {
        this.type = (String)type.get("value");
    }
	
    @JsonProperty("total")
    private void unpackNestedTotal(Map<String,Integer> total) {
        this.total = (Integer)total.get("value");
    }
    
    @JsonProperty("link")
    private void unpackNestedLink(Map<String,Object> link) {
        this.link = (List<SequoiaLink>)link.get("link");
    }
	
    @JsonProperty("entry")
    private void unpackNestedEntry(List<SequoiaEntry> entry) {
        this.entry = entry;
    }
}
