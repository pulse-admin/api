package gov.ca.emsa.pulse.service.security;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.emsa.pulse.ServiceApplicationTestConfig;
import gov.ca.emsa.pulse.common.domain.CreatePatientRequest;
import gov.ca.emsa.pulse.service.QueryService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceApplicationTestConfig.class)
@WebAppConfiguration
public class QueryServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    QueryService queryServiceController;

    String queryUrlPrefix = " /queries";

    @Before
    public void setUp() throws JsonProcessingException, SQLException {
        super.setUp(queryServiceController);

    }

    @Test
    public void testAllEndpoints() throws Exception {
        String[] endpointsGet = {
                "/queries", "/queries/1",
        };

        String[] endpointsPost = {
                "/queries/1/delete", "/queries/1/endpoint/1/cancel", "/queries/1/endpoint/1/requery",
                "/queries/1/stage",

        };

        for (String endpoint : endpointsGet) {
            testPatternProvider(endpoint, true);
        }
        for (String endpoint : endpointsPost) {
            testPatternProvider(endpoint, false, new CreatePatientRequest());
        }
    }
}
