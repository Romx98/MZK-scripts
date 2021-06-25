package cz.mzk.fedora.solrcorrection;

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

public class SolrVcRecords {

    private final SolrClient solrClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public SolrVcRecords(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void removeVc(String idVc, String uuid) throws SolrServerException, IOException {
        SolrQuery query = createSolrQuery(uuid);
        SolrClientApi.singleRequestAndApply(query, solrClient, createUnlinkConsumer(idVc));
    }

    @SuppressWarnings("unchecked")
    private Consumer<SolrDocument> createUnlinkConsumer(String idVc) {
        return solrDoc -> {
            List<String> collections = (List<String>) solrDoc.getFieldValue("collection");

            if ((collections != null) && (collections.contains(idVc))) {
                collections.remove(idVc);
                try {
                    String uuid = (String) solrDoc.getFieldValue("PID");
                    setVcFor(uuid, collections);
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private SolrQuery createSolrQuery(String uuid) {
        String allPartsQueryStr = "pid_path:/" + uuid.trim() + ".*/";
        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.addField("PID");
        solrQuery.addField("collection");
        return solrQuery;
    }

    public void setVcFor(String uuid, List<String> idVc) throws SolrServerException, IOException {
        modifyRecordInSolr(uuid, idVc, "set");
    }

    private void modifyRecordInSolr(String uuid, Object collection, String modifier)
            throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField("PID", uuid);
        inputDoc.addField("collection", Collections.singletonMap(modifier, collection));

        inputDoc.addField("modified_date", Collections.singletonMap("set", dateFormat.format(new Date())));
        solrClient.add(inputDoc);
    }
}
