package cz.mzk.scripts.runcode;

import cz.mzk.scripts.client_api.SdnntClientApi;

import java.io.IOException;

public class CheckPeriodicalsWithSdnnt {

    public static void main(String[] args) throws IOException {
        String sdnntHost = System.getenv("SDNNT_HOST");
        String solrHost = System.getenv("SOLR_HOST");

        //String param = "uuid=2405e7d0-6b97-11e7-aab4-005056827e52";
        String param = "cnb=cnb000356176";
        // SOLR
        //SolrClient solrClient = SolrClientUtils.createSolrClient(solrHost);
        //SolrCheckingDnnt checkingDnnt = new SolrCheckingDnnt(solrClient);

        // SDNNT
        SdnntClientApi sdnntApi = new SdnntClientApi(sdnntHost);
        sdnntApi.printAllResource(param);

    }
}
