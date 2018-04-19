package gov.ca.emsa.pulse.broker.manager;

import java.sql.SQLException;
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
import gov.ca.emsa.pulse.broker.dao.AlternateCareFacilityDAO;
import gov.ca.emsa.pulse.broker.dao.EndpointDAO;
import gov.ca.emsa.pulse.broker.dao.LocationDAO;
import gov.ca.emsa.pulse.broker.dao.LocationEndpointMapDAO;
import gov.ca.emsa.pulse.broker.dao.PatientGenderDAO;
import gov.ca.emsa.pulse.broker.dao.PatientRecordDAO;
import gov.ca.emsa.pulse.broker.dao.QueryDAO;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointStatusDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointTypeDTO;
import gov.ca.emsa.pulse.broker.dto.LocationDTO;
import gov.ca.emsa.pulse.broker.dto.LocationStatusDTO;
import gov.ca.emsa.pulse.broker.dto.PatientGenderDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.QueryDTO;
import gov.ca.emsa.pulse.broker.dto.QueryEndpointMapDTO;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.EndpointStatus;
import gov.ca.emsa.pulse.common.domain.EndpointType;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import gov.ca.emsa.pulse.common.domain.QueryStatus;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BrokerApplicationTestConfig.class})
public class PatientRecordManagerTest extends TestCase {
    @Autowired QueryManager queryManager;
    @Autowired QueryDAO queryDao;
    @Autowired LocationDAO locationDao;
    @Autowired EndpointDAO endpointDao;
    @Autowired LocationEndpointMapDAO mappingDao;
    @Autowired AlternateCareFacilityDAO acfDao;
    @Autowired PatientRecordDAO prDao;
    @Autowired PatientGenderDAO patientGenderDao;
    private AlternateCareFacilityDTO acf;
    private LocationDTO location1;
    private EndpointDTO endpoint1;
    private QueryDTO query;
    private QueryEndpointMapDTO queryEndpointMap1, queryEndpointMap2;
    private PatientGenderDTO patientGenderMale;

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

        mappingDao.create(location1.getId(), endpoint1.getId());

        QueryDTO toInsert = new QueryDTO();
        toInsert.setStatus(QueryStatus.Active);
        toInsert.setTerms("terms");
        toInsert.setUserId("kekey");

        queryEndpointMap1 = new QueryEndpointMapDTO();
        queryEndpointMap1.setEndpointId(endpoint1.getId());
        queryEndpointMap1.setStatus(QueryEndpointStatus.Active);
        toInsert.getEndpointMaps().add(queryEndpointMap1);

        queryEndpointMap2 = new QueryEndpointMapDTO();
        queryEndpointMap2.setEndpointId(endpoint1.getId());
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

        patientGenderMale = new PatientGenderDTO();
        patientGenderMale = patientGenderDao.getById(2L);

        query = queryManager.createQuery(toInsert);
        assertNotNull(query);
        assertNotNull(query.getId());
        assertTrue(query.getId().longValue() > 0);
        assertNotNull(query.getEndpointMaps());
        assertEquals(2, query.getEndpointMaps().size());
        queryEndpointMap1 = query.getEndpointMaps().get(0);
        assertNotNull(query.getEndpointMaps().get(0).getId());
        assertTrue(query.getEndpointMaps().get(0).getId().longValue() > 0);
        queryEndpointMap2 = query.getEndpointMaps().get(1);
        assertNotNull(query.getEndpointMaps().get(1).getId());
        assertTrue(query.getEndpointMaps().get(1).getId().longValue() > 0);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreatePatientRecord() {
        PatientRecordDTO dto = new PatientRecordDTO();
        String date = "20160110";
        dto.setDateOfBirth(date);
        dto.setPhoneNumber("443-745-0888");
        dto.setQueryEndpointId(queryEndpointMap1.getId());
        dto.setSsn("555-55-5555");

        dto.setPatientGender(patientGenderMale);
        PatientRecordDTO added = queryManager.addPatientRecord(dto);

        assertNotNull(added);
        assertNotNull(added.getId());
        assertTrue(added.getDateOfBirth().equals(date));

        PatientRecordDTO selected = queryManager.getPatientRecordById(added.getId());
        assertNotNull(added);
        assertEquals(added.getId(), selected.getId());
        assertTrue(selected.getDateOfBirth().equals(date));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCancelPatientDiscoveryQueryToOrganization() {
        queryManager.cancelQueryToEndpoint(query.getId(), endpoint1.getId());
        QueryDTO updatedQuery = queryManager.getById(query.getId());

        assertNotNull(updatedQuery);
        assertEquals(query.getId(), updatedQuery.getId());
        assertEquals(2, updatedQuery.getEndpointMaps().size());
        boolean queryHadEndpoint = false;
        for(QueryEndpointMapDTO queryEndpointMap : updatedQuery.getEndpointMaps()) {
            assertNotNull(queryEndpointMap.getId());
            if(queryEndpointMap.getId().longValue() == queryEndpointMap1.getId().longValue()) {
                queryHadEndpoint = true;
                assertEquals(QueryEndpointStatus.Cancelled, queryEndpointMap.getStatus());
            }
        }
        assertTrue(queryHadEndpoint);
    }
}
