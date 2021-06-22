package cz.mzk.fedora.solrcorrection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class SolrVcRecords {

    private final SolrClient solrClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public SolrVcRecords(String solrHost) {
        solrClient = buildSolrClient(solrHost);
    }

    public void printSolrResponse(String uuid) throws SolrServerException, IOException {

        QueryResponse queryResponse = querySolrClient(uuid);
        SolrDocumentList docs = queryResponse.getResults();
        System.out.println(docs);
        System.out.println(docs.get(1));
        System.out.println(docs.get(2));
        System.out.println(docs.get(3));

    }

    public void removeVc(String idVc, String uuid) {
        SolrQuery query = createSolrQuery(uuid);
        // sem nejaka metoda, ktora spracuje query a tak...
    }

    public QueryResponse querySolrClient(String uuid) throws SolrServerException, IOException {
        return solrClient.query(createSolrQuery(uuid));
    }

    private SolrQuery createSolrQuery(String uuid) {
        String allPartsQueryStr = "pid_path:/" + uuid.trim() + ".*/";
        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.addField("PID");
        solrQuery.addField("collection");
        return solrQuery;
    }

    private void modifyRecordInSolr(String uuid, Object collection, String modifier)
            throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField("PID", uuid);
        inputDoc.addField("collection", Collections.singletonMap(modifier, collection));

        inputDoc.addField("modified_date", Collections.singletonMap("set", dateFormat.format(new Date())));
        solrClient.add(inputDoc);
    }

    private SolrClient buildSolrClient(String solrHost) {
        return new HttpSolrClient.Builder(solrHost)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    // zatim nic... ale bude potreba
    public void commitChange() throws SolrServerException, IOException {
        solrClient.commit();
    }
}
