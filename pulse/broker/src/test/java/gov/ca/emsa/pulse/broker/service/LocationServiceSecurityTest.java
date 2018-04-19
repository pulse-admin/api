package gov.ca.emsa.pulse.broker.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.service.LocationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BrokerApplicationTestConfig.class)
@WebAppConfiguration
public class LocationServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    LocationService locationServiceController;

    protected String locationUrlPrefix;

    @Before
    public void setUp() throws JsonProcessingException, SQLException {
        super.setUp(locationServiceController);
        locationUrlPrefix = "/locations";
    }

    @Override
    @After
    public void restore() throws SQLException {
        super.restore();
    }

    @Test
    @Ignore
    public void testLocations() throws Exception {
        mockMvc.perform(get(locationUrlPrefix)).andExpect(status().isUnauthorized());
    }
}
