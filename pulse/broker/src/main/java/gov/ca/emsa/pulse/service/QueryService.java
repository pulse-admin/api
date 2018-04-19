package gov.ca.emsa.pulse.service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.domain.AuditType;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.dto.QueryDTO;
import gov.ca.emsa.pulse.broker.dto.QueryEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.manager.QueryManager;
import gov.ca.emsa.pulse.broker.saml.SamlUtil;
import gov.ca.emsa.pulse.common.domain.CreatePatientRequest;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.Query;
import gov.ca.emsa.pulse.common.domain.QueryStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "queryStatus")
@RestController
@RequestMapping("/queries")
public class QueryService {
    private static final Logger logger = LogManager.getLogger(QueryService.class);
    @Autowired
    QueryManager queryManager;
    @Autowired
    PatientManager patientManager;
    @Autowired
    DocumentManager docManager;
    @Autowired
    AlternateCareFacilityManager acfManager;
    @Autowired
    AuditEventManager auditManager;

    @Autowired

    @ApiOperation(value = "Get all queries for the logged-in user")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public List<Query> getQueries() {
        CommonUser user = UserUtil.getCurrentUser();

        synchronized (queryManager) {
            List<QueryDTO> queries = queryManager.getOpenQueriesForUser(user.getSubjectName());
            List<Query> results = new ArrayList<Query>();
            for (QueryDTO query : queries) {
                results.add(DtoToDomainConverter.convert(query));
            }
            return results;
        }
    }

    @ApiOperation(value = "Get the status of a query")
    @RequestMapping(value = "/{queryId}", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Query getQueryStatus(@PathVariable(value = "queryId") Long queryId) {
        synchronized (queryManager) {
            QueryDTO initiatedQuery = queryManager.getById(queryId);
            return DtoToDomainConverter.convert(initiatedQuery);
        }
    }

    @ApiOperation(value = "Cancel part of a query that's going to a specific endpoint")
    @RequestMapping(value = "/{queryId}/endpoint/{endpointId}/cancel", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Query cancelPatientDiscoveryQuery(@PathVariable(value = "queryId") Long queryId,
            @PathVariable(value = "endpointId") Long endpointId) throws InvalidArgumentsException {
        synchronized (queryManager) {
            QueryDTO query = queryManager.getById(queryId);
            if (query == null) {
                throw new InvalidArgumentsException("No query was found with id " + queryId);
            } else if (query.getStatus() == null || query.getStatus() == QueryStatus.Closed) {
                throw new InvalidArgumentsException("Query with id " + queryId
                        + " is already marked as Closed and cannot be requeried. Please start over with a new query.");
            }
            List<QueryEndpointMapDTO> queryEndpointMaps = queryManager.getQueryEndpointMapByQueryAndEndpoint(queryId,
                    endpointId);
            if (queryEndpointMaps == null || queryEndpointMaps.size() == 0) {
                throw new InvalidArgumentsException("No endpoint with ID " + endpointId
                        + " was found for query with ID " + queryId + " that is not already closed.");
            }

            queryManager.cancelQueryToEndpoint(queryId, endpointId);
            queryManager.updateQueryStatusFromEndpoints(queryId);
            QueryDTO queryWithCancelledLocation = queryManager.getById(queryId);
            return DtoToDomainConverter.convert(queryWithCancelledLocation);
        }
    }

    @ApiOperation(value = "Re-query an endpoint from an existing query. "
            + "This runs asynchronously and returns a query object which can later be used to get the results.")
    @RequestMapping(path = "/{queryId}/endpoint/{endpointId}/requery", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public @ResponseBody Query requeryPatients(@PathVariable("queryId") Long queryId,
            @PathVariable("endpointId") Long endpointId)
            throws JsonProcessingException, InvalidArgumentsException, IOException {
        CommonUser user = UserUtil.getCurrentUser();
        QueryDTO initiatedQuery = null;
        synchronized (queryManager) {
            QueryDTO query = queryManager.getById(queryId);
            if (query == null) {
                throw new InvalidArgumentsException("No query was found with id " + queryId);
            } else if (query.getStatus() == null || query.getStatus() == QueryStatus.Closed) {
                throw new InvalidArgumentsException("Query with id " + queryId
                        + " is already marked as Closed and cannot be requeried. Please start over with a new query.");
            }
            List<QueryEndpointMapDTO> queryEndpointMaps = queryManager.getQueryEndpointMapByQueryAndEndpoint(queryId,
                    endpointId);
            if (queryEndpointMaps == null || queryEndpointMaps.size() == 0) {
                throw new InvalidArgumentsException("No endpoint with ID " + endpointId
                        + " was found for query with ID " + queryId + " that is not already closed.");
            }
            String assertion = SamlUtil.signAndBuildStringAssertion(user);
            Long newQueryEndpointMapId = queryManager.requeryForPatientRecords(assertion, queryId, endpointId, user);
            if (newQueryEndpointMapId != null) {
                QueryEndpointMapDTO dto = queryManager.getQueryEndpointMapById(newQueryEndpointMapId);
                initiatedQuery = queryManager.getById(dto.getQueryId());
            }
        }
        return DtoToDomainConverter.convert(initiatedQuery);
    }

    @ApiOperation(value = "Delete a query")
    @RequestMapping(value = "/{queryId}/delete", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public void deleteQuery(@PathVariable(value = "queryId") Long queryId) {
        synchronized (queryManager) {
            queryManager.close(queryId);
        }
    }

    @ApiOperation(value = "Create a Patient from multiple PatientRecords")
    @RequestMapping(value = "/{queryId}/stage", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Patient stagePatientFromResults(@PathVariable(value = "queryId") Long queryId,
            @RequestBody CreatePatientRequest request)
            throws InvalidArgumentsException, SQLException, JsonProcessingException {
        CommonUser user = UserUtil.getCurrentUser();
        if (request.getPatient() == null || request.getPatientRecordIds() == null
                || request.getPatientRecordIds().size() == 0) {
            throw new InvalidArgumentsException("A patient object and at least one patient record id is required.");
        }

        // create a new Patient
        PatientDTO patientToCreate = DomainToDtoConverter.convertToPatient(request.getPatient());
        // full name required by db
        if (StringUtils.isEmpty(StringUtils.isEmpty(patientToCreate.getFullName()))) {
            throw new InvalidArgumentsException("Patient full name is required.");
        }

        AlternateCareFacilityDTO acfDto = null;
        if (user.getLiferayAcfId() == null) {
            if (user.getAcf() != null && user.getAcf().getId() != null) {
                acfDto = acfManager.getById(user.getAcf().getId());
            }
        } else {
            acfDto = acfManager.getByLiferayAcfId(user.getLiferayAcfId());
        }

        if (acfDto == null) {
            throw new InvalidParameterException(
                    "There was no ACF supplied in the User header, or the ACF had a null ID.");
        }

        patientToCreate.setAcf(acfDto);

        PatientDTO patient = patientManager.create(patientToCreate);
        auditManager.createPulseAuditEvent(AuditType.PC, patient.getId());

        synchronized (queryManager) {
            // create patient-endpoint mappings for doc discovery based on the
            // patientrecords we are using
            for (Long patientRecordId : request.getPatientRecordIds()) {
                PatientEndpointMapDTO patLocMapDto = patientManager.createEndpointMapForDocumentDiscovery(patient,
                        patientRecordId);
                String assertion = SamlUtil.signAndBuildStringAssertion(user);
                patient.getEndpointMaps().add(patLocMapDto);
                docManager.queryForDocuments(user, assertion, patLocMapDto);
                // kick off document list retrieval service

            }

            // delete query (all associated items should cascade)
            queryManager.close(queryId);
        }
        return DtoToDomainConverter.convert(patient);
    }
}
