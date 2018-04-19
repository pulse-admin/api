package gov.ca.emsa.pulse.broker.dao;

import gov.ca.emsa.pulse.broker.domain.EndpointStatusEnum;
import gov.ca.emsa.pulse.broker.domain.EndpointTypeEnum;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.entity.EndpointEntity;

import java.util.List;

public interface EndpointDAO {
	
	public EndpointDTO create(EndpointDTO endpoint);
	public EndpointDTO update(EndpointDTO endpoint);
	public List<EndpointEntity> getAllEntities();
	public List<EndpointDTO> findAll();
	public List<EndpointDTO> findAllOfType(List<EndpointTypeEnum> types);
	public List<EndpointDTO> findAllByStatusAndType(List<EndpointStatusEnum> statuses, List<EndpointTypeEnum> types);
	public void delete(EndpointDTO endpointDto);
	public EndpointDTO findById(Long id);
	public EndpointDTO findByExternalId(String externalId);
	public EndpointDTO findByLocationIdAndType(Long locationId, List<EndpointStatusEnum> status, EndpointTypeEnum type);
	public EndpointDTO findByManagingOrganizationAndType(String managingOrganizationName, List<EndpointStatusEnum> status, EndpointTypeEnum type);
	EndpointDTO findByOrganizationIdAndType(String organizationId,
			List<EndpointStatusEnum> statuses, EndpointTypeEnum type);
	public EndpointDTO findByUrl(String url);
}
