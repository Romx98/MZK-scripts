package cz.mzk.fedora.solrcorrection;

import cz.mzk.fedora.model.SolrField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class SolrVcService {

    private final SolrClient solrClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public SolrVcService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void removeVc(String idVc, String uuid) throws SolrServerException, IOException {
        SolrQuery query = createQueryByPidPath(uuid);
        SolrClientUtils.singleRequestAndApply(query, solrClient, createUnlinkConsumer(idVc));
    }

    @SuppressWarnings("unchecked")
    private Consumer<SolrDocument> createUnlinkConsumer(String idVc) {
        return solrDoc -> {
            List<String> collections = (List<String>) solrDoc.getFieldValue(SolrField.COLLECTION);

            if ((collections != null) && (collections.contains(idVc))) {
                collections.remove(idVc);
                try {
                    String uuid = (String) solrDoc.getFieldValue(SolrField.UUID);
                    setVcFor(uuid, collections);
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private SolrQuery createQueryByPidPath(String uuid) {
        String allPartsQueryStr = "root_pid:/" + uuid.trim() + ".*/";
        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.COLLECTION);
        return solrQuery;
    }

    public void setVcFor(String uuid, List<String> idVc) throws SolrServerException, IOException {
        changeCollection(uuid, idVc, "set");
    }

    private void changeCollection(String uuid, Object collectionList, String modifier)
            throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField(SolrField.UUID, uuid);
        inputDoc.addField(SolrField.COLLECTION, Collections.singletonMap(modifier, collectionList));

        updateModifiedDateNow(inputDoc);
        solrClient.add(inputDoc);
    }

    private void updateModifiedDateNow(SolrInputDocument inputDoc) {
        inputDoc.addField(SolrField.MODIFIED_DATE, Collections.singletonMap("set", dateFormat.format(new Date())));
    }
}
