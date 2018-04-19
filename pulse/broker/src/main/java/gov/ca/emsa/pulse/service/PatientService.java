package gov.ca.emsa.pulse.service;

import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.domain.AuditType;
import gov.ca.emsa.pulse.broker.dto.AlternateCareFacilityDTO;

import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.dto.PulseUserDTO;
import gov.ca.emsa.pulse.broker.manager.AlternateCareFacilityManager;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.saml.SamlUtil;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "patients")
@RestController
@RequestMapping("/patients")
public class PatientService {
	private static final Logger logger = LogManager.getLogger(PatientService.class);
	@Autowired private PatientManager patientManager;
	@Autowired private DocumentManager docManager;
	@Autowired private AlternateCareFacilityManager acfManager;
	@Autowired private AuditEventManager auditManager;
	public PatientService() {
	}

	@ApiOperation(value="Get all patients at the logged-in user's ACF")
	@RequestMapping("")
	public List<Patient> getPatientsAtAcf() throws InvalidParameterException, UnknownHostException {
		CommonUser user = UserUtil.getCurrentUser();
		if(user.getAcf() == null || user.getAcf().getId() == null) {
			throw new InvalidParameterException("There was no ACF supplied in the User header, or the ACF had a null ID.");
		}
		List<PatientDTO> queryResults = patientManager.getPatientsAtAcf(user.getAcf().getId());
		List<Patient> results = new ArrayList<Patient>(queryResults.size());
        for(PatientDTO patientDto : queryResults) {
        	Patient patient = DtoToDomainConverter.convert(patientDto);
        	results.add(patient);
        }

        return results;
	}

	@ApiOperation(value="Get a list of documents associated with the given patient")
	@RequestMapping("/{patientId}/documents")
	public List<Document> getDocumentListForPatient(@PathVariable("patientId")Long patientId) {
		CommonUser user = UserUtil.getCurrentUser();
		List<DocumentDTO> docDtos = docManager.getDocumentsForPatient(patientId);
		List<Document> results = new ArrayList<Document>(docDtos.size());
		for(DocumentDTO docDto : docDtos) {
			results.add(DtoToDomainConverter.convert(docDto));
		}
		return results;
	}

	@ApiOperation(value = "Cancel a request to an endpoint for a list of documents")
	@RequestMapping(value = "/{patientId}/endpoints/{endpointId}/cancel", method = RequestMethod.POST)
	public Patient cancelDocumentListQuery(@PathVariable(value="patientId") Long patientId, 
			@PathVariable(value="endpointId") Long endpointId) 
			throws InvalidArgumentsException, SQLException {
		synchronized (patientManager) {
			List<PatientEndpointMapDTO> patientMapsForDocuments = patientManager.getPatientEndpointMaps(patientId, endpointId);
			if(patientMapsForDocuments == null || patientMapsForDocuments.size() == 0){
				throw new InvalidArgumentsException("No document query was found for patient " + patientId + " and endpoint " + endpointId);
			} else {
				//make sure there is at least one active/non-closed request
				boolean hasOpenRequest = false;
				for(PatientEndpointMapDTO dto : patientMapsForDocuments) {
					if(dto.getDocumentsQueryStatus() != null && 
						dto.getDocumentsQueryStatus() == QueryEndpointStatus.Active) {
						hasOpenRequest = true;
					}
				}
				if(!hasOpenRequest) {
					throw new InvalidArgumentsException("There are no Active requests between patient " + patientId + " and endpoint " + endpointId + " eligible for cancellation.");
				}
			}
			
			patientManager.cancelQueryForDocuments(patientId, endpointId);
			PatientDTO patientWithCancelledDocumentRequest = patientManager.getPatientById(patientId);
			return DtoToDomainConverter.convert(patientWithCancelledDocumentRequest);
		}
	}
	
	@ApiOperation(value = "Re-run a request to an endpoint for a list of documents")
	@RequestMapping(value = "/{patientId}/endpoints/{endpointId}/requery", method = RequestMethod.POST)
	public Patient redoDocumentListQuery(@PathVariable(value="patientId") Long patientId, 
			@PathVariable(value="endpointId") Long endpointId) 
			throws InvalidArgumentsException, SQLException {
		CommonUser user = UserUtil.getCurrentUser();

		synchronized (patientManager) {
			List<PatientEndpointMapDTO> patientMapsForDocuments = patientManager.getPatientEndpointMaps(patientId, endpointId);
			if(patientMapsForDocuments == null || patientMapsForDocuments.size() == 0){
				throw new InvalidArgumentsException("No document query was found for patient " + patientId + " and endpoint " + endpointId);
			} 
			
			patientManager.requeryForDocuments(patientId, endpointId, user);
			PatientDTO patientWithCancelledDocumentRequest = patientManager.getPatientById(patientId);
			return DtoToDomainConverter.convert(patientWithCancelledDocumentRequest);
		}
	}
	
	@ApiOperation(value="Retrieve a specific document from an endpoint.")
	@RequestMapping(value = "/{patientId}/documents/{documentId}")
	public @ResponseBody Document getDocumentContents(@PathVariable("patientId") Long patientId,
			@PathVariable("documentId") Long documentId,
			@RequestParam(value="cacheOnly", required= false, defaultValue="true") Boolean cacheOnly) 
		throws SQLException, JsonProcessingException {
		
		CommonUser user = UserUtil.getCurrentUser();
		String assertion = SamlUtil.signAndBuildStringAssertion(user);

		DocumentDTO result = null;
		if(cacheOnly == null || cacheOnly.booleanValue() == false) {
			//get the contents that are cached for this document
			result = docManager.getDocumentById(user, assertion, documentId);
			auditManager.createPulseAuditEvent(AuditType.DV, documentId);
		} else {
			//cache the document's contents
			result = docManager.getDocumentById(user, assertion, documentId);
		}
		Document cachedDoc = DtoToDomainConverter.convert(result);
		//this is the only place the contents should be returned to the caller, ever!
		cachedDoc.setContents(result.getContents());
		return cachedDoc;
	}
	
	@ApiOperation(value="Cancel the retrieval of a specific document from an endpoint.")
	@RequestMapping(value = "/{patientId}/documents/{documentId}/cancel", method=RequestMethod.POST)
	public void cancelDocumentContentQuery(@PathVariable("patientId") Long patientId,
			@PathVariable("documentId") Long documentId) 
		throws SQLException, JsonProcessingException {
		
		docManager.cancelDocumentContentQuery(documentId, patientId);
	}
	
	@ApiOperation(value = "Edit a patient's information")
	@RequestMapping(value = "/{patientId}/edit", method = RequestMethod.POST)
	public Patient update(@PathVariable("patientId") Long patientId, 
			@RequestBody(required=true) Patient toUpdate) throws SQLException {
		
		PatientDTO patientToUpdate = DomainToDtoConverter.convertToPatient(toUpdate);
		patientToUpdate.setId(patientId);
		PatientDTO updated = patientManager.update(patientToUpdate);
		return DtoToDomainConverter.convert(updated);
	}
	
	@ApiOperation(value = "Delete a patient")
	@RequestMapping(value="/{patientId}/delete", method = RequestMethod.POST)
	public void deletePatient(@PathVariable(value="patientId") Long patientId) 
	 throws SQLException, JsonProcessingException {
		auditManager.createPulseAuditEvent(AuditType.PD, patientId);
		patientManager.delete(patientId);
	}
}
