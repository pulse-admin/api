package gov.ca.emsa.pulse.sequoia.domain;

import java.util.ArrayList;
import java.util.List;

public class SequoiaResourceType {
	private List<SequoiaResourceCoding> coding;
	
	public SequoiaResourceType() {
		this.coding = new ArrayList<SequoiaResourceCoding>();
	}
	public List<SequoiaResourceCoding> getCoding() {
		return coding;
	}
	public void setCoding(List<SequoiaResourceCoding> coding) {
		this.coding = coding;
	}
}
