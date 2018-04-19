package gov.ca.emsa.pulse.broker.manager.impl;

import gov.ca.emsa.pulse.broker.cache.CacheCleanupException;
import gov.ca.emsa.pulse.broker.dao.AlternateCareFacilityDAO;
import gov.ca.emsa.pulse.broker.domain.AuditType;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class AlternateCareFacilityManagerImpl implements AlternateCareFacilityManager {
	@Value("${acfWritesAllowed}")
	private Boolean acfWritesAllowed;
	
	@Autowired AlternateCareFacilityDAO acfDao;
	@Autowired private AuditEventManager auditManager;
	private static final Logger logger = LogManager.getLogger(AlternateCareFacilityManagerImpl.class);

	@Override
	public List<AlternateCareFacilityDTO> getAll() {
		return acfDao.findAll();
	}

	@Override
	@Transactional
	public AlternateCareFacilityDTO create(AlternateCareFacilityDTO toCreate) throws SQLException {
		AlternateCareFacilityDTO result = null;
		
		try {
			result = acfDao.create(toCreate);
		} catch(EntityExistsException ex) {
			result = getByIdentifier(toCreate.getIdentifier());
		}
		
		return result;
	}

	@Override
	@Transactional
	public AlternateCareFacilityDTO updateAcfDetails(AlternateCareFacilityDTO toUpdate) throws SQLException {
		toUpdate.setLastReadDate(new Date());
		AlternateCareFacilityDTO after = acfDao.update(toUpdate);
		
		// only insert something if we are changing more than just the last read date - that gets changed A LOT
		try {
			auditManager.createPulseAuditEvent(AuditType.FE, toUpdate.getId());
		} catch(JsonProcessingException ex) {
			logger.error("Error adding audit event for ACF edit. ACF id " + toUpdate.getId(), ex);
		}
		return after;
	}
	
	@Override
	@Transactional
	public AlternateCareFacilityDTO updateLastModifiedDate(Long acfId) throws SQLException {
		AlternateCareFacilityDTO toUpdate = acfDao.getById(acfId);
		toUpdate.setLastReadDate(new Date());
		AlternateCareFacilityDTO after = acfDao.update(toUpdate);
		return after;
	}

	@Override
	@Transactional
	public AlternateCareFacilityDTO getById(Long id) {
		AlternateCareFacilityDTO result = acfDao.getById(id);
		return result;
	}

	@Override
	public AlternateCareFacilityDTO getByIdentifier(String identifier) {
		List<AlternateCareFacilityDTO> matches = acfDao.getByIdentifier(identifier);
		
		AlternateCareFacilityDTO result = null;
		if(matches != null && matches.size() > 0) {
			result = matches.get(0);
		}
		return result;
	}
	
	@Override
	public List<AlternateCareFacilityDTO> getByName(String name) {
		List<AlternateCareFacilityDTO> matches = acfDao.getByName(name);
		return matches;
	}
	
	@Override
	@Transactional
	public void cleanupCache(Date oldestAllowed) throws CacheCleanupException {
		if(acfWritesAllowed != null && acfWritesAllowed.booleanValue() == true) {
			try {
				acfDao.deleteItemsOlderThan(oldestAllowed);
			} catch(SQLException sql) {
				throw new CacheCleanupException("Error updating the database. Message is: " + sql.getMessage());
			} catch(Exception ex) {
				throw new CacheCleanupException(ex.getMessage());
			}
		}
	}

	@Override
	public AlternateCareFacilityDTO getByLiferayAcfId(Long liferayAcfId) {
		List<AlternateCareFacilityDTO> matches = acfDao.getByLiferayAcfId(liferayAcfId);
		AlternateCareFacilityDTO result = null;
		if(matches != null && matches.size() > 0) {
			result = matches.get(0);
		}
		return result;
	}
}
