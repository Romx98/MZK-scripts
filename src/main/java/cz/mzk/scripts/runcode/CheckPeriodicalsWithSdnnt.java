package cz.mzk.scripts.runcode;

import cz.mzk.scripts.client_api.SdnntClientApi;
import cz.mzk.scripts.model.SdnntField;
import cz.mzk.scripts.services.SolrCheckingDnnt;
import cz.mzk.scripts.services.SolrClientUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.Optional;

public class CheckPeriodicalsWithSdnnt {

    public static void main(String[] args) throws IOException, SolrServerException {
        String sdnntHost = System.getenv("SDNNT_HOST");
        String solrHost = System.getenv("SOLR_HOST");

        //String param = "uuid=2405e7d0-6b97-11e7-aab4-005056827e52";
        //String param = "cnb=cnb000356176";
        // SOLR
        SolrClient solrClient = SolrClientUtils.createSolrClient(solrHost);
        SdnntClientApi sdnntApi = new SdnntClientApi(sdnntHost);
        SolrCheckingDnnt checkingDnnt = new SolrCheckingDnnt(solrClient);

        SolrDocumentList solrDocList = checkingDnnt.getSolrDocListResponse("1911", "2010");




        for (SolrDocument doc : solrDocList) {
            String uuid = checkingDnnt.getUuidForUrlRequest(doc);

            if (sdnntApi.getFieldValueFromResourceStr(SdnntField.LICENCE, uuid).isEmpty()) {
                Optional<String> ccnb = checkingDnnt.getCcnbForUrlRequest(doc);

                if (ccnb.isEmpty() ||
                        sdnntApi.getFieldValueFromResourceStr(SdnntField.LICENCE, ccnb.get()).isEmpty()) {
                    System.out.println("PID: " + uuid);
                    break;
                }
                System.out.println(sdnntApi.getFieldValueFromResourceStr(SdnntField.LICENCE, ccnb.get()));
            }
            else {
                System.out.println("PID: " + sdnntApi.getFieldValueFromResourceStr(SdnntField.LICENCE, uuid).get());
            }
        }


    }
}
