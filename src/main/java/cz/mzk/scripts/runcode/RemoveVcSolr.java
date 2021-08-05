package cz.mzk.scripts.runcode;

import cz.mzk.scripts.services.SolrClientUtils;
import cz.mzk.scripts.services.SolrVcService;
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
            SolrClientUtils.commit(solrClient);
        }
        solrClient.close();
    }
}
