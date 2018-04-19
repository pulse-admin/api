package gov.ca.emsa.pulse.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "acfs")
@RestController
@RequestMapping("/acfs")
public class AlternateCareFacilityService {
    @Value("${acfWritesAllowed}")
    private Boolean acfWritesAllowed;

    @Autowired
    AlternateCareFacilityManager acfManager;
    @Autowired
    AuditEventManager auditManager;

    @ApiOperation(value = "Get the list of all alternate care facilities (ACFs)")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROVIDER"
    })
    public List<AlternateCareFacility> getAll() {
        CommonUser user = UserUtil.getCurrentUser();
        Long userStateOrg = user.getLiferayStateId();
        // auditManager.addAuditEntry(QueryType.GET_ALL_ACFS, "/acfs",
        // user.getSubjectName());
        List<AlternateCareFacilityDTO> dtos = acfManager.getAll();
        List<AlternateCareFacility> results = new ArrayList<AlternateCareFacility>();
        for (AlternateCareFacilityDTO dto : dtos) {
            if (user.isRoleAdmin() || (dto.getLiferayStateId() != null && dto.getLiferayStateId().equals(userStateOrg)))
                results.add(DtoToDomainConverter.convert(dto));
        }
        return results;
    }

    @ApiOperation(value = "Get information about a specific ACF")
    @RequestMapping(value = "/{acfId}", method = RequestMethod.GET)
    @PreAuthorize("hasPermissionForAcf(#acfId)")
    public AlternateCareFacility getById(@PathVariable("acfId") Long acfId) {
        CommonUser user = UserUtil.getCurrentUser();
        // auditManager.addAuditEntry(QueryType.GET_ACF_BY_ID, "/acfs" + acfId,
        // user.getSubjectName());
        AlternateCareFacilityDTO dto = acfManager.getById(acfId);
        return DtoToDomainConverter.convert(dto);
    }

    @ApiOperation(value = "Create a new ACF")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROVIDER"
    })
    public AlternateCareFacility create(@RequestBody(required = true) AlternateCareFacility toCreate)
            throws InvalidArgumentsException, AcfChangesNotAllowedException, SQLException {
        if (acfWritesAllowed != null && acfWritesAllowed.booleanValue() == false) {
            throw new AcfChangesNotAllowedException("Alternate Care Facilities may not be added to the system.");
        }

        CommonUser user = UserUtil.getCurrentUser();
        // auditManager.addAuditEntry(QueryType.CREATE_ACF, "/create",
        // user.getSubjectName());
        Long jwtAcfId = user.getLiferayAcfId();
        Long jwtStateId = user.getLiferayStateId();

        toCreate.setLiferayAcfId(jwtAcfId);
        toCreate.setLiferayStateId(jwtStateId);

        AlternateCareFacilityDTO dto = DomainToDtoConverter.convert(toCreate);
        if (StringUtils.isEmpty(dto.getIdentifier())) {
            throw new InvalidArgumentsException("ACF name is required.");
        }
        AlternateCareFacilityDTO created = acfManager.create(dto);
        return DtoToDomainConverter.convert(created);
    }

    @ApiOperation(value = "Edit an existing ACF")
    @RequestMapping(value = "/{acfId}/edit", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN"
    })
    public AlternateCareFacility update(@PathVariable("acfId") Long acfId,
            @RequestBody(required = true) AlternateCareFacility toUpdate)
            throws InvalidArgumentsException, PermissionDeniedException, AcfChangesNotAllowedException, SQLException {

        CommonUser user = UserUtil.getCurrentUser();

        AlternateCareFacilityDTO acfToUpdate = DomainToDtoConverter.convert(toUpdate);
        // if writes aren't allowed check if the name is different
        if (acfWritesAllowed != null && acfWritesAllowed.booleanValue() == false) {
            AlternateCareFacilityDTO existingAcf = acfManager.getById(acfId);
            if (existingAcf.getIdentifier() == null || acfToUpdate.getIdentifier() == null
                    || !existingAcf.getIdentifier().equals(acfToUpdate.getIdentifier())) {
                throw new AcfChangesNotAllowedException("Alternate Care Facility names cannot be changed.");
            }
        }

        if (acfToUpdate.getId() == null) {
            throw new InvalidArgumentsException("An ACF id is required in the body of the update request.");
        }
        AlternateCareFacilityDTO updated = acfManager.updateAcfDetails(acfToUpdate);
        return DtoToDomainConverter.convert(updated);
    }
}
