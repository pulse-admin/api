package gov.ca.emsa.pulse.broker.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.ca.emsa.pulse.broker.dao.AlternateCareFacilityAddressLineDAO;
import gov.ca.emsa.pulse.broker.dao.AlternateCareFacilityDAO;
import gov.ca.emsa.pulse.broker.dao.PatientDAO;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.entity.AlternateCareFacilityAddressLineEntity;
import gov.ca.emsa.pulse.broker.entity.AlternateCareFacilityEntity;

@Repository
public class AlternateCareFacilityDAOImpl extends BaseDAOImpl implements AlternateCareFacilityDAO {
	private static final Logger logger = LogManager.getLogger(AlternateCareFacilityDAOImpl.class);

	@Autowired PatientDAO patientDao;
	@Autowired AlternateCareFacilityAddressLineDAO addressLineDao;
	
	@Override
	public AlternateCareFacilityDTO create(AlternateCareFacilityDTO dto) 
			throws SQLException, EntityExistsException {
		List<AlternateCareFacilityEntity> duplicates = getEntityByIdentifier(dto.getIdentifier()); 
		//we restrict ACFs to have unique names, so first check
		//to see if there is already one with this name. if so throw an exception
		if(duplicates != null && duplicates.size() > 0) {
			logger.info("Attempt to create a duplicate ACF named " + dto.getIdentifier());
			throw new EntityExistsException("An ACF named " + dto.getIdentifier() + " already exists.");
		}
		
		AlternateCareFacilityEntity toInsert = new AlternateCareFacilityEntity();
		toInsert.setLiferayStateId(dto.getLiferayStateId());
		toInsert.setLiferayAcfId(dto.getLiferayAcfId());
		toInsert.setIdentifier(dto.getIdentifier());
		toInsert.setName(dto.getName());
		toInsert.setPhoneNumber(dto.getPhoneNumber());
		toInsert.setLastReadDate(new Date());
		toInsert.setCity(dto.getCity());
		toInsert.setState(dto.getState());
		toInsert.setZipcode(dto.getZipcode());
		entityManager.persist(toInsert);
		entityManager.flush();
		
		if(dto.getLines() != null && dto.getLines().size() > 0) {
			for(int i = 0; i < dto.getLines().size(); i++) {
				AlternateCareFacilityAddressLineEntity addrLine = new AlternateCareFacilityAddressLineEntity();
				addrLine.setAcfId(toInsert.getId());
				addrLine.setOrder(i);
				addrLine.setLine(dto.getLines().get(i).getLine());
				entityManager.persist(addrLine);
				entityManager.flush();
				toInsert.getLines().add(addrLine);
			}
		}
		return new AlternateCareFacilityDTO(toInsert);
	}

	@Override
	public AlternateCareFacilityDTO update(AlternateCareFacilityDTO dto) throws SQLException {
		AlternateCareFacilityEntity toUpdate = this.getEntityById(dto.getId());
		toUpdate.setLiferayStateId(dto.getLiferayStateId());
		toUpdate.setLiferayAcfId(dto.getLiferayAcfId());
		toUpdate.setIdentifier(dto.getIdentifier());
		toUpdate.setName(dto.getName());
		toUpdate.setPhoneNumber(dto.getPhoneNumber());
		toUpdate.setLastReadDate(dto.getLastReadDate());
		toUpdate.setCity(dto.getCity());
		toUpdate.setState(dto.getState());
		toUpdate.setZipcode(dto.getZipcode());
		toUpdate = entityManager.merge(toUpdate);
		entityManager.flush();
		
		//delete all address lines and re-add them
		addressLineDao.deleteAllForAcf(dto.getId());
		toUpdate.getLines().clear();
		if(dto.getLines() != null && dto.getLines().size() > 0) {
			for(int i = 0; i < dto.getLines().size(); i++) {
				AlternateCareFacilityAddressLineEntity addrLine = new AlternateCareFacilityAddressLineEntity();
				addrLine.setAcfId(toUpdate.getId());
				addrLine.setOrder(i);
				addrLine.setLine(dto.getLines().get(i).getLine());	
				entityManager.persist(addrLine);
				entityManager.flush();
				toUpdate.getLines().add(addrLine);
			}
		}
		
		return new AlternateCareFacilityDTO(toUpdate);
	}

	@Override
	public void delete(Long id) throws SQLException {
		AlternateCareFacilityEntity toDelete = getEntityById(id);
		entityManager.remove(toDelete);
		entityManager.flush();
	}

	@Override
	public List<AlternateCareFacilityDTO> findAll() {
		List<AlternateCareFacilityEntity> result = this.findAllEntities();
		List<AlternateCareFacilityDTO> dtos = new ArrayList<AlternateCareFacilityDTO>(result.size());
		for(AlternateCareFacilityEntity entity : result) {
			AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public AlternateCareFacilityDTO getById(Long id) {
		
		AlternateCareFacilityDTO dto = null;
		AlternateCareFacilityEntity ae = this.getEntityById(id);
		
		if (ae != null){
			dto = new AlternateCareFacilityDTO(ae); 
		}
		return dto;
	}

	@Override
	public List<AlternateCareFacilityDTO> getByIdentifier(String identifier) {
		List<AlternateCareFacilityEntity> results = this.getEntityByIdentifier(identifier);
		List<AlternateCareFacilityDTO> dtos = new ArrayList<AlternateCareFacilityDTO>();
		for(AlternateCareFacilityEntity entity : results) {
			AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<AlternateCareFacilityDTO> getByLiferayAcfId(Long acfId) {
		List<AlternateCareFacilityEntity> results = this.getEntityByLiferayAcfId(acfId);
		List<AlternateCareFacilityDTO> dtos = new ArrayList<AlternateCareFacilityDTO>();
		for(AlternateCareFacilityEntity entity : results) {
			AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<AlternateCareFacilityDTO> getByName(String name) {
		List<AlternateCareFacilityEntity> results = this.getEntityByName(name);
		List<AlternateCareFacilityDTO> dtos = new ArrayList<AlternateCareFacilityDTO>();
		for(AlternateCareFacilityEntity entity : results) {
			AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public void deleteItemsOlderThan(Date oldestItem) throws SQLException {
		Query query = entityManager.createQuery( "SELECT acf "
				+ "FROM AlternateCareFacilityEntity acf "
				+ " WHERE acf.lastReadDate <= :cacheDate");
		
		query.setParameter("cacheDate", oldestItem);
		List<AlternateCareFacilityEntity> oldAcfs = query.getResultList();
		if(oldAcfs != null && oldAcfs.size() > 0) {
			for(AlternateCareFacilityEntity oldAcf : oldAcfs) {
				entityManager.remove(oldAcf);
				logger.info("Deleted ACF with ID " + oldAcf.getId() + " and name " + oldAcf.getIdentifier() + " during ACF cleanup.");
			}
		} else {
			logger.info("Deleted 0 ACFs from the cache.");
		}
	}
	
	private List<AlternateCareFacilityEntity> findAllEntities() {
		Query query = entityManager.createQuery("SELECT DISTINCT a "
				+ "FROM AlternateCareFacilityEntity a "
				+ "LEFT OUTER JOIN FETCH a.lines lines "
				+ "ORDER BY lines.order");
		return query.getResultList();
	}
	
	private AlternateCareFacilityEntity getEntityById(Long id) {
		AlternateCareFacilityEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT distinct a "
				+ "FROM AlternateCareFacilityEntity a "
				+ "LEFT JOIN FETCH a.lines lines "
				+ "where a.id = :entityid "
				+ "ORDER BY lines.order", AlternateCareFacilityEntity.class );
		query.setParameter("entityid", id);
		
		List<AlternateCareFacilityEntity> result = query.getResultList();
		if(result.size() != 0) {
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private List<AlternateCareFacilityEntity> getEntityByIdentifier(String identifier) {
		Query query = entityManager.createQuery( "SELECT DISTINCT a "
				+ "FROM AlternateCareFacilityEntity a "
				+ "LEFT OUTER JOIN FETCH a.lines lines "
				+ "where (a.identifier LIKE :identifier)"
				+ "ORDER BY lines.order", AlternateCareFacilityEntity.class );
		query.setParameter("identifier", identifier);
		return query.getResultList();
	}
	
	private List<AlternateCareFacilityEntity> getEntityByName(String name) {
		Query query = entityManager.createQuery( "SELECT DISTINCT a "
				+ "FROM AlternateCareFacilityEntity a "
				+ "LEFT OUTER JOIN FETCH a.lines lines "
				+ "where (a.name LIKE :name)"
				+ "ORDER BY lines.order", AlternateCareFacilityEntity.class );
		query.setParameter("name", name);
		return query.getResultList();
	}
	
	private List<AlternateCareFacilityEntity> getEntityByLiferayAcfId(Long acfId) {
		Query query = entityManager.createQuery( "SELECT DISTINCT a "
				+ "FROM AlternateCareFacilityEntity a "
				+ "where (a.liferayAcfId = :identifier)"
				, AlternateCareFacilityEntity.class );
		query.setParameter("identifier", acfId);
		return query.getResultList();
	}
	
}
