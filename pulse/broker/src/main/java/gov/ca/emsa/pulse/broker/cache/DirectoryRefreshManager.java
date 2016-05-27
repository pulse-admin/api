package gov.ca.emsa.pulse.broker.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;

import gov.ca.emsa.pulse.broker.domain.Organization;
import gov.ca.emsa.pulse.broker.manager.OrganizationManager;

import org.springframework.web.client.RestTemplate;

public class DirectoryRefreshManager extends TimerTask {
	private OrganizationManager organizationManager;
	private String directoryServicesUrl;
	private long expirationMillis;
	
	public void getDirectories(){
		System.out.println("Updating the directories...");
		RestTemplate restTemplate = new RestTemplate();
		Organization[] orgs = restTemplate.getForObject(directoryServicesUrl, Organization[].class);
		ArrayList<Organization> orgList = new ArrayList<Organization>(Arrays.asList(orgs));
		organizationManager.updateOrganizations(orgList);
	}

	@Override
	public void run() {
		getDirectories();
	}

	public void setExpirationMillis(long directoryRefreshExpirationMillis) {
		this.expirationMillis = directoryRefreshExpirationMillis;
	}
	
	public long getExpirationMillis() {
		return expirationMillis;
	}
	
	public void setManager(OrganizationManager orgMan){
		this.organizationManager = orgMan;
	}

	public String getDirectoryServicesUrl() {
		return directoryServicesUrl;
	}

	public void setDirectoryServicesUrl(String directoryServicesUrl) {
		this.directoryServicesUrl = directoryServicesUrl;
	}
}