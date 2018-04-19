package gov.ca.emsa.pulse.broker.dto;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.emsa.pulse.broker.domain.DocumentAudit;
import gov.ca.emsa.pulse.common.domain.Address;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.DocumentIdentifier;
import gov.ca.emsa.pulse.common.domain.Endpoint;
import gov.ca.emsa.pulse.common.domain.EndpointMimeType;
import gov.ca.emsa.pulse.common.domain.EndpointStatus;
import gov.ca.emsa.pulse.common.domain.EndpointType;
import gov.ca.emsa.pulse.common.domain.Location;
import gov.ca.emsa.pulse.common.domain.LocationStatus;
import gov.ca.emsa.pulse.common.domain.NameAssembly;
import gov.ca.emsa.pulse.common.domain.NameRepresentation;
import gov.ca.emsa.pulse.common.domain.NameType;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.PatientEndpointMap;
import gov.ca.emsa.pulse.common.domain.PatientGender;
import gov.ca.emsa.pulse.common.domain.PatientRecord;
import gov.ca.emsa.pulse.common.domain.PatientRecordName;
import gov.ca.emsa.pulse.common.domain.PatientSearch;
import gov.ca.emsa.pulse.common.domain.PulseUser;
import gov.ca.emsa.pulse.common.domain.Query;
import gov.ca.emsa.pulse.common.domain.QueryEndpointMap;

public class DtoToDomainConverter {
    private static final Logger logger = LogManager.getLogger(DtoToDomainConverter.class);
    private static final DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static PulseUser convertToPulseUser(PulseUserDTO pulseUser) {
        PulseUser result = new PulseUser();
        result.setId(pulseUser.getId());
        result.setAssertion(pulseUser.getAssertion());

        return result;
    }

    public static Patient convert(PatientDTO dtoObj) {
        Patient result = new Patient();
        result.setId(dtoObj.getId());
        result.setFullName(dtoObj.getFullName());
        result.setFriendlyName(dtoObj.getFriendlyName());
        result.setGender(dtoObj.getGender());
        result.setDateOfBirth(dtoObj.getDateOfBirth());
        result.setSsn(dtoObj.getSsn());
        result.setLastRead(dtoObj.getLastReadDate());
        result.setCreationDate(dtoObj.getCreationDate());
        if (dtoObj.getAcf() != null) {
            AlternateCareFacility acf = convert(dtoObj.getAcf());
            result.setAcf(acf);
        }

        if (dtoObj.getEndpointMaps() != null && dtoObj.getEndpointMaps().size() > 0) {
            for (PatientEndpointMapDTO orgMapDto : dtoObj.getEndpointMaps()) {
                PatientEndpointMap orgMap = DtoToDomainConverter.convert(orgMapDto);
                result.getEndpointMaps().add(orgMap);
            }
        }

        return result;
    }

    public static PatientEndpointMap convert(PatientEndpointMapDTO dto) {
        PatientEndpointMap result = new PatientEndpointMap();
        result.setId(dto.getId());
        result.setPatientId(dto.getPatientId());
        if (dto.getEndpoint() != null) {
            result.setEndpoint(DtoToDomainConverter.convert(dto.getEndpoint()));
        } else {
            Endpoint endpoint = new Endpoint();
            endpoint.setId(dto.getEndpointId());
            result.setEndpoint(endpoint);
        }
        result.setHomeCommunityId(dto.getHomeCommunityId());
        result.setDocumentsQueryStatus(dto.getDocumentsQueryStatus());
        result.setDocumentsQueryStart(dto.getDocumentsQueryStart());
        result.setDocumentsQueryEnd(dto.getDocumentsQueryEnd());

        if (dto.getDocuments() != null && dto.getDocuments().size() > 0) {
            for (DocumentDTO docDto : dto.getDocuments()) {
                Document doc = DtoToDomainConverter.convert(docDto);
                result.getDocuments().add(doc);
            }
        }
        return result;
    }

    public static AlternateCareFacility convert(AlternateCareFacilityDTO acfDto) {
        AlternateCareFacility acf = new AlternateCareFacility();
        acf.setId(acfDto.getId());
        acf.setIdentifier(acfDto.getIdentifier());
        acf.setName(acfDto.getName());
        acf.setPhoneNumber(acfDto.getPhoneNumber());
        acf.setLastRead(acfDto.getLastReadDate());
        acf.setLiferayAcfId(acfDto.getLiferayAcfId());
        acf.setLiferayStateId(acfDto.getLiferayStateId());

        if (acfDto.hasAddressParts()) {
            Address acfAddr = new Address();
            if (acfDto.getLines() != null) {
                for (AddressLineDTO lineDto : acfDto.getLines()) {
                    ;
                    acfAddr.getLines().add(lineDto.getLine());
                }
            }
            acfAddr.setCity(acfDto.getCity());
            acfAddr.setState(acfDto.getState());
            acfAddr.setZipcode(acfDto.getZipcode());
            acf.setAddress(acfAddr);
        }
        return acf;
    }

    public static Query convert(QueryDTO queryDto) {
        Query query = new Query();
        query.setId(queryDto.getId());
        query.setStatus(queryDto.getStatus());
        query.setLastRead(queryDto.getLastReadDate());

        try {
            ObjectMapper termReader = new ObjectMapper();
            PatientSearch terms = termReader.readValue(queryDto.getTerms(), PatientSearch.class);
            query.setTerms(terms);
        } catch (IOException ioex) {
            logger.error("Could not read " + queryDto.getTerms() + " as JSON.");
        }

        query.setUserToken(queryDto.getUserId());
        for (QueryEndpointMapDTO qOrgDto : queryDto.getEndpointMaps()) {
            QueryEndpointMap qOrg = DtoToDomainConverter.convert(qOrgDto);
            query.getEndpointStatuses().add(qOrg);
        }
        return query;
    }

    public static QueryEndpointMap convert(QueryEndpointMapDTO qOrgDto) {
        QueryEndpointMap qOrg = new QueryEndpointMap();
        qOrg.setId(qOrgDto.getId());

        if (qOrgDto.getEndpoint() != null) {
            qOrg.setEndpoint(convert(qOrgDto.getEndpoint()));
        }

        qOrg.setQueryId(qOrgDto.getQueryId());
        for (PatientRecordDTO prDto : qOrgDto.getResults()) {
            PatientRecord pr = DtoToDomainConverter.convert(prDto);
            qOrg.getResults().add(pr);
        }
        qOrg.setStartDate(qOrgDto.getStartDate());
        qOrg.setEndDate(qOrgDto.getEndDate());
        qOrg.setStatus(qOrgDto.getStatus());
        return qOrg;
    }

    public static PatientRecord convert(PatientRecordDTO prDto) {
        PatientRecord pr = new PatientRecord();
        pr.setId(prDto.getId());
        pr.setSsn(prDto.getSsn());
        if (prDto.getPatientRecordName() != null) {
            for (PatientRecordNameDTO PatientRecordNameDTO : prDto.getPatientRecordName()) {
                PatientRecordName patient = new PatientRecordName();
                patient.setFamilyName(PatientRecordNameDTO.getFamilyName());
                ArrayList<String> givens = new ArrayList<String>();
                for (GivenNameDTO given : PatientRecordNameDTO.getGivenName()) {
                    givens.add(given.getGivenName());
                }
                patient.setGivens(givens);
                if (PatientRecordNameDTO.getSuffix() != null)
                    patient.setSuffix(PatientRecordNameDTO.getSuffix());
                if (PatientRecordNameDTO.getPrefix() != null)
                    patient.setPrefix(PatientRecordNameDTO.getPrefix());
                if (PatientRecordNameDTO.getNameType() != null) {
                    NameType nameType = new NameType();
                    nameType.setCode(PatientRecordNameDTO.getNameType().getCode());
                    nameType.setDescription(PatientRecordNameDTO.getNameType().getDescription());
                    nameType.setId(PatientRecordNameDTO.getNameType().getId());
                    patient.setNameType(nameType);
                }
                if (PatientRecordNameDTO.getNameRepresentation() != null) {
                    NameRepresentation nameRep = new NameRepresentation();
                    nameRep.setCode(PatientRecordNameDTO.getNameType().getCode());
                    nameRep.setDescription(PatientRecordNameDTO.getNameType().getDescription());
                    nameRep.setId(PatientRecordNameDTO.getNameType().getId());
                    patient.setNameRepresentation(nameRep);
                }
                if (PatientRecordNameDTO.getNameAssembly() != null) {
                    NameAssembly nameAssembly = new NameAssembly();
                    nameAssembly.setCode(PatientRecordNameDTO.getNameType().getCode());
                    nameAssembly.setDescription(PatientRecordNameDTO.getNameType().getDescription());
                    nameAssembly.setId(PatientRecordNameDTO.getNameType().getId());
                    patient.setNameAssembly(nameAssembly);
                }
                if (PatientRecordNameDTO.getEffectiveDate() != null)
                    patient.setEffectiveDate(PatientRecordNameDTO.getEffectiveDate());
                if (PatientRecordNameDTO.getExpirationDate() != null)
                    patient.setExpirationDate(PatientRecordNameDTO.getExpirationDate());
                pr.getPatientRecordName().add(patient);
            }
        }
        PatientGender pg = new PatientGender();
        pg.setCode(prDto.getPatientGender().getCode());
        pg.setDescription(prDto.getPatientGender().getDescription());
        pr.setGender(pg);
        pr.setPhoneNumber(prDto.getPhoneNumber());
        pr.setDateOfBirth(prDto.getDateOfBirth());
        List<Address> praDomains = new ArrayList<Address>();
        for (PatientRecordAddressDTO praDto : prDto.getAddress()) {
            Address pra = new Address();
            for (PatientRecordAddressLineDTO pralDto : praDto.getPatientRecordAddressLines()) {
                pra.getLines().add(pralDto.getLine());
            }
            pra.setCity(praDto.getCity());
            pra.setState(praDto.getState());
            pra.setZipcode(praDto.getZipcode());
            praDomains.add(pra);
        }
        pr.setAddress(praDomains);
        return pr;
    }

    public static Location convert(LocationDTO dto) {
        Location result = new Location();
        result.setId(dto.getId());
        result.setName(dto.getName());

        Address addr = null;
        if (dto.hasAddressParts()) {
            addr = new Address();
            for (AddressLineDTO line : dto.getLines()) {
                addr.getLines().add(line.getLine());
            }
            addr.setCity(dto.getCity());
            addr.setState(dto.getState());
            addr.setZipcode(dto.getZipcode());
        }
        result.setAddress(addr);
        if (dto.getStatus() != null) {
            LocationStatus status = new LocationStatus();
            status.setId(dto.getStatus().getId());
            status.setName(dto.getStatus().getName());
            result.setStatus(status);
        }
        result.setDescription(dto.getDescription());
        result.setExternalId(dto.getExternalId());
        result.setParentOrgName(dto.getParentOrgName());
        result.setType(dto.getType());
        result.setExternalLastUpdateDate(dto.getExternalLastUpdateDate());
        result.setLastModifiedDate(dto.getLastModifiedDate());
        result.setCreationDate(dto.getCreationDate());
        return result;
    }

    public static Endpoint convert(EndpointDTO dto) {
        Endpoint result = new Endpoint();
        result.setId(dto.getId());
        result.setAdapter(dto.getAdapter());
        result.setOrganizationId(dto.getOrganizationId());
        result.setManagingOrganization(dto.getManagingOrganization());
        if (dto.getEndpointStatus() != null) {
            EndpointStatus status = new EndpointStatus();
            status.setId(dto.getEndpointStatus().getId());
            status.setName(dto.getEndpointStatus().getName());
            result.setEndpointStatus(status);
        }
        if (dto.getEndpointType() != null) {
            EndpointType type = new EndpointType();
            type.setId(dto.getEndpointType().getId());
            type.setName(dto.getEndpointType().getName());
            type.setCode(dto.getEndpointType().getCode());
            result.setEndpointType(type);
        }
        result.setExternalId(dto.getExternalId());
        if (dto.getMimeTypes() != null && dto.getMimeTypes().size() > 0) {
            for (EndpointMimeTypeDTO dtoMimeType : dto.getMimeTypes()) {
                result.getMimeTypes().add(convert(dtoMimeType));
            }
        }
        if (dto.getLocations() != null && dto.getLocations().size() > 0) {
            for (LocationDTO dtoLocaiton : dto.getLocations()) {
                result.getLocations().add(convert(dtoLocaiton));
            }
        }
        result.setPayloadType(dto.getPayloadType());
        result.setPublicKey(dto.getPublicKey());
        result.setUrl(dto.getUrl());
        result.setLastModifiedDate(dto.getLastModifiedDate());
        result.setExternalLastUpdateDate(dto.getExternalLastUpdateDate());
        result.setCreationDate(dto.getCreationDate());

        return result;
    }

    public static EndpointMimeType convert(EndpointMimeTypeDTO dtoObj) {
        EndpointMimeType result = new EndpointMimeType();
        result.setId(dtoObj.getId());
        result.setMimeType(dtoObj.getMimeType());
        return result;
    }

    public static Document convert(DocumentDTO dtoObj) {
        Document result = new Document();
        result.setId(dtoObj.getId() + "");
        result.setStatus(dtoObj.getStatus());
        result.setName(dtoObj.getName());
        result.setFormat(dtoObj.getFormat());
        result.setCached(!StringUtils.isEmpty(dtoObj.getContents()));
        result.setEndpointMapId(dtoObj.getPatientEndpointMapId());

        result.setClassName(dtoObj.getClassName());
        result.setConfidentiality(dtoObj.getConfidentiality());
        result.setCreationTime(dtoObj.getCreationTime());
        result.setDescription(dtoObj.getDescription());
        result.setSize(dtoObj.getSize());

        DocumentIdentifier docId = new DocumentIdentifier();
        docId.setDocumentUniqueId(dtoObj.getDocumentUniqueId());
        docId.setHomeCommunityId(dtoObj.getHomeCommunityId());
        docId.setRepositoryUniqueId(dtoObj.getRepositoryUniqueId());
        result.setIdentifier(docId);

        return result;
    }

    public static DocumentAudit convertToAuditDoc(DocumentDTO dtoObj) {
        DocumentAudit result = new DocumentAudit();
        result.setName(dtoObj.getName());
        result.setFormat(dtoObj.getFormat());

        result.setClassName(dtoObj.getClassName());
        result.setConfidentiality(dtoObj.getConfidentiality());
        result.setCreationTime(dtoObj.getCreationTime());
        result.setDescription(dtoObj.getDescription());
        result.setSize(dtoObj.getSize());

        DocumentIdentifier docId = new DocumentIdentifier();
        docId.setDocumentUniqueId(dtoObj.getDocumentUniqueId());
        docId.setHomeCommunityId(dtoObj.getHomeCommunityId());
        docId.setRepositoryUniqueId(dtoObj.getRepositoryUniqueId());
        result.setDocumentIdentifier(docId);
        return result;
    }
}
