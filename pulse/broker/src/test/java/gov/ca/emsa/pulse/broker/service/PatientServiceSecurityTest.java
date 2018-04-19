package gov.ca.emsa.pulse.broker.service;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.service.AlternateCareFacilityService;
import gov.ca.emsa.pulse.service.PatientService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BrokerApplicationTestConfig.class)
@WebAppConfiguration
public class PatientServiceSecurityTest extends BaseSecurityTest {
    @Autowired
    AlternateCareFacilityService acfServiceController;
    @Autowired
    PatientService patientServiceController;

    protected String patientUrlPrefix;

    @Before
    public void setUp() throws JsonProcessingException, SQLException {
        super.setUp(patientServiceController);
        patientUrlPrefix = "/patients/" + patientUsedForTest.getId();
    }

    @Override
    @After
    public void restore() throws SQLException {
        super.restore();
    }

    @Test
    @Ignore
    public void testSearch() throws Exception {
        // scenario 0: no user
        mockMvc.perform(get("/patients")).andExpect(status().isUnauthorized());

        // scenario 1: role_admin: allowed.
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        // doesn't
        // matter
        // for
        // role_admin
        mockMvc.perform(get("/patients")).andExpect(status().isOk());

        // scenario 2: role_org_admin: not allowed.
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        // doesn't
        // matter
        // for
        // role_admin
        mockMvc.perform(get("/patients")).andExpect(status().isUnauthorized());

        // scenario 3: role_provider: allowed.
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        // doesn't
        // matter
        // for
        // role_admin
        mockMvc.perform(get("/patients")).andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void testPatientDocuments() throws Exception {
        // scenario 0: no user
        mockMvc.perform(get(patientUrlPrefix + "/documents")).andExpect(status().isUnauthorized());

        // scenario 1: role_admin allowed
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        mockMvc.perform(get(patientUrlPrefix + "/documents")).andExpect(status().isOk());

        // scenario 2: role_org_admin - no
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        mockMvc.perform(get(patientUrlPrefix + "/documents")).andExpect(status().isUnauthorized());

        // scenario 3: provider at the same acf - allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        mockMvc.perform(get(patientUrlPrefix + "/documents")).andExpect(status().isOk());

        // scenario 4: provider at a different acf - not allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest + 1); // the
        // info
        mockMvc.perform(get(patientUrlPrefix + "/documents")).andExpect(status().isUnauthorized());

    }

    @Ignore
    @Test
    public void testCancelDocumentListQuery() throws Exception {
        assertEquals(testPatientUrlPost(patientUrlPrefix + "/endpoints/1/cancel"), "");
    }

    @Ignore
    @Test
    public void testRedoDocumentListQuery() throws Exception {
        assertEquals(testPatientUrlPost(patientUrlPrefix + "/endpoints/1/requery"), "");
    }

    @Ignore
    @Test
    public void testGetDocumentContents() throws Exception {
        assertEquals(testPatientUrlPost(patientUrlPrefix + "/documents/1"), "");
    }

    @Test
    @Ignore
    public void testEdit() throws Exception {
        assertEquals(testPatientUrlPost(patientUrlPrefix + "/edit"), "");
    }

    public String testPatientUrlPost(String url) throws Exception {
        // scenario 0: no user
        String exceptionMessage = "";

        try {
            mockMvc.perform(post(url).contentType(contentType).content(requestJsonPatientTest))
                    .andExpect(status().isUnauthorized());
        } catch (Exception e) {
            exceptionMessage += "0.no user: " + e.getMessage() + "\n";
        }

        // scenario 1: role_admin allowed
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        try {
            mockMvc.perform(post(url).contentType(contentType).content(requestJsonPatientTest))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            exceptionMessage += "1.role admin: " + e.getMessage() + "\n";
        }

        // scenario 2: role_org_admin - no
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        try {
            mockMvc.perform(post(url).contentType(contentType).content(requestJsonPatientTest))
                    .andExpect(status().isUnauthorized());
        } catch (Exception e) {
            exceptionMessage += "2.roleorgadmin " + e.getMessage() + "\n";
        }

        // scenario 3: provider at the same acf - allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        try {
            mockMvc.perform(post(url).contentType(contentType).content(requestJsonPatientTest))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            exceptionMessage += "3.roleprovider " + e.getMessage() + "\n";
        }

        // scenario 4: provider at a different acf - not allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest + 1); // the
        // info
        try {
            mockMvc.perform(post(url).contentType(contentType).content(requestJsonPatientTest))
                    .andExpect(status().isUnauthorized());
        } catch (Exception e) {
            exceptionMessage += "4.roleprovider " + e.getMessage() + "\n";
        }

        return exceptionMessage;
    }

    @Test
    @Ignore
    public void testDelete() throws Exception {
        // scenario 0: no user
        mockMvc.perform(post(patientUrlPrefix + "/delete")).andExpect(status().isUnauthorized());

        // scenario 1: role_admin allowed
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        mockMvc.perform(post(patientUrlPrefix + "/delete")).andExpect(status().isOk());
        // create again
        patientUsedForTest = DtoToDomainConverter
                .convert(patientManager.create(DomainToDtoConverter.convertToPatient(patientUsedForTest)));
        patientUrlPrefix = "/patients/" + patientUsedForTest.getId();

        // scenario 2: role_org_admin - no
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        mockMvc.perform(post(patientUrlPrefix + "/delete")).andExpect(status().isUnauthorized());

        // scenario 3: provider at the same acf - allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest); // the
        // info
        mockMvc.perform(post(patientUrlPrefix + "/delete")).andExpect(status().isOk());
        // create again
        patientUsedForTest = DtoToDomainConverter
                .convert(patientManager.create(DomainToDtoConverter.convertToPatient(patientUsedForTest)));
        patientUrlPrefix = "/patients/" + patientUsedForTest.getId();

        // scenario 4: provider at a different acf - not allowed
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", liferayStateIdUsedForTest, liferayAcfIdUsedForTest + 1); // the
        // info
        mockMvc.perform(post(patientUrlPrefix + "/delete")).andExpect(status().isUnauthorized());

    }
}
