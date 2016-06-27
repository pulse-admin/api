package gov.ca.emsa.pulse.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opensaml.xml.io.MarshallingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.ca.emsa.pulse.broker.domain.Document;
import gov.ca.emsa.pulse.broker.domain.User;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.saml.SAMLInput;
import gov.ca.emsa.pulse.broker.saml.SamlGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "documents")
@RestController
@RequestMapping("/documents")
public class DocumentService {
	@Autowired DocumentManager docManager;
	@Autowired PatientManager patientManager;
	@Autowired SamlGenerator samlGenerator;
	
		@ApiOperation(value="Query a specific organization about a specific person.")
		@RequestMapping(method = RequestMethod.POST,produces="application/json; charset=utf-8")
	    public @ResponseBody List<Document> searchDocuments(
	    		@RequestParam(value="patientId", required=true) String patientId,
	    		@RequestBody User user) throws Exception {
			
			SAMLInput input = new SAMLInput();
			input.setStrIssuer("https://idp.dhv.gov");
			input.setStrNameID("UserBrianLindsey");
			input.setStrNameQualifier("My Website");
			input.setSessionId("abcdedf1234567");
			
			
			HashMap<String, String> customAttributes = new HashMap<String,String>();
			customAttributes.put("RequesterName", user.getName());
			customAttributes.put("RequestReason", "Patient is bleeding.");
			customAttributes.put("PatientFirstName", "Hodor");
			customAttributes.put("PatientLastName", "Guy");
			customAttributes.put("PatientSSN", "123456789");
			
			input.setAttributes(customAttributes);
			
			String samlMessage = null;
			
			try {
				samlMessage = samlGenerator.createSAML(input);
			} catch (MarshallingException e) {
				e.printStackTrace();
			}
			PatientDTO toQuery = patientManager.getPatientById(new Long(patientId));
			List<Document> results = new ArrayList<Document>();
			if(toQuery != null) {
				List<DocumentDTO> docResults = docManager.queryDocumentsForPatient(samlMessage, toQuery);
				for(DocumentDTO docResult : docResults) {
					Document doc = DtoToDomainConverter.convert(docResult);
					results.add(doc);
				}
			}
			return results;
	    }
		
		@ApiOperation(value="Retrieve a specific document from an organization.")
		@RequestMapping(value = "/{documentId}", method=RequestMethod.POST, produces="application/json; charset=utf-8")
		public @ResponseBody String getDocumentContents(@PathVariable("documentId") Long documentId,
				@RequestParam(value="cacheOnly", required= false, defaultValue="true") Boolean cacheOnly,
				@RequestBody User user) {
			
			SAMLInput input = new SAMLInput();
			input.setStrIssuer("https://idp.dhv.gov");
			input.setStrNameID("UserBrianLindsey");
			input.setStrNameQualifier("My Website");
			input.setSessionId("abcdedf1234567");
			
			
			HashMap<String, String> customAttributes = new HashMap<String,String>();
			customAttributes.put("RequesterName", user.getName());
			customAttributes.put("RequestReason", "Patient is bleeding.");
			customAttributes.put("PatientFirstName", "Hodor");
			customAttributes.put("PatientLastName", "Guy");
			customAttributes.put("PatientSSN", "123456789");
			
			input.setAttributes(customAttributes);
			
			String samlMessage = null;
			
			try {
				samlMessage = samlGenerator.createSAML(input);
			} catch (MarshallingException e) {
				e.printStackTrace();
			}
			
			String result = "";
			if(cacheOnly == null || cacheOnly.booleanValue() == false) {
				result = docManager.getDocumentById(samlMessage, documentId);
			} else {
				docManager.getDocumentById(samlMessage, documentId);
			}
			return result;
		}
}