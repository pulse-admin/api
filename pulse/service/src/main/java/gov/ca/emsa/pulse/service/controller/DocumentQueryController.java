package gov.ca.emsa.pulse.service.controller;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.ca.emsa.pulse.common.soap.JSONToSOAPService;
import gov.ca.emsa.pulse.common.soap.SOAPToJSONService;
import gov.ca.emsa.pulse.service.EHealthQueryConsumerService;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;

@RestController
public class DocumentQueryController {
	private static final Logger logger = LogManager.getLogger(DocumentQueryController.class);
	
	@Autowired EHealthQueryConsumerService consumerService;
	@Autowired SOAPToJSONService SOAPService;
	@Autowired JSONToSOAPService jsonService;

	//https://www.ihe.net/uploadedFiles/Documents/ITI/IHE_ITI_TF_Vol3.pdf
	// section 4.2.4.1
	@RequestMapping(value = "/documentQuery", method = RequestMethod.POST, produces = "application/soap+xml")
	public String queryRequest(@RequestBody String request) 
		throws JAXBException, SOAPException {
		SOAPMessage soapRequest = consumerService.getSoapMessageFromXml(request);
		AdhocQueryResponse responseObj = jsonService.createNoDocumentListResponse();
		String response = null;
		try {
			response = consumerService.marshallDocumentQueryResponse(responseObj, soapRequest);
		} catch (JAXBException |SOAPException e) {
			e.printStackTrace();
			throw e;
		} 
		logger.info("Document query Response string: " + response);
		return response;
	}
}
