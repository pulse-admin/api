package gov.ca.emsa.pulse.broker.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.ca.emsa.pulse.broker.dao.NameAssemblyDAO;
import gov.ca.emsa.pulse.broker.dao.NameRepresentationDAO;
import gov.ca.emsa.pulse.broker.dao.NameTypeDAO;
import gov.ca.emsa.pulse.common.domain.Address;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.EndpointMimeType;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.PatientRecord;
import gov.ca.emsa.pulse.common.domain.PatientRecordName;
import gov.ca.emsa.pulse.common.domain.PulseUser;

public class DomainToDtoConverter {
    private static final Logger logger = LogManager.getLogger(DomainToDtoConverter.class);
    @Autowired static NameTypeDAO nameTypeDao;
    @Autowired static NameRepresentationDAO nameRepDao;
    @Autowired static NameAssemblyDAO nameAssemblyDao;

    public static PulseUserDTO convertToPulseUser(PulseUser pulseUser) {
        PulseUserDTO result = new PulseUserDTO();
        if (pulseUser.getId() != null) {
            result.setId(new Long(pulseUser.getId()));
        }
        result.setAssertion(pulseUser.getAssertion());

        return result;
    }

    public static PatientDTO convertToPatient(Patient domainObj) {
        PatientDTO result = new PatientDTO();
        if (domainObj.getId() != null) {
            result.setId(new Long(domainObj.getId()));
        }
        result.setFriendlyName(domainObj.getFriendlyName());
        result.setFullName(domainObj.getFullName());
        result.setGender(domainObj.getGender());
        result.setDateOfBirth(domainObj.getDateOfBirth());
        result.setSsn(domainObj.getSsn());
        result.setCreationDate(domainObj.getCreationDate());
        if (domainObj.getAcf() != null) {
            AlternateCareFacilityDTO acf = convert(domainObj.getAcf());
            result.setAcf(acf);
        }

        return result;
    }

    public static PatientRecordDTO convertToPatientRecord(PatientRecord domainObj) {
        PatientRecordDTO result = new PatientRecordDTO();
        if (domainObj.getId() != null) {
            result.setId(new Long(domainObj.getId()));
        }
        result.setEndpointPatientRecordId(domainObj.getLocationPatientRecordId());
        if (domainObj.getPatientRecordName() != null) {
            for (PatientRecordName patientRecordName : domainObj.getPatientRecordName()) {
                PatientRecordNameDTO patientRecordNameDTO = new PatientRecordNameDTO();
                patientRecordNameDTO.setFamilyName(patientRecordName.getFamilyName());
                ArrayList<GivenNameDTO> givens = new ArrayList<GivenNameDTO>();
                for (String given : patientRecordName.getGivenName()) {
                    GivenNameDTO givenName = new GivenNameDTO();
                    givenName.setGivenName(given);
                    givens.add(givenName);
                }
                patientRecordNameDTO.setGivenName(givens);
                if (patientRecordName.getSuffix() != null)
                    patientRecordNameDTO.setSuffix(patientRecordName.getSuffix());
                if (patientRecordName.getPrefix() != null)
                    patientRecordNameDTO.setPrefix(patientRecordName.getPrefix());
                if (patientRecordName.getNameType() != null) {
                    NameTypeDTO nameType = nameTypeDao.getByCode(patientRecordName.getNameType().getCode());
                    patientRecordNameDTO.setNameType(nameType);
                    patientRecordNameDTO.setNameTypeId(nameType.getId());
                }
                if (patientRecordName.getNameRepresentation() != null) {
                    NameRepresentationDTO nameRep = nameRepDao
                            .getByCode(patientRecordName.getNameRepresentation().getCode());
                    patientRecordNameDTO.setNameRepresentation(nameRep);
                    patientRecordNameDTO.setNameTypeId(nameRep.getId());
                }
                if (patientRecordName.getNameAssembly() != null) {
                    NameAssemblyDTO nameAssembly = nameAssemblyDao
                            .getByCode(patientRecordName.getNameAssembly().getCode());
                    patientRecordNameDTO.setNameAssembly(nameAssembly);
                    patientRecordNameDTO.setNameTypeId(nameAssembly.getId());
                }
                if (patientRecordName.getEffectiveDate() != null)
                    patientRecordNameDTO.setEffectiveDate(patientRecordName.getEffectiveDate());
                if (patientRecordName.getExpirationDate() != null)
                    patientRecordNameDTO.setExpirationDate(patientRecordName.getExpirationDate());
                result.getPatientRecordName().add(patientRecordNameDTO);
            }
        }
        PatientGenderDTO pgDto = new PatientGenderDTO();
        pgDto.setCode(domainObj.getGender().getCode());
        pgDto.setDescription(domainObj.getGender().getDescription());
        result.setPatientGender(pgDto);
        result.setDateOfBirth(domainObj.getDateOfBirth());

        result.setPhoneNumber(domainObj.getPhoneNumber());
        result.setSsn(domainObj.getSsn());
        result.setHomeCommunityId(domainObj.getHomeCommunityId());

        List<PatientRecordAddressDTO> praDto = new ArrayList<PatientRecordAddressDTO>();
        for (Address pra : domainObj.getAddress()) {
            PatientRecordAddressDTO address = new PatientRecordAddressDTO();
            List<PatientRecordAddressLineDTO> lines = new ArrayList<PatientRecordAddressLineDTO>();
            for (int i = 0; i < pra.getLines().size(); i++) {
                PatientRecordAddressLineDTO pralDto = new PatientRecordAddressLineDTO();
                pralDto.setLine(pra.getLines().get(i));
                pralDto.setLineOrder(i);
                lines.add(pralDto);
            }
            address.setPatientRecordAddressLines(lines);
            address.setCity(pra.getCity());
            address.setState(pra.getState());
            address.setZipcode(pra.getZipcode());
            praDto.add(address);
        }
        result.setAddress(praDto);

        return result;
    }

    public static DocumentDTO convert(Document domainObj) {
        DocumentDTO result = new DocumentDTO();
        result.setStatus(domainObj.getStatus());
        result.setName(domainObj.getName());
        result.setFormat(domainObj.getFormat());
        result.setClassName(domainObj.getClassName());
        result.setConfidentiality(domainObj.getConfidentiality());
        result.setCreationTime(domainObj.getCreationTime());
        result.setDescription(domainObj.getDescription());
        result.setSize(domainObj.getSize());
        result.setPatientEndpointMapId(domainObj.getEndpointMapId());

        if (domainObj.getIdentifier() != null) {
            result.setDocumentUniqueId(domainObj.getIdentifier().getDocumentUniqueId());
            result.setHomeCommunityId(domainObj.getIdentifier().getHomeCommunityId());
            result.setRepositoryUniqueId(domainObj.getIdentifier().getRepositoryUniqueId());
        }
        return result;
    }

    /**
     * Converts domain object to DTO.
     * 
     * @param domainObj
     *            incoming ACF domain object
     * @return DTO containing ACF
     */
    public static AlternateCareFacilityDTO convert(final AlternateCareFacility domainObj) {
        AlternateCareFacilityDTO result = new AlternateCareFacilityDTO();
        result.setId(domainObj.getId());
        result.setIdentifier(domainObj.getIdentifier());
        result.setLiferayStateId(domainObj.getLiferayStateId());
        result.setLiferayAcfId(domainObj.getLiferayAcfId());
        result.setName(domainObj.getName());
        result.setPhoneNumber(domainObj.getPhoneNumber());
        if (domainObj.getAddress() != null) {
            if (domainObj.getAddress().getLines() != null) {
                for (String line : domainObj.getAddress().getLines()) {
                    AddressLineDTO lineDto = new AddressLineDTO();
                    lineDto.setLine(line);
                    result.getLines().add(lineDto);
                }
            }
            result.setCity(domainObj.getAddress().getCity());
            result.setState(domainObj.getAddress().getState());
            result.setZipcode(domainObj.getAddress().getZipcode());
        }
        return result;
    }

    public static LocationDTO convert(Location domain) {
        LocationDTO result = new LocationDTO();
        result.setId(domain.getId());
        result.setName(domain.getName());

        if (domain.getAddress() != null) {
            if (domain.getAddress().getLines() != null) {
                for (String line : domain.getAddress().getLines()) {
                    AddressLineDTO addrLine = new AddressLineDTO();
                    addrLine.setLine(line);
                    result.getLines().add(addrLine);
                }
            }
            result.setCity(domain.getAddress().getCity());
            result.setState(domain.getAddress().getState());
            result.setZipcode(domain.getAddress().getZipcode());
        }
        if (domain.getStatus() != null) {
            LocationStatusDTO status = new LocationStatusDTO();
            status.setId(domain.getStatus().getId());
            status.setName(domain.getStatus().getName());
            result.setStatus(status);
        }
        result.setDescription(domain.getDescription());
        result.setExternalId(domain.getExternalId());
        result.setParentOrgName(domain.getParentOrgName());
        result.setType(domain.getType());
        result.setExternalLastUpdateDate(domain.getExternalLastUpdateDate());
        result.setLastModifiedDate(domain.getLastModifiedDate());
        result.setCreationDate(domain.getCreationDate());
        return result;
    }

    public static EndpointDTO convert(Endpoint domain) {
        EndpointDTO result = new EndpointDTO();
        result.setId(domain.getId());
        result.setAdapter(domain.getAdapter());
        result.setOrganizationId(domain.getOrganizationId());
        result.setManagingOrganization(domain.getManagingOrganization());
        if (domain.getEndpointStatus() != null) {
            EndpointStatusDTO status = new EndpointStatusDTO();
            status.setId(domain.getEndpointStatus().getId());
            status.setName(domain.getEndpointStatus().getName());
            result.setEndpointStatus(status);
        }
        if (domain.getEndpointType() != null) {
            EndpointTypeDTO type = new EndpointTypeDTO();
            type.setId(domain.getEndpointType().getId());
            type.setName(domain.getEndpointType().getName());
            type.setCode(domain.getEndpointType().getCode());
            result.setEndpointType(type);
        }
        result.setExternalId(domain.getExternalId());
        if (domain.getMimeTypes() != null && domain.getMimeTypes().size() > 0) {
            for (EndpointMimeType domainMimeType : domain.getMimeTypes()) {
                result.getMimeTypes().add(convert(domainMimeType));
            }
        }

        if (domain.getLocations() != null && domain.getLocations().size() > 0) {
            for (Location location : domain.getLocations()) {
                result.getLocations().add(convert(location));
            }
        }
        result.setPayloadType(domain.getPayloadType());
        result.setPublicKey(domain.getPublicKey());
        result.setUrl(domain.getUrl());
        result.setLastModifiedDate(domain.getLastModifiedDate());
        result.setExternalLastUpdateDate(domain.getExternalLastUpdateDate());
        result.setCreationDate(domain.getCreationDate());

        return result;
    }

    public static EndpointMimeTypeDTO convert(EndpointMimeType domain) {
        EndpointMimeTypeDTO result = new EndpointMimeTypeDTO();
        result.setId(domain.getId());
        result.setMimeType(domain.getMimeType());
        return result;
    }
}
