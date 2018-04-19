package gov.ca.emsa.pulse.broker.service;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.EndpointManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.service.ApiExceptionControllerAdvice;

public class BaseSecurityTest {
    @Autowired
    private AlternateCareFacilityManager acfManager;

    @Autowired
    protected PatientManager patientManager;

    @Autowired
    protected DocumentManager docManager;

    @Autowired
    protected EndpointManager endpointManager;

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

    protected MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    protected final ResultMatcher authorized = status().is(CoreMatchers.not(CoreMatchers.is(401)));
    protected static final Pattern p = Pattern.compile("<id>([0-9]+)</id>");

    public void setUp(Object controller) throws SQLException, JsonProcessingException {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionControllerAdvice()).build();

        /*
         * prepare data set acf 1 liferay_state_id 1 liferay_state_id
         */
        List<AlternateCareFacilityDTO> allAcfs = acfManager.getAll();
        totalAcfs = allAcfs.size();

        assertTrue(totalAcfs > 0);

        AlternateCareFacilityDTO acfUsedForTest = allAcfs.get(0);
        acfIdUsedForTest = acfUsedForTest.getId();
        savedAcfId = acfUsedForTest.getLiferayAcfId();
        savedStateId = acfUsedForTest.getLiferayStateId();

        acfUsedForTest.setLiferayStateId(liferayStateIdUsedForTest);
        acfUsedForTest.setLiferayAcfId(liferayAcfIdUsedForTest);
        acfManager.updateAcfDetails(acfUsedForTest);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        acfCreate = new AlternateCareFacility();
        acfCreate.setName("Test Create ACF");
        acfCreate.setLiferayAcfId(1001L);
        acfCreate.setLiferayStateId(102L);
        acfCreate.setIdentifier("The ACF that is tested for create");
        requestJsonAcf = ow.writeValueAsString(acfCreate);

        requestJsonAcfTest = ow.writeValueAsString(DtoToDomainConverter.convert(acfUsedForTest));

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFullName("Test Patient");
        patientDTO.setAcf(acfUsedForTest);
        patientUsedForTest = DtoToDomainConverter.convert(patientManager.create(patientDTO));
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
         * endpointUsedForTest));
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
        AlternateCareFacilityDTO toUpdate = acfManager.getById(acfIdUsedForTest);
        toUpdate.setLiferayStateId(savedStateId);
        toUpdate.setLiferayAcfId(savedAcfId);
        acfManager.updateAcfDetails(toUpdate);
        patientManager.delete(patientUsedForTest.getId());
    }

}
