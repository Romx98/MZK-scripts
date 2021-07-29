package cz.mzk.scripts.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class RemoveVcSolr {

    public static void main(String[] args) throws SolrServerException, IOException {
        String solrHost = System.getenv("SOLR_HOST");
        String vc = "";
        String[] uuids = {""};

        SolrClient solrClient = SolrClientUtils.createSolrClient(solrHost);

        SolrVcService solrVcService = new SolrVcService(solrClient);
        for (String uuid : uuids) {
            solrVcService.removeVc(vc, uuid);
            SolrClientUtils.commitAndClose(solrClient);
        }
        solrClient.close();
    }
}
