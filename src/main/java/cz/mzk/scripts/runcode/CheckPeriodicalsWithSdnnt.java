package cz.mzk.scripts.runcode;

import cz.mzk.scripts.client_api.SdnntClientApi;
import cz.mzk.scripts.model.SdnntField;
import cz.mzk.scripts.model.SolrField;
import cz.mzk.scripts.services.SolrCheckingDnnt;
import cz.mzk.scripts.services.SolrClientUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.*;

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
        HashMap<String, String> mismatch = new HashMap<>();

        SolrDocumentList solrDocList = checkingDnnt.getSolrDocListResponse("1911", "2010");
        int count = 0;
        System.out.println("DOCUMENT: " + solrDocList.getNumFound());
        for (SolrDocument doc : solrDocList) {
            count++;
            String uuid = checkingDnnt.getUuidForUrlRequest(doc);
            Optional<String> dnntLabel = checkingDnnt.getDnntLabel(doc);
            String sdnntStatus = "";

            if (sdnntApi.getFieldValueFromResourceStr(SdnntField.STATUS, uuid).isEmpty()) {
                Optional<String> ccnb = checkingDnnt.getCcnbForUrlRequest(doc);

                if (ccnb.isEmpty() ||
                        sdnntApi.getFieldValueFromResourceStr(SdnntField.STATUS, ccnb.get()).isEmpty()) {
                    mismatch.put(uuid, "no-found");
                } else {
                    sdnntStatus = sdnntApi.getFieldValueFromResourceStr(SdnntField.STATUS, ccnb.get()).get();
                }
            }
            else {
                sdnntStatus = sdnntApi.getFieldValueFromResourceStr(SdnntField.STATUS, uuid).get();
            }

            if (dnntLabel.isEmpty()) {
                if (!mismatch.containsKey(uuid)) {
                    mismatch.put(uuid, "covid");
                }
                continue;
            }

            if (!dnntLabel.get().equals(SdnntField.dnntLabelByChar(sdnntStatus))) {
                System.out.println("Mismatch: " + sdnntStatus + " == " + dnntLabel.get());
                mismatch.put(uuid, "problem-label");
            } else {
                System.out.println(sdnntStatus + " == " + dnntLabel.get());
            }
        }

        System.out.println("count: " + count);
        System.out.println("Mismatch lenght: " + mismatch.size());
        for (Map.Entry<String, String> str : mismatch.entrySet()) {
            System.out.println(str.getKey() + " -> " + str.getValue());
        }
    }
}
