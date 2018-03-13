package gov.ca.emsa.pulse.common.domain;

public class EndpointType {
	private Long id;
	private String name;
	private String code;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return "EndpointType [id=" + id + ", name=" + name + ", code=" + code
				+ "]";
	}
	
	
}
