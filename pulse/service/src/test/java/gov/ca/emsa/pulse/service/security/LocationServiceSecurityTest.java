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
import gov.ca.emsa.pulse.service.LocationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceApplicationTestConfig.class)
@WebAppConfiguration
public class LocationServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    LocationService locationServiceController;

    @Before
    public void setUp() throws JsonProcessingException, SQLException {
        super.setUp(locationServiceController);

    }

    @Test
    public void testAllEndpoints() throws Exception {
        String[] endpoints = {
                "/locations", "/locations/statistics"
        };

        for (String endpoint : endpoints) {
            testPatternOrgAdmin(endpoint, true);
        }
    }
}
