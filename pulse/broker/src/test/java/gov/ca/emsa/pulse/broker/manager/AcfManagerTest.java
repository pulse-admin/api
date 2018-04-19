package gov.ca.emsa.pulse.broker.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import gov.ca.emsa.pulse.broker.BrokerApplicationTestConfig;
import gov.ca.emsa.pulse.broker.cache.CacheCleanupException;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import gov.ca.emsa.pulse.broker.dto.AddressLineDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BrokerApplicationTestConfig.class})
public class AcfManagerTest {

    @Autowired AlternateCareFacilityManager acfManager;

    @Test
    @Transactional
    @Rollback(true)
    public void createAcfNoAddress() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto = acfManager.create(dto);

        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertNull(dto.getCity());
        Assert.assertNull(dto.getState());
        Assert.assertNull(dto.getZipcode());
        Assert.assertTrue(dto.getLines() == null || dto.getLines().size() == 0);

        dto = acfManager.getById(dto.getId());
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertNull(dto.getCity());
        Assert.assertNull(dto.getState());
        Assert.assertNull(dto.getZipcode());
        Assert.assertTrue(dto.getLines() == null || dto.getLines().size() == 0);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createDuplicateAcf() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto = acfManager.create(dto);

        AlternateCareFacilityDTO dup = new AlternateCareFacilityDTO();
        dup.setIdentifier(identifier);
        dup.setLiferayStateId(1L);
        dup.setLiferayAcfId(2L);
        dup = acfManager.create(dup);

        Assert.assertEquals(dto.getId(), dup.getId());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createAcfWithAddress() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";
        String streetLine1 = "1000 Hilltop Circle";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto.setCity(city);
        dto.setState(state);
        dto.setZipcode(zip);
        AddressLineDTO addressLine = new AddressLineDTO();
        addressLine.setLine(streetLine1);
        List<AddressLineDTO> lines = new ArrayList<AddressLineDTO>();
        lines.add(addressLine);
        dto.setLines(lines);
        dto = acfManager.create(dto);

        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertEquals(city, dto.getCity());
        Assert.assertEquals(state, dto.getState());
        Assert.assertEquals(zip, dto.getZipcode());
        Assert.assertNotNull(dto.getLines());
        Assert.assertEquals(1, dto.getLines().size());
        Assert.assertEquals(streetLine1, dto.getLines().get(0).getLine());

        dto = acfManager.getById(dto.getId());
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertEquals(city, dto.getCity());
        Assert.assertEquals(state, dto.getState());
        Assert.assertEquals(zip, dto.getZipcode());
        Assert.assertNotNull(dto.getLines());
        Assert.assertEquals(1, dto.getLines().size());
        Assert.assertEquals(streetLine1, dto.getLines().get(0).getLine());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateAcfName() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto = acfManager.create(dto);
        Long acfId = dto.getId();

        Assert.assertNotNull(dto);
        Assert.assertNotNull(acfId);
        Assert.assertTrue(acfId.longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertNull(dto.getName());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());

        String name = "M&T Bank Stadium";
        dto.setName(name);
        dto = acfManager.updateAcfDetails(dto);
        Assert.assertEquals(name, dto.getName());

        dto = acfManager.getById(dto.getId());
        Assert.assertEquals(name, dto.getName());

        List<AlternateCareFacilityDTO> nameMatches = acfManager.getByName(name);
        Assert.assertNotNull(nameMatches);
        Assert.assertEquals(1, nameMatches.size());
        Assert.assertNotNull(nameMatches.get(0));
        Assert.assertEquals(acfId.longValue(), nameMatches.get(0).getId().longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateAcfPhoneNumber() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";
        String updatedPhoneNumber = "3015551000";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto = acfManager.create(dto);

        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());

        dto.setPhoneNumber(updatedPhoneNumber);
        dto = acfManager.updateAcfDetails(dto);
        Assert.assertEquals(updatedPhoneNumber, dto.getPhoneNumber());

        dto = acfManager.getById(dto.getId());
        Assert.assertEquals(updatedPhoneNumber, dto.getPhoneNumber());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateAcfAddStreetLineToAddress() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";
        String streetLine1 = "1000 Hilltop Circle";
        String streetLine2 = "APT 2B";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto.setCity(city);
        dto.setState(state);
        dto.setZipcode(zip);
        AddressLineDTO addressLine = new AddressLineDTO();
        addressLine.setLine(streetLine1);
        List<AddressLineDTO> lines = new ArrayList<AddressLineDTO>();
        lines.add(addressLine);
        dto.setLines(lines);
        dto = acfManager.create(dto);

        dto = acfManager.getById(dto.getId());
        AddressLineDTO addressLine2 = new AddressLineDTO();
        addressLine2.setLine(streetLine2);
        dto.getLines().add(addressLine2);

        dto = acfManager.updateAcfDetails(dto);
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertEquals(city, dto.getCity());
        Assert.assertEquals(state, dto.getState());
        Assert.assertEquals(zip, dto.getZipcode());
        Assert.assertNotNull(dto.getLines());
        Assert.assertEquals(2, dto.getLines().size());
        Assert.assertEquals(streetLine1, dto.getLines().get(0).getLine());
        Assert.assertEquals(streetLine2, dto.getLines().get(1).getLine());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateAcfRemoveStreetLineFromAddress() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";
        String streetLine1 = "1000 Hilltop Circle";
        String streetLine2 = "APT 2B";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto.setCity(city);
        dto.setState(state);
        dto.setZipcode(zip);
        AddressLineDTO addressLine = new AddressLineDTO();
        addressLine.setLine(streetLine1);
        AddressLineDTO addressLine2 = new AddressLineDTO();
        addressLine2.setLine(streetLine2);
        List<AddressLineDTO> lines = new ArrayList<AddressLineDTO>();
        lines.add(addressLine);
        lines.add(addressLine2);
        dto.setLines(lines);
        dto = acfManager.create(dto);

        dto = acfManager.getById(dto.getId());
        Assert.assertEquals(2, dto.getLines().size());
        Assert.assertEquals(streetLine1, dto.getLines().get(0).getLine());
        Assert.assertEquals(streetLine2, dto.getLines().get(1).getLine());

        dto.getLines().remove(1);
        dto = acfManager.updateAcfDetails(dto);
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertEquals(city, dto.getCity());
        Assert.assertEquals(state, dto.getState());
        Assert.assertEquals(zip, dto.getZipcode());
        Assert.assertNotNull(dto.getLines());
        Assert.assertEquals(1, dto.getLines().size());
        Assert.assertEquals(streetLine1, dto.getLines().get(0).getLine());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateAcfChangeStreetLineInAddress() throws SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";
        String streetLine1 = "1000 Hilltop Circle";
        String streetLine2 = "APT 2B";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        AlternateCareFacilityDTO dto = new AlternateCareFacilityDTO();
        dto.setIdentifier(identifier);
        dto.setLiferayStateId(1L);
        dto.setLiferayAcfId(2L);
        dto.setPhoneNumber(phoneNumber);
        dto.setCity(city);
        dto.setState(state);
        dto.setZipcode(zip);
        AddressLineDTO addressLine = new AddressLineDTO();
        addressLine.setLine(streetLine1);
        AddressLineDTO addressLine2 = new AddressLineDTO();
        addressLine2.setLine(streetLine2);
        List<AddressLineDTO> lines = new ArrayList<AddressLineDTO>();
        lines.add(addressLine);
        lines.add(addressLine2);
        dto.setLines(lines);
        dto = acfManager.create(dto);

        dto = acfManager.getById(dto.getId());
        Assert.assertEquals(2, dto.getLines().size());
        Assert.assertEquals(streetLine1, dto.getLines().get(0).getLine());
        Assert.assertEquals(streetLine2, dto.getLines().get(1).getLine());

        String updatedAddressLine1 = "Updated address line.";
        dto.getLines().get(0).setLine(updatedAddressLine1);
        dto = acfManager.updateAcfDetails(dto);
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(dto.getId().longValue() > 0);
        Assert.assertEquals(identifier, dto.getIdentifier());
        Assert.assertEquals(phoneNumber, dto.getPhoneNumber());
        Assert.assertEquals(city, dto.getCity());
        Assert.assertEquals(state, dto.getState());
        Assert.assertEquals(zip, dto.getZipcode());
        Assert.assertNotNull(dto.getLines());
        Assert.assertEquals(2, dto.getLines().size());
        Assert.assertEquals(updatedAddressLine1, dto.getLines().get(0).getLine());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCleanupOldAcfs() throws CacheCleanupException, SQLException  {
        String identifier = "ACF 1";
        String phoneNumber = "4105551000";

        String identifier2 = "ACF 2";
        String streetLine1 = "1000 Hilltop Circle";
        String streetLine2 = "ABT 5";
        String city = "Baltimore";
        String state = "MD";
        String zip = "21227";

        AlternateCareFacilityDTO dto1 = new AlternateCareFacilityDTO();
        dto1.setIdentifier(identifier);
        dto1.setLiferayStateId(1L);
        dto1.setLiferayAcfId(2L);
        dto1.setPhoneNumber(phoneNumber);
        dto1 = acfManager.create(dto1);

        AlternateCareFacilityDTO dto2 = new AlternateCareFacilityDTO();
        dto2.setIdentifier(identifier2);
        dto2.setLiferayStateId(1L);
        dto2.setLiferayAcfId(2L);
        dto2.setCity(city);
        dto2.setState(state);
        dto2.setZipcode(zip);
        AddressLineDTO addressLine = new AddressLineDTO();
        addressLine.setLine(streetLine1);
        AddressLineDTO addressLine2 = new AddressLineDTO();
        addressLine2.setLine(streetLine2);
        List<AddressLineDTO> lines = new ArrayList<AddressLineDTO>();
        lines.add(addressLine);
        lines.add(addressLine2);
        dto2.setLines(lines);
        dto2 = acfManager.create(dto2);

        //should delete neither
        Date cleanupIfOlderThan = new Date(System.currentTimeMillis()-60000);
        acfManager.cleanupCache(cleanupIfOlderThan);
        dto1 = acfManager.getById(dto1.getId());
        Assert.assertNotNull(dto1);

        Assert.assertNotNull(dto2.getCity());
        Assert.assertEquals(2, dto2.getLines().size());
        dto2 = acfManager.getById(dto2.getId());
        Assert.assertNotNull(dto2);

        //should delete both
        cleanupIfOlderThan = new Date(System.currentTimeMillis()+1000);
        acfManager.cleanupCache(cleanupIfOlderThan);

        dto1 = acfManager.getById(dto1.getId());
        Assert.assertNull(dto1);

        dto2 = acfManager.getById(dto2.getId());
        Assert.assertNull(dto2);
    }
}
