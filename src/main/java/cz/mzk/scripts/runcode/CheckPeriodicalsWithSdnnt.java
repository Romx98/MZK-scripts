package cz.mzk.scripts.runcode;

import cz.mzk.scripts.clientapi.SolrClientUtils;
import cz.mzk.scripts.services.SolrCheckingDnnt;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class CheckPeriodicalsWithSdnnt {

    public static void main(String[] args) throws SolrServerException, IOException {
        //String sdnntHost = System.getenv("SDNNT_HOST");
        //String proxyHost = System.getenv("PROXY_HOST");
        //String proxyPort = System.getenv("PROXY_PORT");
        String solrHost = System.getenv("SOLR_HOST");

        // SOLR
        SolrClient solrClient = SolrClientUtils.createSolrClient(solrHost);
        SolrCheckingDnnt checkingDnnt = new SolrCheckingDnnt(solrClient);
        checkingDnnt.printAllPeriodicalDnnt("1911", "2010");
    }
}
