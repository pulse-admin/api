package gov.ca.emsa.pulse.broker.manager;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.broker.dao.AlternateCareFacilityDAO;
import gov.ca.emsa.pulse.broker.dao.EndpointDAO;
import gov.ca.emsa.pulse.broker.dao.LocationDAO;
import gov.ca.emsa.pulse.broker.dao.LocationEndpointMapDAO;
import gov.ca.emsa.pulse.broker.dao.PatientRecordDAO;
import gov.ca.emsa.pulse.broker.dao.QueryDAO;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointStatusDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointTypeDTO;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.dto.LocationStatusDTO;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.QueryDTO;
import gov.ca.emsa.pulse.broker.dto.QueryEndpointMapDTO;
import gov.ca.emsa.pulse.common.domain.LocationStatus;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import gov.ca.emsa.pulse.common.domain.QueryStatus;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BrokerApplicationTestConfig.class})
public class PatientManagerTest extends TestCase {
    @Autowired PatientManager patientManager;
    @Autowired QueryDAO queryDao;
    @Autowired LocationDAO locationDao;
    @Autowired EndpointDAO endpointDao;
    @Autowired LocationEndpointMapDAO mappingDao;
    @Autowired AlternateCareFacilityDAO acfDao;
    @Autowired PatientRecordDAO prDao;
    private AlternateCareFacilityDTO acf;
    private LocationDTO location1, location2;
    private EndpointDTO endpoint1, endpoint2;

    @Before
    public void setup() throws SQLException  {
        acf = new AlternateCareFacilityDTO();
        acf.setIdentifier("ACF1");
        acf.setLiferayStateId(1L);
        acf.setLiferayAcfId(2L);
        acf = acfDao.create(acf);
        assertNotNull(acf);
        assertNotNull(acf.getId());
        assertTrue(acf.getId().longValue() > 0);
        LocationStatusDTO locStatus = new LocationStatusDTO();
        locStatus.setId(1L);

        location1 = new LocationDTO();
        location1.setExternalId("1");
        location1.setName("John's Hopkins Medical Center");
        location1.setDescription("A hospital");
        location1.setType("Hospital");
        location1.setExternalLastUpdateDate(new Date());
        location1.setParentOrgName("EHealth Parent Org");
        location1.setStatus(locStatus);
        location1 = locationDao.create(location1);

        location2 = new LocationDTO();
        location2.setExternalId("2");
        location2.setName("University of Maryland Medical Center");
        location2.setDescription("A hospital");
        location2.setType("Hospital");
        location2.setExternalLastUpdateDate(new Date());
        location2.setParentOrgName("EHealth Parent Org");
        location2.setStatus(locStatus);
        location2 = locationDao.create(location2);

        EndpointStatusDTO status = new EndpointStatusDTO();
        status.setId(1L);
        EndpointTypeDTO type = new EndpointTypeDTO();
        type.setId(1L);
        endpoint1 = new EndpointDTO();
        endpoint1.setAdapter("eHealth");
        endpoint1.setEndpointStatus(status);
        endpoint1.setEndpointType(type);
        endpoint1.setExternalId("001");
        endpoint1.setExternalLastUpdateDate(new Date());
        endpoint1.setUrl("http://test.com"); 
        endpoint1 = endpointDao.create(endpoint1);

        endpoint2 = new EndpointDTO();
        endpoint2.setAdapter("eHealth");
        endpoint2.setEndpointStatus(status);
        endpoint2.setEndpointType(type);
        endpoint2.setExternalId("002");
        endpoint2.setExternalLastUpdateDate(new Date());
        endpoint2.setUrl("http://test.com"); 
        endpoint2 = endpointDao.create(endpoint1);

        mappingDao.create(location1.getId(), endpoint1.getId());
        mappingDao.create(location2.getId(), endpoint1.getId());
        mappingDao.create(location2.getId(), endpoint2.getId());

        QueryDTO toInsert = new QueryDTO();
        toInsert.setStatus(QueryStatus.Active);
        toInsert.setTerms("terms");
        toInsert.setUserId("kekey");

        QueryEndpointMapDTO queryEndpointMap1 = new QueryEndpointMapDTO();
        queryEndpointMap1.setEndpointId(endpoint1.getId());
        queryEndpointMap1.setStatus(QueryEndpointStatus.Active);
        toInsert.getEndpointMaps().add(queryEndpointMap1);

        QueryEndpointMapDTO queryEndpointMap2 = new QueryEndpointMapDTO();
        queryEndpointMap2.setEndpointId(endpoint2.getId());
        queryEndpointMap2.setStatus(QueryEndpointStatus.Active);
        toInsert.getEndpointMaps().add(queryEndpointMap2);

        QueryDTO inserted = queryDao.create(toInsert);
        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertTrue(inserted.getId().longValue() > 0);
        assertNotNull(inserted.getEndpointMaps());
        assertEquals(2, inserted.getEndpointMaps().size());
        queryEndpointMap1 = inserted.getEndpointMaps().get(0);
        assertNotNull(inserted.getEndpointMaps().get(0).getId());
        assertTrue(inserted.getEndpointMaps().get(0).getId().longValue() > 0);
        queryEndpointMap2 = inserted.getEndpointMaps().get(1);
        assertNotNull(inserted.getEndpointMaps().get(1).getId());
        assertTrue(inserted.getEndpointMaps().get(1).getId().longValue() > 0);

        //queryResult1 = new PatientRecordDTO();
        //queryResult1.setGivenName("John");
        //queryResult1.setFamilyName("Smith");
        //queryResult1.setGender("Male");
        //queryResult1.setOrgPatientId("JS1");
        //queryResult1.setQueryOrganizationId(orgQuery1.getId());
        //queryResult1.setSsn("111223333");
        //queryResult1.setPhoneNumber("5555555555");
        //queryResult1 = prDao.create(queryResult1);
        //assertNotNull(queryResult1);
        //assertNotNull(queryResult1.getId());
        //assertTrue(queryResult1.getId().longValue() > 0);
        //
        //queryResult2 = new PatientRecordDTO();
        //queryResult2.setGivenName("John");
        //queryResult2.setFamilyName("Smith");
        //queryResult2.setGender("Male");
        //queryResult2.setOrgPatientId("JSMITH15");
        //queryResult2.setQueryOrganizationId(orgQuery2.getId());
        //queryResult2.setSsn("111223344");
        //queryResult2.setPhoneNumber("5555555555");
        //queryResult2 = prDao.create(queryResult1);
        //assertNotNull(queryResult2);
        //assertNotNull(queryResult2.getId());
        //assertTrue(queryResult2.getId().longValue() > 0);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatient() throws SQLException  {
        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Jon Snow");
        toCreate.setFriendlyName("Bri");
        toCreate.setSsn("111223344");
        toCreate.setGender("Male");

        PatientDTO created = patientManager.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());

        //TODO: why is this coming back empty?? It works when the service is called
        //assertEquals(1, selected.getOrgMaps().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientWithoutFriendlyName() throws SQLException  {
        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Jon Snow");
        toCreate.setSsn("111223344");
        toCreate.setGender("Male");

        PatientDTO created = patientManager.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetPatientsAtAcf() throws SQLException  {
        PatientDTO toCreate = new PatientDTO();
        toCreate.setFriendlyName("Bri");
        toCreate.setAcf(acf);
        toCreate.setFullName("Brian Lindsey");

        patientManager.create(toCreate);

        List<PatientDTO> patients = patientManager.getPatientsAtAcf(acf.getId());
        assertNotNull(patients);
        assertEquals(1, patients.size());
        assertNotNull(patients.get(0).getEndpointMaps());
        //TODO: this should work
        //assertEquals(1, patients.get(0).getOrgMaps().size());
    }
}
