package gov.ca.emsa.pulse.broker.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Assert;
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
import gov.ca.emsa.pulse.broker.dto.PatientGenderDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordAddressDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordNameDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BrokerApplicationTestConfig.class})
public class PatientRecordDaoTest extends TestCase {

    @Autowired QueryDAO queryDao;
    @Autowired PatientRecordAddressDAO addrDao;
    @Autowired LocationDAO locationDao;
    @Autowired AlternateCareFacilityDAO acfDao;
    @Autowired PatientRecordDAO patientRecordDao;
    @Autowired PatientDAO patientDao;
    @Autowired PatientRecordNameDAO prNameDao;
    @Autowired GivenNameDAO givenNameDao;
    @Autowired NameTypeDAO nameTypeDao;
    @Autowired PatientGenderDAO patientGenderDao;
    private AlternateCareFacilityDTO acf;
    private LocationDTO location1, location2;
    private PatientRecordDTO queryResult1, queryResult2;
    private NameTypeDTO nameTypeCodeLegal;
    private PatientGenderDTO patientGenderMale, patientGenderFemale, patientGenderUn;

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

        patientGenderFemale = new PatientGenderDTO();
        patientGenderFemale = patientGenderDao.getByCode("F");
        patientGenderMale = new PatientGenderDTO();
        patientGenderMale = patientGenderDao.getByCode("M");
        patientGenderUn = new PatientGenderDTO();
        patientGenderUn = patientGenderDao.getById(3L);
        nameTypeCodeLegal = new NameTypeDTO();
        nameTypeCodeLegal = nameTypeDao.getByCode("L");
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientRecord() {
        PatientRecordDTO toCreate = new PatientRecordDTO();

        toCreate.setSsn("111223344");
        toCreate.setDateOfBirth("19930502");
        toCreate.setPhoneNumber("4430001111");
        toCreate.setPatientGender(patientGenderMale);

        PatientRecordDTO created = patientRecordDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);

        PatientRecordNameDTO prnDto = new PatientRecordNameDTO();
        prnDto.setFamilyName("Lindsey");
        prnDto.setExpirationDate(new Date());
        prnDto.setPatientRecordId(created.getId());
        prnDto.setNameType(nameTypeCodeLegal);
        PatientRecordNameDTO prnCreated = prNameDao.create(prnDto);

        assertNotNull(prnCreated);
        assertEquals("Lindsey", prnCreated.getFamilyName());

        GivenNameDTO given1 = new GivenNameDTO();
        given1.setGivenName("Brian");
        given1.setPatientRecordNameId(prnCreated.getId());
        GivenNameDTO givenCreated = givenNameDao.create(given1);

        assertNotNull(givenCreated);
        assertEquals("Brian", givenCreated.getGivenName());

        PatientRecordDTO selectedPatientRecord = patientRecordDao.getById(created.getId());
        PatientRecordNameDTO selectedPatientRecordName = prNameDao.getById(prnCreated.getId());
        assertNotNull(selectedPatientRecord);
        assertNotNull(selectedPatientRecordName);
        assertNotNull(selectedPatientRecord.getPatientRecordName());
        assertNotNull(selectedPatientRecordName.getFamilyName());
        //assertNotNull(selectedPatientRecordName.getGivenName().get(0));
        //assertNotNull(selected);
        //assertNotNull(selected.getId());
        //assertEquals("Lindsey", selected.getPatientRecordName());
        //assertEquals("L", selected.getPatientRecordName().get(0).getNameType().getCode());
        //assertEquals("Brian", selected.getPatientRecordName().get(0).getGivenName().get(0).getGivenName());
        //assertTrue(selected.getId().longValue() > 0);
    }


    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientRecordWithNewAddress() {
        String streetLine1 = "1000 Hilltop Circle";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        PatientRecordDTO toCreate = new PatientRecordDTO();

        PatientRecordAddressDTO addrDto = new PatientRecordAddressDTO();
        addrDto.setCity(city);
        addrDto.setState(state);
        addrDto.setZipcode(zip);
        ArrayList<PatientRecordAddressDTO> addresses = new ArrayList<PatientRecordAddressDTO>();
        addresses.add(addrDto);

        toCreate.getAddress().addAll(addresses);

        toCreate.setSsn("111223344");
        toCreate.setPatientGender(patientGenderFemale);
        toCreate.setDateOfBirth("19930502");
        toCreate.setPhoneNumber("4430001111");

        PatientRecordDTO created = patientRecordDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);

        PatientRecordDTO patientRecordCreated = patientRecordDao.getById(created.getId());

        assertNotNull(patientRecordCreated);
        assertNotNull(patientRecordCreated.getAddress());
        assertEquals("21227", patientRecordCreated.getAddress().get(0).getZipcode());
        assertEquals("Baltimore", patientRecordCreated.getAddress().get(0).getCity());
        assertEquals("MD", patientRecordCreated.getAddress().get(0).getState());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testUpdatePatientRecordFirstName() {

        PatientRecordDTO toCreate = new PatientRecordDTO();

        toCreate.setSsn("111223344");
        toCreate.setPatientGender(patientGenderUn);
        toCreate.setDateOfBirth("19930502");
        toCreate.setPhoneNumber("4430001111");

        PatientRecordDTO created = patientRecordDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().longValue() > 0);
        String streetLine1 = "1000 Hilltop Circle";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        PatientRecordAddressDTO addrDto = new PatientRecordAddressDTO();
        addrDto.setCity(city);
        addrDto.setState(state);
        addrDto.setZipcode(zip);
        addrDto.setPatientRecordId(created.getId());
        addrDto = addrDao.create(addrDto);
        Assert.assertNotNull(addrDto);
        Assert.assertNotNull(addrDto.getId());
        Assert.assertTrue(addrDto.getId().longValue() > 0);
        long existingAddrId = addrDto.getId().longValue();

        PatientRecordNameDTO prnDto = new PatientRecordNameDTO();
        prnDto.setFamilyName("Lindsey");
        prnDto.setExpirationDate(new Date());
        prnDto.setPatientRecordId(created.getId());
        prnDto.setNameType(nameTypeCodeLegal);
        PatientRecordNameDTO prnCreated = prNameDao.create(prnDto);

        assertNotNull(prnCreated);
        assertEquals("Lindsey", prnCreated.getFamilyName());

        GivenNameDTO given1 = new GivenNameDTO();
        given1.setGivenName("Brian");
        given1.setPatientRecordNameId(prnCreated.getId());
        GivenNameDTO givenCreated = givenNameDao.create(given1);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testDeletePatientRecord() {
        PatientRecordDTO toCreate = new PatientRecordDTO();
        toCreate.setSsn("111223344");

        toCreate.setDateOfBirth("19930502");
        toCreate.setPhoneNumber("4430001111");

        toCreate.setPatientGender(patientGenderMale);

        PatientRecordDTO created = patientRecordDao.create(toCreate);
        patientRecordDao.delete(created.getId());

        PatientRecordDTO selected = patientRecordDao.getById(created.getId());
        assertNull(selected);

        AlternateCareFacilityDTO selectedAcf = acfDao.getById(acf.getId());
        assertNotNull(selectedAcf);
    }
}
