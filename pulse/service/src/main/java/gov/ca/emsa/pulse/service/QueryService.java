package gov.ca.emsa.pulse.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.common.domain.CreatePatientRequest;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Queries")
@RequestMapping(value = "/queries")
@RestController
public class QueryService {
    private static final Logger logger = LogManager.getLogger(QueryService.class);

    @Value("${brokerUrl}")
    private String brokerUrl;

    // get all queries that belong to the logged in user
    @ApiOperation(value = "Get all queries that belong to the logged in user.")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public List<Query> getQueries() throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        ArrayList<Query> queryList = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Query[]> entity = new HttpEntity<Query[]>(headers);
            HttpEntity<Query[]> response = query.exchange(brokerUrl + "/queries", HttpMethod.GET, entity,
                    Query[].class);
            logger.info("Request sent to broker from services REST.");
            queryList = new ArrayList<Query>(Arrays.asList(response.getBody()));
        }

        return queryList;
    }

    // get the query that has the id = queryId
    @ApiOperation(value = "Get the query that has the given id.")
    @RequestMapping(value = "/{queryId}", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Query getQueryWithId(@PathVariable Long queryId) throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        HttpEntity<Query> response = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Query> entity = new HttpEntity<Query>(headers);
            response = query.exchange(brokerUrl + "/queries/" + queryId, HttpMethod.GET, entity, Query.class);
            logger.info("Request sent to broker from services REST.");
        }

        return response.getBody();
    }

    @ApiOperation(value = "Delete the specified query and any associated results.")
    @RequestMapping(value = "/{queryId}/delete", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Void deleteQuery(@PathVariable(value = "queryId") Long queryId) throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        HttpEntity<Void> response = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Void> entity = new HttpEntity<Void>(headers);
            response = query.exchange(brokerUrl + "/queries/" + queryId + "/delete", HttpMethod.POST, entity,
                    Void.class);
            logger.info("Request sent to broker from services REST.");
        }

        return response.getBody();
    }

    @ApiOperation(value = "Cancel part of a patient discovery query that's going to a specific endpoint")
    @RequestMapping(value = "/{queryId}/endpoint/{endpointId}/cancel", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Query cancelPatientDiscoveryRequestToEndpoint(@PathVariable(value = "queryId") Long queryId,
            @PathVariable(value = "endpointId") Long endpointId) throws JsonProcessingException {
        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        HttpEntity<Query> response = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Query> entity = new HttpEntity<Query>(headers);
            response = query.exchange(brokerUrl + "/queries/" + queryId + "/endpoint/" + endpointId + "/cancel",
                    HttpMethod.POST, entity, Query.class);
            logger.info("Request sent to broker from services REST.");
        }

        return response.getBody();
    }

    @ApiOperation(value = "Re-run a patient search from a specific endpoint.")
    @RequestMapping(path = "/{queryId}/endpoint/{endpointId}/requery", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public @ResponseBody Query requeryPatients(@PathVariable("queryId") Long queryId,
            @PathVariable("endpointId") Long endpointId) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        HttpEntity<Query> response = null;
        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new AccessDeniedException("Only logged in users can search for patients.");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Query> entity = new HttpEntity<Query>(headers);
            RestTemplate query = new RestTemplate();
            response = query.exchange(brokerUrl + "/queries/" + queryId + "/endpoint/" + endpointId + "/requery",
                    HttpMethod.POST, entity, Query.class);
            logger.info("Request sent to broker from services REST.");
        }
        return response.getBody();
    }

    // stages a patient in the database from a query id
    @ApiOperation(value = "Stages a patient in the database from a query id.")
    @RequestMapping(value = "/{queryId}/stage", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Patient stagePatientFromQueryResults(@RequestBody CreatePatientRequest request, @PathVariable Long queryId)
            throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        Patient patient = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<CreatePatientRequest> httpRequest = new HttpEntity<CreatePatientRequest>(request, headers);
            patient = query.postForObject(brokerUrl + "/queries/" + queryId + "/stage", httpRequest, Patient.class);
            logger.info("Request sent to broker from services REST.");
        }

        return patient;
    }
}
