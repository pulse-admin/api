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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.common.domain.stats.LocationStatistics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Locations")
@RestController
public class LocationService {

    private static final Logger logger = LogManager.getLogger(LocationService.class);

    @Value("${brokerUrl}")
    private String brokerUrl;

    // get all locations
    @ApiOperation(value = "Get all locations.")
    @RequestMapping(value = "/locations", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN"
    })
    public ArrayList<Location> getLocations() throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        ArrayList<Location> locList = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<Location[]> entity = new HttpEntity<Location[]>(headers);
            HttpEntity<Location[]> response = query.exchange(brokerUrl + "/locations", HttpMethod.GET, entity,
                    Location[].class);
            logger.info("Request sent to broker from services REST.");
            locList = new ArrayList<Location>(Arrays.asList(response.getBody()));
        }
        return locList;
    }

    // get all location statistics
    @ApiOperation(value = "Get query time statistics for all locations.")
    @RequestMapping(value = "/locations/statistics", method = RequestMethod.GET)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN"
    })
    public ArrayList<LocationStatistics> getLocationRequestStatistics(
            @RequestParam(name = "start", required = false) Long startMillis,
            @RequestParam(name = "end", required = false) Long endMillis) throws JsonProcessingException {

        RestTemplate query = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        ArrayList<LocationStatistics> statList = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
        } else {
            headers.set("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<LocationStatistics[]> entity = new HttpEntity<LocationStatistics[]>(headers);
            String url = brokerUrl + "/locations/statistics";
            if (startMillis != null) {
                url += "?start=" + startMillis;
                if (endMillis != null) {
                    url += "&end=" + endMillis;
                }
            } else if (endMillis != null) {
                url += "?end=" + endMillis;
            }

            HttpEntity<LocationStatistics[]> response = query.exchange(url, HttpMethod.GET, entity,
                    LocationStatistics[].class);
            logger.info("Request sent to broker from services REST.");
            statList = new ArrayList<LocationStatistics>(Arrays.asList(response.getBody()));
        }
        return statList;
    }
}
