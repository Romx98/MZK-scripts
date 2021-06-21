package cz.mzk.fedora.solrcorrection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class SolrVcRecords {

    private final SolrClient solrClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public SolrVcRecords(String solrHost) {
        solrClient = buildSolrClient(solrHost);
    }

    public void printSolrResponse(String uuid) throws SolrServerException, IOException {
        SolrDocumentList doc = querySolrClient(uuid).getResults();
    }

    public QueryResponse querySolrClient(String uuid) throws SolrServerException, IOException {
        return solrClient.query(createSolrQuery(uuid));
    }

    private SolrQuery createSolrQuery(String uuid) {
        String allPartsQueryStr = "pid_path:/" + uuid.trim() + ".*/";
        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.add("PID");
        solrQuery.add("collection");
        return solrQuery;
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
