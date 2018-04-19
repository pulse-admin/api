package gov.ca.emsa.pulse.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.emsa.pulse.auth.user.JWTAuthenticatedUser;
import gov.ca.emsa.pulse.auth.user.UserRetrievalException;
import gov.ca.emsa.pulse.common.domain.PulseUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "user")
@RestController
@RequestMapping("/user")
public class PulseUserService {

    private static final Logger logger = LogManager.getLogger(PulseUserService.class);

    @Value("${brokerUrl}")
    private String brokerUrl;

    @ApiOperation(value = "Create a new User")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Secured({
            "ROLE_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROVIDER"
    })
    public PulseUser create(@RequestBody(required = true) PulseUser toCreate)
            throws JsonProcessingException, UserRetrievalException {

        RestTemplate query = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();

        JWTAuthenticatedUser jwtUser = (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
        PulseUser returnUser = null;
        if (jwtUser == null) {
            logger.error("Could not find a logged in user. ");
            throw new UserRetrievalException("Could not find a logged in user. ");
        } else {
            headers.add("User", mapper.writeValueAsString(jwtUser));
            HttpEntity<PulseUser> request = new HttpEntity<PulseUser>(toCreate, headers);
            returnUser = query.postForObject(brokerUrl + "/user/create", request, PulseUser.class);
            logger.info("Request sent to broker from services REST.");
        }
        return returnUser;
    }

}
