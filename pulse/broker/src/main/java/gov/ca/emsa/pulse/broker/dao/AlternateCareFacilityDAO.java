package gov.ca.emsa.pulse.broker.dao;

import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;

public interface AlternateCareFacilityDAO {
	public AlternateCareFacilityDTO create(AlternateCareFacilityDTO dto) throws SQLException, EntityExistsException;
	public AlternateCareFacilityDTO update(AlternateCareFacilityDTO dto) throws SQLException;
	public void delete(Long id) throws SQLException;
	public List<AlternateCareFacilityDTO> findAll();	
	public AlternateCareFacilityDTO getById(Long id);
	public List<AlternateCareFacilityDTO> getByIdentifier(String name);
	public List<AlternateCareFacilityDTO> getByName(String name);
	public void deleteItemsOlderThan(Date oldestItem) throws SQLException;
	public List<AlternateCareFacilityDTO> getByLiferayAcfId(Long acfId);
}
