package gov.ca.emsa.pulse.sequoia.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SequoiaPosition {
	
	private String latitude;
	private String longitude;
	
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	@JsonProperty("latitude")
    private void unpackNestedLatitude(Map<String,String> latitude) {
		this.latitude = latitude.get("value");
    }
	@JsonProperty("longitude")
    private void unpackNestedLongitude(Map<String,String> longitude) {
		this.longitude = longitude.get("value");
    }
}
