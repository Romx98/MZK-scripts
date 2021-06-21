package cz.mzk.fedora.solrcorrection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class RemoveVcSolr {

    public static void main(String[] args) throws SolrServerException, IOException {
        String sorlHost = "";
        String uuid = "";

        SolrVcRecords solrVcRecords = new SolrVcRecords(sorlHost);
        solrVcRecords.printSolrResponse(uuid);

    }
}
