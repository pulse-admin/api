package gov.ca.emsa.pulse.common.soap;

import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.DocumentWrapper;
import gov.ca.emsa.pulse.common.domain.Patient;
import gov.ca.emsa.pulse.common.domain.PatientRecord;
import gov.ca.emsa.pulse.common.domain.PatientSearch;
import gov.ca.emsa.pulse.common.domain.PatientSearchAddress;
import gov.ca.emsa.pulse.common.domain.PatientSearchName;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.DocumentRequest;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.RegistryObjectListType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotListType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ValueListType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.hl7.v3.ADExplicit;
import org.hl7.v3.AdxpExplicitState;
import org.hl7.v3.CE;
import org.hl7.v3.ENExplicit;
import org.hl7.v3.EnExplicitFamily;
import org.hl7.v3.EnExplicitGiven;
import org.hl7.v3.II;
import org.hl7.v3.IVLTSExplicit;
import org.hl7.v3.PNExplicit;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201305UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01ControlActProcess;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAIN201310UV02MFMIMT700711UV01Subject2;
import org.hl7.v3.PRPAMT201304UV02Patient;
import org.hl7.v3.PRPAMT201304UV02Person;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectAdministrativeGender;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectBirthTime;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectId;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectName;
import org.hl7.v3.PRPAMT201306UV02ParameterList;
import org.hl7.v3.PRPAMT201306UV02PatientAddress;
import org.hl7.v3.PRPAMT201306UV02PatientTelecom;
import org.hl7.v3.PRPAMT201306UV02QueryByParameter;
import org.hl7.v3.PRPAMT201310UV02OtherIDs;
import org.hl7.v3.TELExplicit;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JSONToSOAPServiceImpl implements JSONToSOAPService{
	public PRPAIN201310UV02 convertPatientRecordListToSOAPResponse(List<PatientRecord> patientRecords){
		PRPAIN201310UV02 returnSOAP = new PRPAIN201310UV02();
		List<PRPAIN201310UV02MFMIMT700711UV01Subject1> subjects = new ArrayList<PRPAIN201310UV02MFMIMT700711UV01Subject1>();
		PRPAIN201310UV02MFMIMT700711UV01ControlActProcess cap = new PRPAIN201310UV02MFMIMT700711UV01ControlActProcess();
		for(PatientRecord record : patientRecords){
			PRPAIN201310UV02MFMIMT700711UV01Subject1 subject = new PRPAIN201310UV02MFMIMT700711UV01Subject1();
			PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent registrationEvent = new PRPAIN201310UV02MFMIMT700711UV01RegistrationEvent();
			PRPAIN201310UV02MFMIMT700711UV01Subject2 subject1 = new PRPAIN201310UV02MFMIMT700711UV01Subject2();
			PRPAMT201304UV02Patient patient = new PRPAMT201304UV02Patient();
			JAXBElement<EnExplicitGiven> given = new JAXBElement(new QName("given"), String.class, record.getPatientRecordName().get(0).getGivenName());
			JAXBElement<EnExplicitFamily> family = new JAXBElement(new QName("family"), String.class, record.getPatientRecordName().get(0).getFamilyName());
			PNExplicit pnGiven = new PNExplicit();
			PNExplicit pnFamily = new PNExplicit();
			pnGiven.getContent().add(given);
			pnFamily.getContent().add(family);
			PRPAMT201304UV02Person patientPerson1 = new PRPAMT201304UV02Person();
			patientPerson1.getName().add(pnGiven);
			patientPerson1.getName().add(pnFamily);
			JAXBElement<PRPAMT201304UV02Person> patientPerson = new JAXBElement(new QName("patient"), PRPAMT201304UV02Person.class, patientPerson1);
			patient.setPatientPerson(patientPerson);
			subject1.setPatient(patient);
			registrationEvent.setSubject1(subject1);
			subject.setRegistrationEvent(registrationEvent);
			subjects.add(subject);
		}
		cap.getSubject().addAll(subjects);
		returnSOAP.setControlActProcess(cap);
		return returnSOAP;
	}
	
	public JAXBElement<PRPAMT201306UV02QueryByParameter> getQueryByParameter(PRPAIN201305UV02 message){
		return message.getControlActProcess().getQueryByParameter();
	}
	
	public PRPAIN201305UV02 convertFromPatientSearch(PatientSearch search) {
		PRPAIN201305UV02 request = new PRPAIN201305UV02();
		PRPAIN201305UV02QUQIMT021001UV01ControlActProcess controlActProcess = new PRPAIN201305UV02QUQIMT021001UV01ControlActProcess();
		PRPAMT201306UV02QueryByParameter queryByParameter1 = new PRPAMT201306UV02QueryByParameter();
		PRPAMT201306UV02ParameterList parameterList = new PRPAMT201306UV02ParameterList();
		
		for(PatientSearchName patientName : search.getPatientNames()){
			if(!patientName.getGivenName().isEmpty() || !StringUtils.isEmpty(patientName.getFamilyName())) {
				PRPAMT201306UV02LivingSubjectName name = new PRPAMT201306UV02LivingSubjectName();
				ENExplicit nameValue = new ENExplicit();
				nameValue.getContent().add(new JAXBElement<String>(new QName("family"), String.class, patientName.getFamilyName()));
				for(String given : patientName.getGivenName()){
					nameValue.getContent().add(new JAXBElement<String>(new QName("given"), String.class, given));
				}
				if(!StringUtils.isEmpty(patientName.getPrefix())){
					nameValue.getContent().add(new JAXBElement<String>(new QName("prefix"), String.class, patientName.getPrefix()));
				}
				if(!StringUtils.isEmpty(patientName.getSuffix())){
					nameValue.getContent().add(new JAXBElement<String>(new QName("suffix"), String.class, patientName.getSuffix()));
				}
				name.getValue().add(nameValue);
				parameterList.getLivingSubjectName().add(name);
			}
		}
		PRPAMT201306UV02PatientAddress patientAddress = new PRPAMT201306UV02PatientAddress();
		if(search.getAddresses() != null){
			for(PatientSearchAddress patientSearchAddress : search.getAddresses()){
				ADExplicit addr = new ADExplicit();
				addr.getContent().add(new JAXBElement<String>(new QName("state"), String.class, patientSearchAddress.getState()));
				addr.getContent().add(new JAXBElement<String>(new QName("city"), String.class, patientSearchAddress.getCity()));
				addr.getContent().add(new JAXBElement<String>(new QName("postalCode"), String.class, patientSearchAddress.getZipcode()));

				for(String line : patientSearchAddress.getLines()){
					addr.getContent().add(new JAXBElement<String>(new QName("streetAddressLine"), String.class, line));
				}
				patientAddress.getValue().add(addr);
				parameterList.getPatientAddress().add(patientAddress);
			}
		}

		if(!StringUtils.isEmpty(search.getGender())) {
			PRPAMT201306UV02LivingSubjectAdministrativeGender gender = new PRPAMT201306UV02LivingSubjectAdministrativeGender();
			CE genderValue = new CE();
			genderValue.setCode(search.getGender());
			gender.getValue().add(genderValue);
			parameterList.getLivingSubjectAdministrativeGender().add(gender);
		}
		
		if(!StringUtils.isEmpty(search.getDob())) {
			PRPAMT201306UV02LivingSubjectBirthTime birthTime = new PRPAMT201306UV02LivingSubjectBirthTime();
			IVLTSExplicit birthTimeValue = new IVLTSExplicit();
			birthTimeValue.setValue(search.getDob());
			birthTime.getValue().add(birthTimeValue);
			parameterList.getLivingSubjectBirthTime().add(birthTime);
		}
		
		if(!StringUtils.isEmpty(search.getTelephone())) {
			PRPAMT201306UV02PatientTelecom telecom = new PRPAMT201306UV02PatientTelecom();
			TELExplicit telecomEx = new TELExplicit();
			telecomEx.setValue(search.getTelephone());
			telecom.getValue().add(telecomEx);
			parameterList.getPatientTelecom().add(telecom);
		}
		
		if(!StringUtils.isEmpty(search.getSsn())) {
			PRPAMT201306UV02LivingSubjectId ssn = new PRPAMT201306UV02LivingSubjectId();
			II ssnEx = new II();
			ssnEx.setRoot("2.16.840.1.113883.4.1");
			ssnEx.setExtension(search.getSsn());
			ssn.getValue().add(ssnEx);
			parameterList.getLivingSubjectId().add(ssn);
		}
		
		queryByParameter1.setParameterList(parameterList);
		
		JAXBElement<PRPAMT201306UV02QueryByParameter> queryByParameter = 
				new JAXBElement<PRPAMT201306UV02QueryByParameter>(new QName("queryByParameter"), PRPAMT201306UV02QueryByParameter.class, queryByParameter1);

		controlActProcess.setQueryByParameter(queryByParameter);
		request.setControlActProcess(controlActProcess);
		
		return request;
	}
	
	// will be adding metadata to the document object soon to fill in more response fields
	public AdhocQueryResponse convertDocumentListToSOAPResponse(List<Document> doc, String patientId){
		AdhocQueryResponse response = new AdhocQueryResponse();
		RegistryObjectListType rolt = new RegistryObjectListType();
		ExtrinsicObjectType eo = new ExtrinsicObjectType();
		List<SlotType1> slots = new ArrayList<SlotType1>();
		SlotType1 slot = new SlotType1();
		slot.setName("sourcePatientId");
		ValueListType valueList = new ValueListType();
		valueList.getValue().add(patientId);
		slot.setValueList(valueList);
		slots.add(slot);
		eo.getSlot().addAll(slots);
		rolt.getIdentifiable().add(new JAXBElement(new QName("ExtrinsicObject"), ExtrinsicObjectType.class, eo));
		response.setRegistryObjectList(rolt);
		return response;
	}

	public AdhocQueryRequest convertToDocumentRequest(Patient patient) {
		AdhocQueryRequest request = new AdhocQueryRequest();
		SlotListType slotList = new SlotListType();
		SlotType1 slot = new SlotType1();
		slot.setName("$XDSDocumentEntryPatientId");
		ValueListType valueList = new ValueListType();
		valueList.getValue().add(patient.getExternalPatientId());
		slot.setValueList(valueList);
		slotList.getSlot().add(slot);
		request.setRequestSlotList(slotList);
		return request;
	}
	
	public RetrieveDocumentSetResponseType convertDocumentSetToSOAPResponse(List<DocumentWrapper> docs) {
		RetrieveDocumentSetResponseType dsrt = new RetrieveDocumentSetResponseType();
		RegistryResponseType rr = new RegistryResponseType();
		List<DocumentResponse> documentResponses = new ArrayList<DocumentResponse>();
		for(DocumentWrapper doc : docs){
			DocumentResponse docResponse = new DocumentResponse();
			DataHandler dh = new DataHandler(doc, "text/xml");
			docResponse.setDocument(dh);
			docResponse.setMimeType(dh.getContentType());
			docResponse.setDocumentUniqueId(doc.getDocumentUniqueId());
			docResponse.setHomeCommunityId(doc.getHomeCommunityId());
			docResponse.setRepositoryUniqueId(doc.getRepositoryUniqueId());
			documentResponses.add(docResponse);
		}
		rr.setStatus("urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success");
		dsrt.setRegistryResponse(rr);
		dsrt.getDocumentResponse().addAll(documentResponses);
		return dsrt;
	}
	
	public RetrieveDocumentSetRequestType convertToRetrieveDocumentSetRequest(List<Document> documents) {
		RetrieveDocumentSetRequestType request = new RetrieveDocumentSetRequestType();
		for(Document doc : documents) {
			DocumentRequest docReq = new DocumentRequest();
			if(doc.getIdentifier() != null) {
				docReq.setDocumentUniqueId(doc.getIdentifier().getDocumentUniqueId());
				docReq.setHomeCommunityId(doc.getIdentifier().getHomeCommunityId());
				docReq.setRepositoryUniqueId(doc.getIdentifier().getRepositoryUniqueId());
				request.getDocumentRequest().add(docReq);
			}
		}
		return request;
	}

}
