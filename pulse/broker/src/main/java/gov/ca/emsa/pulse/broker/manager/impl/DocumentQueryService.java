package gov.ca.emsa.pulse.broker.manager.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.ca.emsa.pulse.auth.user.CommonUser;
import gov.ca.emsa.pulse.broker.adapter.Adapter;
import gov.ca.emsa.pulse.broker.adapter.AdapterFactory;
import gov.ca.emsa.pulse.broker.dto.DocumentDTO;
import gov.ca.emsa.pulse.broker.dto.DocumentQueryResults;
import gov.ca.emsa.pulse.broker.dto.EndpointDTO;
import gov.ca.emsa.pulse.broker.dto.PatientDTO;
import gov.ca.emsa.pulse.broker.dto.PatientEndpointMapDTO;
import gov.ca.emsa.pulse.broker.manager.DocumentManager;
import gov.ca.emsa.pulse.broker.manager.PatientManager;
import gov.ca.emsa.pulse.broker.manager.QueryManager;
import gov.ca.emsa.pulse.common.domain.QueryEndpointStatus;
import gov.ca.emsa.pulse.cten.IheStatus;

/**
 * Manages Document Queries.
 * @author alarned
 *
 */
//@Component("DocumentQueryManager")
public class DocumentQueryService implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(DocumentQueryService.class);

    private PatientEndpointMapDTO patientEndpointMap;
    private EndpointDTO endpoint;
    @Autowired private QueryManager queryManager;
    @Autowired private PatientManager patientManager;
    @Autowired private DocumentManager docManager;
    @Autowired private AdapterFactory adapterFactory;
    private PatientDTO toSearch;
    private String assertion;
    private CommonUser user;

    @Override
    public void run() {
        //query this organization directly for
        boolean querySuccess = true;
        DocumentQueryResults searchResults = null;
        if (endpoint == null) {
            LOGGER.error("There is no active document discovery endpoint to query for documents.");
            querySuccess = false;
        } else {
            Adapter adapter = adapterFactory.getAdapter(endpoint);
            if (adapter != null) {
                LOGGER.info("Starting query to endpoint with external id '" + endpoint.getExternalId() + "'");
                try {
                    searchResults = adapter.queryDocuments(user, endpoint, patientEndpointMap, assertion);
                } catch (Exception ex) {
                    LOGGER.error("Exception thrown in adapter " + adapter.getClass(), ex);
                    querySuccess = false;
                }
            }
            synchronized (patientManager) {
                patientEndpointMap = patientManager.getPatientEndpointMapById(patientEndpointMap.getId());
                if (patientEndpointMap.getDocumentsQueryStatus() != QueryEndpointStatus.Cancelled
                        && patientEndpointMap.getDocumentsQueryStatus() != QueryEndpointStatus.Closed) {
                    //store the returned document info
                    if (searchResults != null && searchResults.getStatus() == IheStatus.Success) {
                        List<DocumentDTO> docs = searchResults.getResults();
                        if (docs != null && docs.size() > 0) {
                            for (DocumentDTO doc : docs) {
                                doc.setPatientEndpointMapId(patientEndpointMap.getId());
                                //save document
                                docManager.create(doc);
                            }
                        } else {
                            LOGGER.info("Got 0 document results from query to endpoint with external id '"
                        + endpoint.getExternalId() + "'");
                        }
                    } else if (searchResults != null && searchResults.getStatus() == IheStatus.Failure) {
                        querySuccess = false;
                    } else {
                        LOGGER.error("Got a null response back from query to endpoint with external id '"
                    + endpoint.getExternalId() + "'");
                    }
                    LOGGER.info("Completed query to endpoint with external id '" + endpoint.getExternalId() + "'");
                }

                patientEndpointMap.setDocumentsQueryEnd(new Date());
                if (querySuccess) {
                    patientEndpointMap.setDocumentsQueryStatus(QueryEndpointStatus.Successful);
                } else {
                    patientEndpointMap.setDocumentsQueryStatus(QueryEndpointStatus.Failed);
                }

                //update mapping of our staged patient to the endpoint just queried
                try {
                    patientManager.updatePatientEndpointMap(patientEndpointMap);
                } catch (SQLException ex) {
                    LOGGER.error("Could not update patient endpoint map with "
                            + "[id: " + patientEndpointMap.getId() + ", "
                            + "externalPatientRecordId: " + patientEndpointMap.getExternalPatientRecordId() + ", "
                            + "endpointId: " + patientEndpointMap.getEndpointId() + ", "
                            + "patientId: " + patientEndpointMap.getPatientId() + "]");
                }
            }
        }
    }

    public CommonUser getUser() {
        return user;
    }

    public void setUser(final CommonUser user) {
        this.user = user;
    }

    public EndpointDTO getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final EndpointDTO endpoint) {
        this.endpoint = endpoint;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(final QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public PatientManager getPatientManager() {
        return patientManager;
    }

    public void setPatientManager(final PatientManager patientManager) {
        this.patientManager = patientManager;
    }

    public AdapterFactory getAdapterFactory() {
        return adapterFactory;
    }

    public void setAdapterFactory(final AdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }

    public PatientEndpointMapDTO getPatientEndpointMap() {
        return patientEndpointMap;
    }

    public void setPatientEndpointMap(final PatientEndpointMapDTO patientEndpointMap) {
        this.patientEndpointMap = patientEndpointMap;
    }

    public PatientDTO getToSearch() {
        return toSearch;
    }

    public void setToSearch(final PatientDTO toSearch) {
        this.toSearch = toSearch;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(final String assertion) {
        this.assertion = assertion;
    }
}
