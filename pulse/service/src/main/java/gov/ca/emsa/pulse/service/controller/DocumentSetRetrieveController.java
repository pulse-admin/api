package gov.ca.emsa.pulse.service.controller;

import javax.xml.bind.JAXBException;
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
import gov.ca.emsa.pulse.service.EHealthQueryConsumerService;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;

@RestController
public class DocumentSetRetrieveController {
	private static final Logger logger = LogManager.getLogger(DocumentSetRetrieveController.class);

	@Autowired EHealthQueryConsumerService consumerService;
	@Autowired JSONToSOAPService jsonService;

	//https://www.ihe.net/uploadedFiles/Documents/ITI/IHE_ITI_TF_Vol3.pdf
	//section 4.2.4.1
	@RequestMapping(value = "/retrieveDocumentSet", method = RequestMethod.POST, produces = "application/soap+xml")
	public String documentRequest(@RequestBody String request) 
		throws JAXBException, SOAPException {
		SOAPMessage soapRequest = consumerService.getSoapMessageFromXml(request);
		RetrieveDocumentSetResponseType responseObj = jsonService.createNoDocumentSetRetrieveResponse();
		logger.info("Document set Response object: " + responseObj.toString());
		String response = null;
		try {
			response = consumerService.marshallDocumentSetResponse(responseObj, soapRequest);
		} catch (JAXBException | SOAPException e) {
			e.printStackTrace();
			throw e;
		}
		logger.info("Document set Response string: " + response);
		return response;
	}
}
