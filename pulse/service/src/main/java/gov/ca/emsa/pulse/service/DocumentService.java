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
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.auth.user.UserRetrievalException;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.Patient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Documents")
@RestController
public class DocumentService {
    private static final Logger logger = LogManager.getLogger(DocumentService.class);

    @Value("${brokerUrl}")
    private String brokerUrl;

    @ApiOperation(value = "Search Documents for the given patient id.")
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    @RequestMapping(value = "/patients/{id}/documents", method = RequestMethod.GET)
    public List<Document> searchDocuments(@PathVariable Long id) throws Exception {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        ArrayList<Document> docList = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Document[]> entity = new HttpEntity<Document[]>(headers);
            HttpEntity<Document[]> response = query.exchange(brokerUrl + "/patients/" + id + "/documents",
                    HttpMethod.GET, entity, Document[].class);
            logger.info("Request sent to broker from services REST.");
            docList = new ArrayList<Document>(Arrays.asList(response.getBody()));
        }

        return docList;
    }

    @ApiOperation(value = "Cancel a request to an endpoint for a list of documents")
    @RequestMapping(value = "/patients/{patientId}/endpoints/{endpointId}/cancel", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Patient cancelDocumentListQuery(@PathVariable(value = "patientId") Long patientId,
            @PathVariable(value = "endpointId") Long endpointId) throws Exception {
        RestTemplate query = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        Patient returnPatient = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Patient> request = new HttpEntity<Patient>(headers);
            returnPatient = query.postForObject(
                    brokerUrl + "/patients/" + patientId + "/endpoints/" + endpointId + "/cancel", request,
                    Patient.class);
            logger.info("Request sent to broker from services REST.");
        }

        return returnPatient;
    }

    @ApiOperation(value = "Re-run a request to an endpoint for a list of documents")
    @RequestMapping(value = "/patients/{patientId}/endpoints/{endpointId}/requery", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Patient redoDocumentListQuery(@PathVariable(value = "patientId") Long patientId,
            @PathVariable(value = "endpointId") Long endpointId) throws Exception {
        RestTemplate query = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        Patient returnPatient = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Patient> request = new HttpEntity<Patient>(headers);
            returnPatient = query.postForObject(
                    brokerUrl + "/patients/" + patientId + "/endpoints/" + endpointId + "/requery", request,
                    Patient.class);
            logger.info("Request sent to broker from services REST.");
        }

        return returnPatient;
    }

    @ApiOperation(value = "Retrieve a specific Document from an endpoint.")
    @RequestMapping(value = "/patients/{patientId}/documents/{documentId}", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public Document getDocumentContents(@PathVariable("documentId") Long documentId,
            @PathVariable("patientId") Long patientId,
            @RequestParam(value = "cacheOnly", required = false, defaultValue = "true") Boolean cacheOnly)
            throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();
        Document doc = null;

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Document> entity = new HttpEntity<Document>(headers);
            HttpEntity<Document> response = query.exchange(brokerUrl + "/patients/" + patientId + "/documents/"
                    + documentId + "?cacheOnly=" + cacheOnly.toString(), HttpMethod.GET, entity, Document.class);
            logger.info("Request sent to broker from services REST.");
            doc = response.getBody();
        }
        return doc;
    }

    @ApiOperation(value = "Cancel the retrieval of a specific document from an endpoint.")
    @RequestMapping(value = "/patients/{patientId}/documents/{documentId}/cancel", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_PROVIDER"
    })
    public void cancelDocumentContentQuery(@PathVariable("patientId") Long patientId,
            @PathVariable("documentId") Long documentId) throws Exception {
        RestTemplate query = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Void> request = new HttpEntity<Void>(headers);
            query.postForObject(brokerUrl + "/patients/" + patientId + "/documents/" + documentId + "/cancel", request,
                    Patient.class);
            logger.info("Request sent to broker from services REST.");
        }
    }
}
