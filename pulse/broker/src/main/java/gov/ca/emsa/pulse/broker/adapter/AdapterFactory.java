package gov.ca.emsa.pulse.broker.adapter;

import gov.ca.emsa.pulse.broker.dto.EndpointDTO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdapterFactory {
	private static final Logger logger = LogManager.getLogger(AdapterFactory.class);
	@Autowired private EHealthAdapter ehealthAdapter;
	@Autowired private CareQualityAdapter careQualityAdapter;
	
	public Adapter getAdapter(EndpointDTO endpoint) {
		if(StringUtils.isEmpty(endpoint.getAdapter())) {
			return null;
		}
		
		if(endpoint.getAdapter().toUpperCase().contains("EHEALTH")) {
			return getEhealthAdapter();
		} else if(endpoint.getAdapter().toUpperCase().contains("CAREQUALITY")){
			return getCareQualityAdapter();
		}else {
			logger.error("Could not find a matching adapter for endpoint type : " + endpoint.getAdapter());
		}
		return null;
	}

	public EHealthAdapter getEhealthAdapter() {
		return ehealthAdapter;
	}

	public void setEhealthAdapter(EHealthAdapter ehealthAdapter) {
		this.ehealthAdapter = ehealthAdapter;
	}

	public CareQualityAdapter getCareQualityAdapter() {
		return careQualityAdapter;
	}

	public void setCareQualityAdapter(CareQualityAdapter careQualityAdapter) {
		this.careQualityAdapter = careQualityAdapter;
	}
	
}
