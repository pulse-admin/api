package gov.ca.emsa.pulse.service.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import gov.ca.emsa.pulse.ServiceExceptionControllerAdvice;
import gov.ca.emsa.pulse.auth.jwt.JWTUserTestHelper;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.Patient;

public class BaseSecurityTest {
    protected MockMvc mockMvc;

    protected Long acfIdUsedForTest = 1L;
    protected Patient patientUsedForTest;
    protected Document docUsedForTest;
    protected Endpoint endpointUsedForTest;

    protected Long savedAcfId;
    protected Long savedStateId;
    protected Integer totalAcfs;
    protected Long liferayStateIdUsedForTest = 10L;
    protected Long liferayAcfIdUsedForTest = 100L;
    protected String requestJsonAcf;
    protected String requestJsonAcfTest;
    protected String requestJsonPatientTest;
    protected AlternateCareFacility acfCreate;

    ObjectWriter ow;

    protected MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    protected static final Pattern p = Pattern.compile("<id>([0-9]+)</id>");

    protected final ResultMatcher authorized = status().is(CoreMatchers.not(CoreMatchers.is(401)));

    public void setUp(Object controller) throws SQLException, JsonProcessingException {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ServiceExceptionControllerAdvice()).build();

        /*
         * prepare data set acf 1 liferay_state_id 1 liferay_state_id
         */
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ow = mapper.writer().withDefaultPrettyPrinter();
        acfCreate = new AlternateCareFacility();
        acfCreate.setName("Test Create ACF");
        acfCreate.setLiferayAcfId(1001L);
        acfCreate.setLiferayStateId(102L);
        acfCreate.setIdentifier("The ACF that is tested for create");
        requestJsonAcf = ow.writeValueAsString(acfCreate);

        patientUsedForTest = new Patient();
        requestJsonPatientTest = ow.writeValueAsString(patientUsedForTest);

        /*
         * Future
         * 
         * 
         * endpointUsedForTest = new Endpoint();
         * endpointUsedForTest.setExternalId("external"); EndpointType
         * endpointType = new EndpointType(); endpointType.setId(1L);
         * endpointUsedForTest.setEndpointType(endpointType); EndpointStatus
         * status = new EndpointStatus(); status.setId(1L);
         * endpointUsedForTest.setEndpointStatus(status);
         * endpointManager.updateEndpoints(Collections.singletonList(
         * endpointUsedForTest ));
         * 
         * DocumentDTO documentDTO = new DocumentDTO();
         * documentDTO.setDocumentUniqueId("UnitTestPulse");
         * documentDTO.setPatientEndpointMapId(1L);
         * documentDTO.setName("Name of the Doc"); docUsedForTest =
         * DtoToDomainConverter.convert(docManager.create(documentDTO));
         */
    }

    public void restore() throws SQLException {

        /*
         * restore data set acf 1 liferay_state_id 1 liferay_state_id
         */
    }

    protected void setNullUser() {
        JWTUserTestHelper.setNullUser();// the
    }

    protected void setAdmin() {
        JWTUserTestHelper.setCurrentUser("ROLE_ADMIN", 999999L, 99999L); // the
    }

    protected void setOrgAdmin() {
        JWTUserTestHelper.setCurrentUser("ROLE_ORG_ADMIN", 9009L, 100L); // the
    }

    protected void setProvider() {
        JWTUserTestHelper.setCurrentUser("ROLE_PROVIDER", 9009L, 100L); // the
    }

    void testPatternProvider(String url, boolean isGet) throws Exception {
        testPatternProvider(url, isGet, null);
    }

    void testPatternOrgAdmin(String url, boolean isGet) throws Exception {
        testPatternOrgAdmin(url, isGet, null);
    }

    void testPatternProvider(String url, boolean isGet, Object content) throws Exception {
        setNullUser();
        mockMvc.perform(isGet ? get(url)
                : content == null ? post(url)
                        : post(url).contentType(contentType).content(ow.writeValueAsString(content)))
                .andExpect(status().isUnauthorized());
        setAdmin();
        mockMvc.perform(isGet ? get(url)
                : content == null ? post(url)
                        : post(url).contentType(contentType).content(ow.writeValueAsString(content)))
                .andExpect(authorized);
        setOrgAdmin();
        mockMvc.perform(isGet ? get(url)
                : content == null ? post(url)
                        : post(url).contentType(contentType).content(ow.writeValueAsString(content)))
                .andExpect(status().isUnauthorized());
        setProvider();
        mockMvc.perform(isGet ? get(url)
                : content == null ? post(url)
                        : post(url).contentType(contentType).content(ow.writeValueAsString(content)))
                .andExpect(authorized);
    }

    void testPatternOrgAdmin(String url, boolean isGet, Object content) throws Exception {
        setNullUser();
        mockMvc.perform(isGet ? get(url)
                : content == null ? post(url)
                        : post(url).contentType(contentType).content(ow.writeValueAsString(content)))
                .andExpect(status().isUnauthorized());
        setAdmin();
        mockMvc.perform(
                isGet ? get(url) : content == null ? post(url) : post(url).contentType(ow.writeValueAsString(content)))
                .andExpect(authorized);
        setOrgAdmin();
        mockMvc.perform(
                isGet ? get(url) : content == null ? post(url) : post(url).contentType(ow.writeValueAsString(content)))
                .andExpect(authorized);
        setProvider();
        mockMvc.perform(
                isGet ? get(url) : content == null ? post(url) : post(url).contentType(ow.writeValueAsString(content)))
                .andExpect(status().isUnauthorized());
    }

}
