package gov.ca.emsa.pulse.sequoia.domain;

public class SequoiaLocationIdentifier {
	private String use;
	private String system;
	private String value;
	private SequoiaResourceType type;
	
	public SequoiaLocationIdentifier() {
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SequoiaResourceType getType() {
		return type;
	}

	public void setType(SequoiaResourceType type) {
		this.type = type;
	}
}
