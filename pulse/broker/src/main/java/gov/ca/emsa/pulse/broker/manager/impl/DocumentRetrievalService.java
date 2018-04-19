package gov.ca.emsa.pulse.broker.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.adapter.Adapter;
import gov.ca.emsa.pulse.broker.adapter.AdapterFactory;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;

@Component
public class DocumentRetrievalService implements Runnable {

    private static final Logger logger = LogManager.getLogger(DocumentRetrievalService.class);

    private PatientEndpointMapDTO patientEndpointMap;
    private EndpointDTO endpoint;
    private DocumentDTO document;
    // private List<DocumentDTO> documents;
    @Autowired
    private DocumentManager docManager;
    @Autowired
    private AdapterFactory adapterFactory;
    private String assertion;
    private CommonUser user;

    @Override
    public void run() {
        List<DocumentDTO> documents = new ArrayList<DocumentDTO>();

        boolean querySuccess = true;
        if (endpoint == null) {
            logger.error("There is no document retrieval endpoint to get document contents.");
            querySuccess = false;
        } else if (document == null) {
            logger.error("No document was specified.");
            querySuccess = false;
        } else {
            if (document.getStatus() != null && (document.getStatus() == QueryEndpointStatus.Cancelled
                    || document.getStatus() == QueryEndpointStatus.Failed)) {

                document.setStatus(QueryEndpointStatus.Closed);
                docManager.update(document);

                DocumentDTO newDocRequest = new DocumentDTO(document);
                newDocRequest.setStatus(QueryEndpointStatus.Active);
                document = docManager.create(newDocRequest);
                logger.info("Is resultDoc null?: " + (document == null));
                logger.info("Created document with ID " + document.getId());
            }
            documents.add(document);

            Adapter adapter = adapterFactory.getAdapter(endpoint);
            if (adapter != null) {
                logger.info("Starting query to endpoint with external id '" + endpoint.getExternalId()
                        + "' for document contents.");
                try {
                    synchronized (docManager) {
                        for (DocumentDTO document : documents) {
                            document.setStatus(QueryEndpointStatus.Active);
                            docManager.update(document);
                        }
                    }
                    adapter.retrieveDocumentsContents(user, endpoint, documents, patientEndpointMap, assertion);
                } catch (Exception ex) {
                    logger.error("Exception thrown in adapter " + adapter.getClass(), ex);
                    ex.printStackTrace();
                    querySuccess = false;
                }
            }
            logger.info("Completed query to endpoint with external id '" + endpoint.getEndpointStatus()
                    + "' for contents of " + documents.size() + " documents.");
        }

        synchronized (docManager) {
            if (querySuccess) {
                // store the returned document contents
                for (DocumentDTO doc : documents) {
                    doc.setStatus(QueryEndpointStatus.Successful);
                    docManager.update(doc);
                }
            } else {
                for (DocumentDTO doc : documents) {
                    if (!StringUtils.isEmpty(doc.getContents())) {
                        doc.setStatus(QueryEndpointStatus.Successful);
                        docManager.update(doc);
                    } else {
                        doc.setStatus(QueryEndpointStatus.Failed);
                        docManager.update(doc);
                    }
                }
            }
        }
    }

    public CommonUser getUser() {
        return user;
    }

    public void setUser(CommonUser user) {
        this.user = user;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }

    public EndpointDTO getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(EndpointDTO endpoint) {
        this.endpoint = endpoint;
    }

    public AdapterFactory getAdapterFactory() {
        return adapterFactory;
    }

    public void setAdapterFactory(AdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }

    // public List<DocumentDTO> getDocuments() {
    // return documents;
    // }
    //
    // public void setDocuments(List<DocumentDTO> documents) {
    // this.documents = documents;
    // }

    public PatientEndpointMapDTO getPatientEndpointMap() {
        return patientEndpointMap;
    }

    public void setPatientEndpointMap(PatientEndpointMapDTO patientEndpointMap) {
        this.patientEndpointMap = patientEndpointMap;
    }

    public DocumentDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentDTO document) {
        this.document = document;
    }
}
