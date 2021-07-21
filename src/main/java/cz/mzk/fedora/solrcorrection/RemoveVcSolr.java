package cz.mzk.fedora.solrcorrection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class RemoveVcSolr {

    public static void main(String[] args) throws SolrServerException, IOException {
        String solrHost = System.getenv("SOLR_HOST");
        String vc = "";
        String uuid = "";

        SolrClient solrClient = SolrClientUtils.createSolrClient(solrHost);

        SolrVcService solrVcService = new SolrVcService(solrClient);
        solrVcService.removeVc(vc, uuid);
        SolrClientUtils.commitAndClose(solrClient);
    }
}
