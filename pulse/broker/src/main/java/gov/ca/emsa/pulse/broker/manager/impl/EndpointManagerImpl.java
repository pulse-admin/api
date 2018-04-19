package gov.ca.emsa.pulse.broker.manager.impl;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.ca.emsa.pulse.broker.dao.EndpointDAO;
import gov.ca.emsa.pulse.broker.dao.LocationEndpointMapDAO;
import gov.ca.emsa.pulse.broker.domain.EndpointStatusEnum;
import gov.ca.emsa.pulse.broker.domain.EndpointTypeEnum;
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.LocationEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.EndpointManager;
import gov.ca.emsa.pulse.common.domain.Endpoint;

@Service
public class EndpointManagerImpl implements EndpointManager {
	private static final Logger logger = LogManager.getLogger(EndpointManagerImpl.class);

	@Autowired private EndpointDAO endpointDao;
	@Autowired private LocationEndpointMapDAO mappingDao;
	
	@Transactional(readOnly = true)
	public EndpointDTO getById(Long id) {
		return endpointDao.findById(id);
	}
	
	@Transactional(readOnly = true)
	public EndpointDTO getByExternalId(String id) {
		return endpointDao.findByExternalId(id);
	}
	
	@Transactional(readOnly = true) 
	public List<EndpointDTO> getByType(List<EndpointTypeEnum> types) {
		return endpointDao.findAllOfType(types);
	}
	
	@Transactional(readOnly = true) 
	public List<EndpointDTO> getByStatusAndType(List<EndpointStatusEnum> statuses, List<EndpointTypeEnum> types) {
		return endpointDao.findAllByStatusAndType(statuses, types);
	}
	
	@Transactional
	public void updateEndpoints(List<Endpoint> newEndpoints){
		List<EndpointDTO> currentEndpoints = getAll();
		
		//look at the old endpoints for anything not in the new endpoints and delete
		for(EndpointDTO oldEndpoint : currentEndpoints) {
			boolean stillExists = false;
			for(Endpoint newEndpoint : newEndpoints) {
				if(oldEndpoint.getUrl().equals(newEndpoint.getUrl())) {
					stillExists = true;
				}
			}
			
			if(!stillExists) {
				endpointDao.delete(oldEndpoint);
			}
		}
		
		//look at the new endpoints for anything that should be created or updated
		for(Endpoint newEndpoint : newEndpoints) {
			String url = newEndpoint.getUrl();
			EndpointDTO existingEndpoint = endpointDao.findByUrl(url);
			if(existingEndpoint == null) {
				try {
					EndpointDTO toCreate = DomainToDtoConverter.convert(newEndpoint);
					endpointDao.create(toCreate);
				} catch(Exception ex) {
					logger.error("Error creating endpoint with url " + url, ex);
					ex.printStackTrace();
				}
			} else {
				try {
					EndpointDTO toUpdate = DomainToDtoConverter.convert(newEndpoint);
					toUpdate.setId(existingEndpoint.getId());
					endpointDao.update(toUpdate);
				} catch(Exception ex) {
					logger.error("Error updating endpoint with id " + existingEndpoint.getId(), ex);
					ex.printStackTrace();
				}
			}
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<EndpointDTO> getAll() {
		List<EndpointDTO> results = endpointDao.findAll();
		return results;
	}
	
	@Override
	@Transactional
	public void updateEndpointLocationMappings(List<LocationEndpointMapDTO> newMappings) {
		for(LocationEndpointMapDTO oldMapping : mappingDao.findAll()) {
			boolean stillExists = false;
			for(LocationEndpointMapDTO newMapping : newMappings) {
				if(oldMapping.getLocationId().longValue() == newMapping.getLocationId().longValue() && 
					oldMapping.getEndpointId().longValue() == newMapping.getEndpointId().longValue()) {
					stillExists = true;
				}
			}
			
			if(!stillExists) {
				mappingDao.delete(oldMapping.getId());
			}
		}
		
		//look at the new mappings for anything that should be created
		for(LocationEndpointMapDTO newMapping : newMappings) {
			if(!mappingDao.exists(newMapping.getLocationId(), newMapping.getEndpointId())) {
				mappingDao.create(newMapping.getLocationId(), newMapping.getEndpointId());
			}
		}
	}
}
