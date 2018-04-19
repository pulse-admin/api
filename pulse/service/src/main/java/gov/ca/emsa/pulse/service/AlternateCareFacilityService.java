package gov.ca.emsa.pulse.service;

import java.util.ArrayList;
import java.util.Arrays;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.auth.user.UserRetrievalException;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "acfs")
@RestController
@RequestMapping("/acfs")
public class AlternateCareFacilityService {

    private static final Logger logger = LogManager.getLogger(AlternateCareFacilityService.class);

    @Value("${brokerUrl}")
    private String brokerUrl;

    // POST - create an alternate care facility
    @ApiOperation(value = "Create a new ACF")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROVIDER"
    })
    public AlternateCareFacility createACF(@RequestBody AlternateCareFacility acf)
            throws UserRetrievalException, JsonProcessingException {

        RestTemplate query = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        AlternateCareFacility returnAcf = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<AlternateCareFacility> request = new HttpEntity<AlternateCareFacility>(acf, headers);
            returnAcf = query.postForObject(brokerUrl + "/acfs/create", request, AlternateCareFacility.class);
            logger.info("Request sent to broker from services REST.");
        }
        return returnAcf;
    }

    // GET all acfs
    @ApiOperation(
            value = "Get the list of all alternate care facilities (ACFs) for the PULSE organization of the logged in user")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROVIDER"
    })
    public ArrayList<AlternateCareFacility> getACFs() throws UserRetrievalException, JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        ArrayList<AlternateCareFacility> acfList = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<AlternateCareFacility[]> entity = new HttpEntity<AlternateCareFacility[]>(headers);
            HttpEntity<AlternateCareFacility[]> response = query.exchange(brokerUrl + "/acfs", HttpMethod.GET, entity,
                    AlternateCareFacility[].class);
            logger.info("Request sent to broker from services REST.");
            acfList = new ArrayList<AlternateCareFacility>(Arrays.asList(response.getBody()));
        }
        return acfList;

    }

    // get acf by its id
    @ApiOperation(value = "Get information about a specific ACF")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROVIDER"
    })
    public AlternateCareFacility getACFById(@PathVariable Long id)
            throws UserRetrievalException, JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        HttpEntity<AlternateCareFacility> response = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<AlternateCareFacility> entity = new HttpEntity<AlternateCareFacility>(headers);
            response = query.exchange(brokerUrl + "/acfs/" + id, HttpMethod.GET, entity, AlternateCareFacility.class);
            logger.info("Request sent to broker from services REST.");
        }
        return response.getBody();
    }

    // edit an acf by its id
    @ApiOperation(value = "Edit an existing ACF")
    @RequestMapping(value = "/{acfId}/edit", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN"
    })
    public AlternateCareFacility editACFById(@PathVariable Long acfId, @RequestBody AlternateCareFacility acf)
            throws UserRetrievalException, JsonProcessingException {

        RestTemplate query = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        AlternateCareFacility returnAcf = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<AlternateCareFacility> request = new HttpEntity<AlternateCareFacility>(acf, headers);
            returnAcf = query.postForObject(brokerUrl + "/acfs/" + acfId + "/edit", request,
                    AlternateCareFacility.class);
            logger.info("Request sent to broker from services REST.");
        }

        return returnAcf;
    }
}
