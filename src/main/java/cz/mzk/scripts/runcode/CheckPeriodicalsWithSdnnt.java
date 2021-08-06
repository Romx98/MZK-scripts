package cz.mzk.scripts.runcode;

import cz.mzk.scripts.client_api.SdnntClientApi;
import cz.mzk.scripts.model.SolrField;
import cz.mzk.scripts.services.SolrCheckingDnnt;
import cz.mzk.scripts.services.SolrClientUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;

public class CheckPeriodicalsWithSdnnt {

    public static void main(String[] args) throws IOException, SolrServerException {
        String sdnntHost = System.getenv("SDNNT_HOST");
        String solrHost = System.getenv("SOLR_HOST");

        //String param = "uuid=2405e7d0-6b97-11e7-aab4-005056827e52";
        //String param = "cnb=cnb000356176";
        // SOLR
        SolrClient solrClient = SolrClientUtils.createSolrClient(solrHost);
        SolrCheckingDnnt checkingDnnt = new SolrCheckingDnnt(solrClient);
        checkingDnnt.printAllPeriodicalDnnt("1911", "2010");

        // SDNNT
        //SdnntClientApi sdnntApi = new SdnntClientApi(sdnntHost);

    }
}
