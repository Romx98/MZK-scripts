package cz.mzk.fedora.solrcorrection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class RemoveVcSolr {

    public static void main(String[] args) throws SolrServerException, IOException {
        String solrHost = System.getenv("SH");
        String vc = "";
        String uuid = "";

        SolrClient solrClient = SolrClientApi.createSolrClient(solrHost);

        SolrVcRecords solrVcRecords = new SolrVcRecords(solrClient);
        solrVcRecords.removeVc(vc, uuid);
        SolrClientApi.commitAndClose(solrClient);

    }
}
