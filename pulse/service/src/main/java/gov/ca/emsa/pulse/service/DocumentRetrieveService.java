package gov.ca.emsa.pulse.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import gov.ca.emsa.pulse.common.domain.Document;
import gov.ca.emsa.pulse.common.domain.DocumentRetrieve;
import gov.ca.emsa.pulse.common.domain.DocumentWrapper;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.DocumentRequest;

@Service
public class DocumentRetrieveService extends EHealthQueryService {

    public List<DocumentWrapper> retrieveDocuments(RestTemplate restTemplate, DocumentRetrieve docRetrieve,
            String patientId) {
        this.setRestTemplate(restTemplate);
        this.setAuthorizationHeader();
        List<DocumentWrapper> docs = new ArrayList<DocumentWrapper>();
        for (DocumentRequest docRequest : docRetrieve.getDocRequests()) {
            String documentId = docRequest.getHomeCommunityId() + "-" + docRequest.getRepositoryUniqueId() + "-"
                    + docRequest.getDocumentUniqueId();
            HttpEntity<Document[]> entity = new HttpEntity<Document[]>(this.getHeaders());
            HttpEntity<DocumentWrapper> response = restTemplate.exchange(
                    "http://localhost:" + this.getPort() + "/patients/" + patientId + "/documents/" + documentId,
                    HttpMethod.GET, entity, DocumentWrapper.class);
            docs.add(response.getBody());
        }
        return docs;
    }

}
