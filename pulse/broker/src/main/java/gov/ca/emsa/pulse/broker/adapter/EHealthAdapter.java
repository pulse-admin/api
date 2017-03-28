package gov.ca.emsa.pulse.broker.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.opensaml.common.SAMLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.adapter.service.EHealthQueryProducerService;
import gov.ca.emsa.pulse.broker.dao.NameTypeDAO;
import gov.ca.emsa.pulse.broker.dto.AuditDocumentDTO;
import gov.ca.emsa.pulse.broker.dto.AuditEventDTO;
import gov.ca.emsa.pulse.broker.dto.AuditHumanRequestorDTO;
import gov.ca.emsa.pulse.broker.dto.AuditPatientDTO;
import gov.ca.emsa.pulse.broker.dto.AuditQueryParametersDTO;
import gov.ca.emsa.pulse.broker.dto.AuditRequestDestinationDTO;
import gov.ca.emsa.pulse.broker.dto.AuditRequestSourceDTO;
import gov.ca.emsa.pulse.broker.dto.AuditSourceDTO;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.DocumentQueryResults;
import gov.ca.emsa.pulse.broker.dto.DomainToDtoConverter;
import gov.ca.emsa.pulse.broker.dto.DtoToDomainConverter;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.NameTypeDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordDTO;
import gov.ca.emsa.pulse.broker.dto.PatientRecordResults;
import gov.ca.emsa.pulse.broker.manager.AuditEventManager;
import gov.ca.emsa.pulse.broker.saml.SAMLInput;
import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.PatientRecord;
import gov.ca.emsa.pulse.common.domain.PatientSearch;
import gov.ca.emsa.pulse.common.soap.JSONToSOAPService;
import gov.ca.emsa.pulse.common.soap.SOAPToJSONService;
import gov.ca.emsa.pulse.cten.IheStatus;
import gov.ca.emsa.pulse.service.AuditUtil;
import gov.ca.emsa.pulse.service.UserUtil;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;

@Component
public class EHealthAdapter implements Adapter {
	public static final String HOME_COMMUNITY_ID = "urn:oid:1.2.3.928.955";
	private static final Logger logger = LogManager.getLogger(EHealthAdapter.class);
	
	@Value("${defaultRequestTimeoutSeconds}")
	private Long defaultRequestTimeoutSeconds;
	
	@Value("${defaultConnectTimeoutSeconds}")
	private Long defaultConnectTimeoutSeconds;
	
	@Autowired JSONToSOAPService jsonConverterService;
	@Autowired SOAPToJSONService soapConverterService;
	@Autowired EHealthQueryProducerService queryProducer;
	@Autowired NameTypeDAO nameTypeDao;
	@Autowired AuditEventManager auditManager;
	private RestTemplate restTemplate;
	
	@PostConstruct
	public void initRestTemplate() {
		restTemplate = new RestTemplate();
		SimpleClientHttpRequestFactory rf =
			    (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
		rf.setConnectTimeout(defaultConnectTimeoutSeconds.intValue() * 1000);
		rf.setReadTimeout(defaultRequestTimeoutSeconds.intValue() * 1000);
	}
	
	@Override
	public PatientRecordResults queryPatients(CommonUser user, EndpointDTO endpoint, PatientSearch toSearch, SAMLInput samlInput) throws Exception {
		PRPAIN201305UV02 requestBody = jsonConverterService.convertFromPatientSearch(toSearch);
		String requestBodyXml = null;
		try {
			requestBodyXml = queryProducer.marshallPatientDiscoveryRequest(samlInput, requestBody);
		} catch(JAXBException ex) {
			logger.error(ex.getMessage(), ex);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);   
		HttpEntity<String> request = new HttpEntity<String>(requestBodyXml, headers);
		String searchResults = null;
		try {
			logger.info("Querying " + endpoint.getUrl() + " with timeout " + defaultRequestTimeoutSeconds + " seconds");
			searchResults = restTemplate.postForObject(endpoint.getUrl(), request, String.class);
		} catch(Exception ex) {
			auditManager.createAuditEventIG("FAILURE" , user, endpoint.getUrl(), queryProducer.marshallQueryByParameter(jsonConverterService.getQueryByParameter(requestBody).getValue()), HOME_COMMUNITY_ID);
			throw ex;
		}
		
		PatientRecordResults results = new PatientRecordResults();
		results.setStatus(IheStatus.Success);
		if(!StringUtils.isEmpty(searchResults)) {
			try {
				PRPAIN201306UV02 resultObj = queryProducer.unMarshallPatientDiscoveryResponseObject(searchResults);
				auditManager.createAuditEventIG("SUCCESS", user, endpoint.getUrl(), queryProducer.marshallQueryByParameter(jsonConverterService.getQueryByParameter(requestBody).getValue()), HOME_COMMUNITY_ID);

				List<PatientRecord> patientResults = soapConverterService.convertToPatientRecords(resultObj);
				for(int i = 0; i < patientResults.size(); i++) {
					PatientRecordDTO record = DomainToDtoConverter.convertToPatientRecord(patientResults.get(i));
					for(int j = 0; j < record.getPatientRecordName().size(); j++){
						NameTypeDTO nameType = nameTypeDao.getByCode("L");
						record.getPatientRecordName().get(j).setNameType(nameType);
					}
					results.getResults().add(record);
				}
			} catch(Exception ex) {
				logger.error("Exception unmarshalling patient discovery response", ex);
				results.setStatus(IheStatus.Failure);
			}
			
			if(results.getStatus() != IheStatus.Success) {
				logger.info("Trying to unmarshal response as an AdHocQueryRequest object to look for errors.");
				try {
					AdhocQueryResponse resultObj = queryProducer.unmarshallErrorQueryResponse(searchResults);
					results.setStatus(soapConverterService.getErrorStatus(resultObj));
					logger.info("Got error back from " + endpoint.getUrl() + ". Status: " + results.getStatus().name());
					auditManager.createAuditEventIG("FAILURE", user, endpoint.getUrl(), queryProducer.marshallQueryByParameter(jsonConverterService.getQueryByParameter(requestBody).getValue()), HOME_COMMUNITY_ID);
				} catch(Exception ex) {
					auditManager.createAuditEventIG("FAILURE", user, endpoint.getUrl(), queryProducer.marshallQueryByParameter(jsonConverterService.getQueryByParameter(requestBody).getValue()), HOME_COMMUNITY_ID);
					logger.error("Exception unmarshalling patient discovery response as error", ex);
				}
			}
		}

		return results;
	}

	@Override
	public DocumentQueryResults queryDocuments(CommonUser user, EndpointDTO endpoint, PatientEndpointMapDTO toSearch, SAMLInput samlInput) throws UnknownHostException, UnsupportedEncodingException {
		Patient patientToSearch = new Patient();
		toSearch.setExternalPatientRecordId(toSearch.getExternalPatientRecordId());
		AdhocQueryRequest requestBody = jsonConverterService.convertToDocumentRequest(patientToSearch);
		String requestBodyXml = null;
		try {
			requestBodyXml = queryProducer.marshallDocumentQueryRequest(samlInput, requestBody);
		} catch(JAXBException ex) {
			logger.error(ex.getMessage(), ex);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);   
		HttpEntity<String> request = new HttpEntity<String>(requestBodyXml, headers);
		
		String patientId = toSearch.getExternalPatientRecordId();
		
		String searchResults = null;
		try {
			logger.info("Querying " + endpoint.getUrl() + " with request " + request + " and timeout " + defaultRequestTimeoutSeconds + " seconds");
			searchResults = restTemplate.postForObject(endpoint.getUrl(), request, String.class);
		} catch(Exception ex) {
			logger.error("Exception when querying " + endpoint.getUrl() + ": " + ex.getMessage(), ex);
			auditManager.createAuditEventDCXGatewayQuery("FAILURE", user, endpoint.getUrl(),"", "", patientId);
			throw ex;
		}
		
		DocumentQueryResults results = new DocumentQueryResults();
		results.setStatus(IheStatus.Success);
		if(!StringUtils.isEmpty(searchResults)) {
			try {
				AdhocQueryResponse resultObj = queryProducer.unMarshallDocumentQueryResponseObject(searchResults);
				List<Document> documentResults = soapConverterService.convertToDocumentQueryResponse(resultObj);
				for(int i = 0; i < documentResults.size(); i++) {
					DocumentDTO record = DomainToDtoConverter.convert(documentResults.get(i));
					auditManager.createAuditEventDCXGatewayQuery("SUCCESS", user, endpoint.getUrl(),record.getRepositoryUniqueId(), record.getDocumentUniqueId(), patientId);
					results.getResults().add(record);
				}
			} catch(Exception ex) {
				logger.error("Exception unmarshalling document discovery response", ex);
				results.setStatus(IheStatus.Failure);
			}
			
			if(results.getStatus() != IheStatus.Success) {
				logger.error("Trying to unmarshal response as an AdHocQueryRequest object to look for errors.");
				try {
					AdhocQueryResponse resultObj = queryProducer.unmarshallErrorQueryResponse(searchResults);
					results.setStatus(soapConverterService.getErrorStatus(resultObj));
					logger.error("Got error back from " + endpoint.getUrl() + ". Status: " + results.getStatus().name());
					auditManager.createAuditEventDCXGatewayQuery("FAILURE", user, endpoint.getUrl(),"", "", patientId);
				} catch(Exception ex) {
					logger.error("Exception unmarshalling documents discovery response as error", ex);
				}
			}
		}

		return results;
	}

	/**
	 * get all of the document contents available from the given organization
	 * @param orgMap
	 * @param documents
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws UnknownHostException 
	 */
	@Override
	public void retrieveDocumentsContents(CommonUser user, EndpointDTO endpoint, List<DocumentDTO> documents, SAMLInput samlInput, PatientEndpointMapDTO patientMap) 
			throws UnknownHostException, UnsupportedEncodingException, IheErrorException {
		List<Document> docsToSearch = new ArrayList<Document>();
		for(DocumentDTO docDto : documents) {
			Document doc = DtoToDomainConverter.convert(docDto);
			docsToSearch.add(doc);
		}
		RetrieveDocumentSetRequestType requestBody = jsonConverterService.convertToRetrieveDocumentSetRequest(docsToSearch);
		String requestBodyXml = null;
		try {
			requestBodyXml = queryProducer.marshallDocumentSetRequest(samlInput, requestBody);
		} catch(JAXBException ex) {
			logger.error(ex.getMessage(), ex);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);   
		HttpEntity<String> request = new HttpEntity<String>(requestBodyXml, headers);

		String searchResults = null;
		try {
			logger.info("Querying " + endpoint.getUrl() + " with request " + request + " and timeout " + defaultRequestTimeoutSeconds + " seconds");
			searchResults = restTemplate.postForObject(endpoint.getUrl(), request, String.class);
		} catch(Exception ex) {
			logger.error("Exception when querying " + endpoint.getUrl() + ": " + ex.getMessage(), ex);
			for(Document doc : docsToSearch){
				auditManager.createAuditEventDCXGatewayRetrieve("FAILURE", user, endpoint.getUrl(), doc.getIdentifier().getRepositoryUniqueId(), doc.getIdentifier().getDocumentUniqueId(), doc.getIdentifier().getHomeCommunityId(), patientMap.getExternalPatientRecordId());
			}
			throw ex;
		}

		if(!StringUtils.isEmpty(searchResults)) {
			IheStatus resultStatus = IheStatus.Success;
			try {
				RetrieveDocumentSetResponseType resultObj = queryProducer.unMarshallDocumentSetRetrieveResponseObject(searchResults);
				List<DocumentResponse> documentResponses = soapConverterService.convertToDocumentSetResponse(resultObj);
				for(DocumentResponse docResponse : documentResponses) {
					//find the matching DocumentDTO that we sent in
					DocumentDTO matchingDto = null;
					for(int i = 0; i < documents.size() && matchingDto == null; i++) {
						DocumentDTO currDto = documents.get(i);
						auditManager.createAuditEventDCXGatewayRetrieve("SUCCESS", user, endpoint.getUrl(), docResponse.getRepositoryUniqueId(), docResponse.getDocumentUniqueId(), docResponse.getHomeCommunityId(), patientMap.getExternalPatientRecordId());
						if(currDto.getRepositoryUniqueId().equals(docResponse.getRepositoryUniqueId()) && 
								currDto.getHomeCommunityId().equals(docResponse.getHomeCommunityId()) &&
								currDto.getDocumentUniqueId().equals(docResponse.getDocumentUniqueId())) {
							matchingDto = currDto;
						}
					}

					if(matchingDto != null) {
						//read the binary document data from this DocumentResponse
						DataHandler dataHandler = docResponse.getDocument();
						InputStream in = null;
						try {
							in = dataHandler.getDataSource().getInputStream();
							StringWriter writer = new StringWriter();
							IOUtils.copy(in, writer, Charset.forName("UTF-8"));
							String dataStr = base64DecodeMessage(writer.toString());
							logger.debug("Converted binary to " + dataStr);
							matchingDto.setContents(dataStr.getBytes());
						} catch(IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if(in != null) { in.close(); }
							} catch(Exception ignore) {}
						}
					}
				}
			} catch(Exception ex) {
				logger.error("Exception unmarshalling document retrieve response", ex);
				resultStatus = IheStatus.Failure;
			}
			
			if(resultStatus != IheStatus.Success) {
				logger.error("Trying to unmarshal response as an AdHocQueryRequest object to look for errors.");
				try {
					AdhocQueryResponse resultObj = queryProducer.unmarshallErrorQueryResponse(searchResults);
					resultStatus = soapConverterService.getErrorStatus(resultObj);
					logger.error("Got error back from " + endpoint.getUrl() + ". Status: " + resultStatus.name());
					for(Document doc : docsToSearch){
						auditManager.createAuditEventDCXGatewayRetrieve("FAILURE", user, endpoint.getUrl(), doc.getIdentifier().getRepositoryUniqueId(), doc.getIdentifier().getDocumentUniqueId(), doc.getIdentifier().getHomeCommunityId(), patientMap.getExternalPatientRecordId());
					}
					throw new IheErrorException("Got error code: " + resultStatus.name());
				} catch(Exception ex) {
					logger.error("Exception unmarshalling document set response as error", ex);
					throw ex;
				}
			}
		}
	}

	private String base64DecodeMessage(String encodedMessage){       
		byte[] decoded = Base64.getDecoder().decode(encodedMessage);
		String decodedMessage = new String(decoded, Charset.forName("UTF-8"));
		return decodedMessage;
	}
}
