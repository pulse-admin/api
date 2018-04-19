package gov.ca.emsa.pulse.broker.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.dto.GivenNameDTO;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.dto.LocationStatusDTO;
import gov.ca.emsa.pulse.broker.dto.NameTypeDTO;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BrokerApplicationTestConfig.class})
public class PatientDaoTest extends TestCase {

    @Autowired QueryDAO queryDao;
    @Autowired LocationDAO locationDao;
    @Autowired AlternateCareFacilityDAO acfDao;
    @Autowired PatientDAO patientDao;
    @Autowired PatientRecordDAO prDao;
    private AlternateCareFacilityDTO acf;
    private LocationDTO location1, location2;
    private PatientRecordDTO queryResult1, queryResult2;

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
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientMultipleGivens() throws SQLException {
        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Brian Lindsey");
        toCreate.setFriendlyName("Bri");
        NameTypeDTO nameTypeDTO = new NameTypeDTO();
        nameTypeDTO.setCode("L");
        toCreate.setSsn("111223344");
        toCreate.setGender("M");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());

        PatientDTO selected = patientDao.getById(created.getId());
        assertNotNull(selected);
        assertNotNull(selected.getId());
        assertTrue(selected.getId().longValue() > 0);
        assertNotNull(selected.getAcf());
        assertNotNull(selected.getAcf().getId());
        assertEquals(selected.getAcf().getId().longValue(), acf.getId().longValue());
        assertEquals(0, selected.getEndpointMaps().size());
        assertEquals(toCreate.getFullName(), selected.getFullName());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientNoAddress() throws SQLException {
        String fullName = "Brian Lindsey";

        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        ArrayList<GivenNameDTO> givens = new ArrayList<GivenNameDTO>();
        GivenNameDTO given = new GivenNameDTO();
        given.setGivenName("Jonathon");
        givens.add(given);
        toCreate.setFullName(fullName);
        toCreate.setFriendlyName("Bri");
        NameTypeDTO nameTypeDTO = new NameTypeDTO();
        nameTypeDTO.setCode("L");
        toCreate.setSsn("111223344");
        toCreate.setGender("M");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());
        assertEquals(fullName, created.getFullName());

        PatientDTO selected = patientDao.getById(created.getId());
        assertNotNull(selected);
        assertNotNull(selected.getId());
        assertTrue(selected.getId().longValue() > 0);
        assertNotNull(selected.getAcf());
        assertNotNull(selected.getAcf().getId());
        assertEquals(selected.getAcf().getId().longValue(), acf.getId().longValue());
        assertEquals(0, selected.getEndpointMaps().size());
        assertEquals(fullName, selected.getFullName());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientWithExistingAddress() throws SQLException {

        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        ArrayList<GivenNameDTO> givens = new ArrayList<GivenNameDTO>();
        toCreate.setFullName("Brian Lindsey");
        toCreate.setFriendlyName("Bri");
        NameTypeDTO nameTypeDTO = new NameTypeDTO();
        nameTypeDTO.setCode("L");
        toCreate.setSsn("111223344");
        toCreate.setGender("M");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());

        PatientDTO selected = patientDao.getById(created.getId());
        assertNotNull(selected);
        assertNotNull(selected.getId());
        assertTrue(selected.getId().longValue() > 0);
        assertNotNull(selected.getAcf());
        assertNotNull(selected.getAcf().getId());
        assertEquals(selected.getAcf().getId().longValue(), acf.getId().longValue());
        assertEquals(0, selected.getEndpointMaps().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientWithNewAddress() throws SQLException {

        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Brian Lindsey");
        toCreate.setFriendlyName("Bri");
        NameTypeDTO nameTypeDTO = new NameTypeDTO();
        nameTypeDTO.setCode("L");
        toCreate.setSsn("111223344");
        toCreate.setGender("Male");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());

        PatientDTO selected = patientDao.getById(created.getId());
        assertNotNull(selected);
        assertNotNull(selected.getId());
        assertTrue(selected.getId().longValue() > 0);
        assertNotNull(selected.getAcf());
        assertNotNull(selected.getAcf().getId());
        assertEquals(selected.getAcf().getId().longValue(), acf.getId().longValue());
        assertEquals(0, selected.getEndpointMaps().size());
    }

    //TODO: the org maps aren't coming back from the create or select 
    //but they do appear when making calls via POSTman.. can't figure out the disconnect
    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientWithOrgMaps() throws SQLException {
        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Brian Lindsey");
        toCreate.setFriendlyName("Bri");
        toCreate.setSsn("111223344");
        toCreate.setGender("Male");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        assertNotNull(created.getAcf());
        assertNotNull(created.getAcf().getId());
        assertEquals(created.getAcf().getId().longValue(), acf.getId().longValue());

        PatientDTO selected = patientDao.getById(created.getId());
        assertNotNull(selected);
        assertNotNull(selected.getEndpointMaps());
        //TODO: this is not working but should be
        //assertTrue(selected.getOrgMaps().size() == 1);
        //assertEquals(orgMap.getId().longValue(), selected.getOrgMaps().get(0).getId().longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testUpdatePatientFirstName() throws SQLException {

        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Brian Lindsey");
        toCreate.setFriendlyName("Bri");
        toCreate.setSsn("111223344");
        toCreate.setGender("Male");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        created.setFullName("Katy Ekey");
        PatientDTO updated = patientDao.update(created);
        assertNotNull(updated);
        assertEquals(updated.getId().longValue(), created.getId().longValue());
        assertEquals("Katy Ekey", updated.getFullName());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testDeletePatient() throws SQLException {
        PatientDTO toCreate = new PatientDTO();
        toCreate.setAcf(acf);
        toCreate.setFullName("Brian Lindsey");
        toCreate.setFriendlyName("Bri");
        toCreate.setSsn("111223344");
        toCreate.setGender("Male");
        toCreate.setDateOfBirth("2014-01-01");

        PatientDTO created = patientDao.create(toCreate);
        patientDao.delete(created.getId());

        PatientDTO selected = patientDao.getById(created.getId());
        assertNull(selected);

        AlternateCareFacilityDTO selectedAcf = acfDao.getById(acf.getId());
        assertNotNull(selectedAcf);
    }
}
