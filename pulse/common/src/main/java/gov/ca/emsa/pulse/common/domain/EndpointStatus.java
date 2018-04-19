package gov.ca.emsa.pulse.common.domain;

public class EndpointStatus {
	private Long id;
	private String name;
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
	@Override
	public String toString() {
		return "EndpointStatus [id=" + id + ", name=" + name + "]";
	}
	
	
}
