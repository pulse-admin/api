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
import gov.ca.emsa.pulse.service.DocumentService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceApplicationTestConfig.class)
@WebAppConfiguration
public class DocumentServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    DocumentService documentServiceController;

    @Before
    public void setUp() throws JsonProcessingException, SQLException {
        super.setUp(documentServiceController);

    }

    @Test
    public void testAllEndpoints() throws Exception {
        String[] endpoints = {
                "/patients/1/documents", "/patients/1/endpoints/1/cancel", "/patients/1/endpoints/1/requery",
                "/patients/1/documents/1", "/patients/1/documents", "/patients/1/documents/1/cancel"
        };

        for (String endpoint : endpoints) {
            testPatternProvider(endpoint, !endpoint.contains("endpoint") && !endpoint.contains("cancel"));
        }
    }

}
