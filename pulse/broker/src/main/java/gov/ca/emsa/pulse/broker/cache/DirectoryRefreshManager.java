package gov.ca.emsa.pulse.broker.cache;

import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DirectoryRefreshManager extends TimerTask {
	private static final Logger logger = LogManager.getLogger(DirectoryRefreshManager.class);
	private long directoryRefreshExpirationMillis;
	
	private CtenDirectoryRefreshManager ctenDirectoryManager;
	private SequoiaDirectoryRefreshManager sequoiaDirectoryManager;
	
	@Override
	public void run() {
		try {
			if(ctenDirectoryManager.getStatus().equals("Active")){
				ctenDirectoryManager.getCtenLocationsAndEndpoints();
			}
			if(sequoiaDirectoryManager.getStatus().equals("Active")){
				sequoiaDirectoryManager.getSequoiaLocationsAndEndpoints();
			}
		} catch(Exception ex) {
			logger.error("Error updating location cache", ex);
			ex.printStackTrace();
		}
	}

	public long getDirectoryRefreshExpirationMillis() {
		return directoryRefreshExpirationMillis;
	}

	public void setDirectoryRefreshExpirationMillis(
			long directoryRefreshExpirationMillis) {
		this.directoryRefreshExpirationMillis = directoryRefreshExpirationMillis;
	}

	public CtenDirectoryRefreshManager getCtenDirectoryManager() {
		return ctenDirectoryManager;
	}

	public void setCtenDirectoryManager(
			CtenDirectoryRefreshManager ctenDirectoryManager) {
		this.ctenDirectoryManager = ctenDirectoryManager;
	}

	public SequoiaDirectoryRefreshManager getSequoiaDirectoryManager() {
		return sequoiaDirectoryManager;
	}

	public void setSequoiaDirectoryManager(
			SequoiaDirectoryRefreshManager sequoiaDirectoryManager) {
		this.sequoiaDirectoryManager = sequoiaDirectoryManager;
	}
}
