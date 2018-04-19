package gov.ca.emsa.pulse.broker.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import gov.ca.emsa.pulse.auth.jwt.JWTUserTestHelper;
import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.service.SearchService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BrokerApplicationTestConfig.class)
@WebAppConfiguration
public class SearchServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    SearchService searchServiceController;

    protected String searchUrlPrefix;

    @Before
    public void setUp() throws JsonProcessingException, SQLException {
        super.setUp(searchServiceController);
        searchUrlPrefix = "/search";
    }

    @After
    public void restore() throws SQLException {
        super.restore();
    }

    @Test
    @Ignore
    public void testSearch() throws Exception {
        // scenario 0: no user
        mockMvc.perform(post("/search").contentType(contentType).content(requestJsonPatientTest))
                .andExpect(status().isUnauthorized());

        // scenario 1: role_admin: allowed.
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
                                                                                                    // info
                                                                                                    // doesn't
                                                                                                    // matter
                                                                                                    // for
                                                                                                    // role_admin
        mockMvc.perform(post("/search").contentType(contentType).content(requestJsonPatientTest))
                .andExpect(status().isOk());

        // scenario 2: role_org_admin: not allowed.
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
                                                                                                        // info
                                                                                                        // doesn't
                                                                                                        // matter
                                                                                                        // for
                                                                                                        // role_admin
        mockMvc.perform(post("/search").contentType(contentType).content(requestJsonPatientTest))
                .andExpect(status().isUnauthorized());

        // scenario 3: role_provider: allowed.
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
                                                                                                       // info
                                                                                                       // doesn't
                                                                                                       // matter
                                                                                                       // for
                                                                                                       // role_admin
        mockMvc.perform(post("/search").contentType(contentType).content(requestJsonPatientTest))
                .andExpect(status().isOk());
    }
}
