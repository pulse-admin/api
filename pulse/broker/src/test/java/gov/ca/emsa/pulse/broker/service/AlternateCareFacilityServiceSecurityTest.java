package gov.ca.emsa.pulse.broker.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.emsa.pulse.auth.jwt.JWTUserTestHelper;
import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.service.AlternateCareFacilityService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BrokerApplicationTestConfig.class)
@WebAppConfiguration
public class AlternateCareFacilityServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    AlternateCareFacilityService acfServiceController;
    @Autowired
    private AlternateCareFacilityManager acfManager;

    @Before
    public void setUp() throws SQLException, JsonProcessingException {
        super.setUp(acfServiceController);
    }

    @Override
    @After
    public void restore() throws SQLException {
        super.restore();
    }

    @Test
    @Ignore
    public void testGetAcfs() throws Exception {

        /* scenario 0: deny */
        mockMvc.perform(get("/acfs")).andExpect(status().isUnauthorized());

        /*
         * scenario 1: user=ROLE_ADMIN, gets all acfs.
         * 
         * scenario 2a: user=ROLE_ORG_ADMIN, state=9009 (arbitrary), gets none.
         * scenario 2b: user=ROLE_ORG_ADMIN, state=10, gets only 1 acf
         * 
         * scenario 3a: user=ROLE_PROVIDER, state=9009, gets none. scenario 3b:
         * user=ROLE_PROVIDER, state=10, gets only 1 acf.
         * 
         */

        // scenario 1: user=ROLE_ADMIN, gets all acfs.
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", 999999L, 99999L); // the
                                                                         // info
        // doesn't
        // matter for
        // role_admin

        MvcResult result = mockMvc.perform(get("/acfs")).andExpect(status().isOk()).andReturn();

        String out = result.getResponse().getContentAsString();
        // System.out.println(result.getResponse().getContentAsString());
        // System.out.println("Matches = " + StringUtils.countMatches(out,
        // "<item>"));
        assertTrue(StringUtils.countMatches(out, "<item>") == totalAcfs);

        // scenario 2a: user=ROLE_ORG_ADMIN, state=9009 (arbitrary), gets none.
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", 9009L, 100L); // the
                                                                         // info
        // doesn't
        // matter for
        // role_admin
        result = mockMvc.perform(get("/acfs")).andExpect(status().isOk()).andReturn();

        out = result.getResponse().getContentAsString();
        assertTrue(StringUtils.countMatches(out, "<item>") == 0);

        // scenario 2b: user=ROLE_ORG_ADMIN, state=10, gets only 1 acf
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest,
                10000L /* ignored */);
        result = mockMvc.perform(get("/acfs")).andExpect(status().isOk()).andReturn();

        out = result.getResponse().getContentAsString();
        assertTrue(StringUtils.countMatches(out, "<item>") == 1);

        // scenario 3a: user=ROLE_PROVIDER, state=9009, gets none.
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", 9009L, liferayAcfIdUsedForTest);
        result = mockMvc.perform(get("/acfs")).andExpect(status().isOk()).andReturn();

        out = result.getResponse().getContentAsString();
        assertTrue(StringUtils.countMatches(out, "<item>") == 0);

        // scenario 3b: user=ROLE_PROVIDER, state=10, gets only 1 acf.
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest);
        result = mockMvc.perform(get("/acfs")).andExpect(status().isOk()).andReturn();

        out = result.getResponse().getContentAsString();
        assertTrue(StringUtils.countMatches(out, "<item>") == 1);
    }

    @Test
    @Ignore
    public void testgetACFById() throws Exception {
        /* scenario 0: deny */
        String getUrl = "/acfs/" + acfIdUsedForTest;
        String searchString = "<AlternateCareFacility>";

        mockMvc.perform(get(getUrl)).andExpect(status().isUnauthorized());

        /*
         * tests get Acf: role_admin, and org_admin/provider only if they have
         * permissions to the state
         */

        // scenario1: role_admin gets back 1 acf
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", 999999L, 99999L); // the
                                                                         // info
        // doesn't
        // matter for
        // role_admin

        MvcResult result = mockMvc.perform(get(getUrl)).andExpect(status().isOk()).andReturn();

        String out = result.getResponse().getContentAsString();
        System.out.println(result.getResponse().getContentAsString());
        // System.out.println("Matches = " + StringUtils.countMatches(out,
        // "<item>"));
        assertTrue(StringUtils.countMatches(out, searchString) == 1);

        // scenario2: org_admin gets back 0, since he belongs to a different org
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", 9999L, 99999L); // the
                                                                           // info
        // doesn't
        // matter for
        // role_admin

        result = mockMvc.perform(get(getUrl)).andExpect(status().isUnauthorized()).andReturn();

        out = result.getResponse().getContentAsString();
        // System.out.println(result.getResponse().getContentAsString());
        // System.out.println("Matches = " + StringUtils.countMatches(out,
        // "<item>"));
        assertTrue(StringUtils.countMatches(out, searchString) == 0);

        // scenario2: org_admin gets back 0, since he belongs to a different org
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", 10L, 100L); // the
                                                                       // info
        // doesn't matter
        // for role_admin

        result = mockMvc.perform(get(getUrl)).andExpect(status().isOk()).andReturn();

        out = result.getResponse().getContentAsString();
        // System.out.println(result.getResponse().getContentAsString());
        // System.out.println("Matches = " + StringUtils.countMatches(out,
        // "<item>"));
        assertTrue(StringUtils.countMatches(out, searchString) == 1);

    }

    @Test
    @Ignore
    public void testCreate() throws Exception {
        /* scenario 0: unauthorized */
        mockMvc.perform(post("/acfs/create").contentType(contentType).content(requestJsonAcf))
                .andExpect(status().isUnauthorized());

        /* scenario 1: role provider - currently not allowed */
        /*
         * TestUsers.setCurrentUser("ROLE_PROVIDER", 999999L, 99999L); // the
         * info // doesn't // matter // for // role_admin
         * mockMvc.perform(post("/acfs/create").contentType(contentType).content
         * (requestJsonAcf)) .andExpect(status().isUnauthorized());
         */

        /* scenario 2: role_admin */
        String out = "";
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", 999999L, 99999L); // the
                                                                         // info
        // doesn't
        // matter for
        // role_admin
        MvcResult result = mockMvc.perform(post("/acfs/create").contentType(contentType).content(requestJsonAcf))
                .andExpect(status().isOk()).andReturn();
        System.out.println(out = result.getResponse().getContentAsString());
        Matcher m = p.matcher(out);
        long acfCreatedId = 0;
        if (m.find()) {
            System.out.println(m.group(1));
            acfCreatedId = Long.parseLong(m.group(1));
        }

        AlternateCareFacilityDTO acfDb = acfManager.getById(acfCreatedId);
        assertEquals(acfDb.getLiferayAcfId(), JWTUserTestHelper.getCurrentUser().getLiferayAcfId());
        assertEquals(acfDb.getLiferayStateId(), JWTUserTestHelper.getCurrentUser().getLiferayStateId());

        /* scenario 3: role_org_admin */
        acfCreate.setName("Test Create ACF2");
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", 999999L, 99999L); // the
                                                                             // info
        // doesn't
        // matter
        // for
        // role_admin
        result = mockMvc.perform(post("/acfs/create").contentType(contentType).content(requestJsonAcf))
                .andExpect(status().isOk()).andReturn();
        System.out.println(out = result.getResponse().getContentAsString());
        m = p.matcher(out);
        acfCreatedId = 0;
        if (m.find()) {
            System.out.println(m.group(1));
            acfCreatedId = Long.parseLong(m.group(1));
        }

        acfDb = acfManager.getById(acfCreatedId);
        assertEquals(acfDb.getLiferayAcfId(), JWTUserTestHelper.getCurrentUser().getLiferayAcfId());
        assertEquals(acfDb.getLiferayStateId(), JWTUserTestHelper.getCurrentUser().getLiferayStateId());

    }

    @Test
    @Ignore
    public void testAcfEdit() throws Exception {
        String testAcfEditUrl = "/acfs/" + acfIdUsedForTest + "/edit";
        // scenario 0: unauthorized
        mockMvc.perform(post(testAcfEditUrl).contentType(contentType).content(requestJsonAcfTest))
                .andExpect(status().isUnauthorized());

        // scenario 1: admin allowed
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", liferayStateIdUsedForTest + 1, liferayAcfIdUsedForTest + 1); // the
        // info
        // doesn't
        // matter
        // for
        // role_admin
        mockMvc.perform(post(testAcfEditUrl).contentType(contentType).content(requestJsonAcfTest))
                .andExpect(status().isOk());

        // scenario 2: org_admin from a different state not allowed
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest + 1, 100L);
        mockMvc.perform(post(testAcfEditUrl).contentType(contentType).content(requestJsonAcfTest))
                .andExpect(status().isUnauthorized());

        // scenario 3: org_admin for the same state allowed
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest + 1);
        mockMvc.perform(post(testAcfEditUrl).contentType(contentType).content(requestJsonAcfTest))
                .andExpect(status().isOk());

        // scenario 4: provider for the same acf allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest);
        mockMvc.perform(post(testAcfEditUrl).contentType(contentType).content(requestJsonAcfTest))
                .andExpect(status().isOk());

        // scenario 5: provider for differnt acf not allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest + 1);
        mockMvc.perform(post(testAcfEditUrl).contentType(contentType).content(requestJsonAcfTest))
                .andExpect(status().isUnauthorized());
    }

    /*
     * 
     * @Test public void testAcfDelete() throws Exception {
     * mockMvc.perform(post("/acfs/1/delete")) .andExpect(status().isOk()); }
     * 
     * public void testAcfDesignate() throws Exception {
     * mockMvc.perform(post("/acfs/1/delete")) .andExpect(status().isOk()); }
     *
     *
     *
     *
     *
     *
     *
     * 
     */

}
