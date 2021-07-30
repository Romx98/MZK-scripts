package cz.mzk.scripts.services;

import cz.mzk.scripts.clientapi.SolrClientUtils;
import cz.mzk.scripts.model.SolrField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class SolrVcService {

    private final SolrClient solrClient;

    public SolrVcService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void removeVc(String idVc, String uuid) throws SolrServerException, IOException {
        SolrQuery query = createQueryByRootUuid(uuid);
        SolrClientUtils.singleRequestAndApply(query, solrClient, createUnlinkConsumer(idVc));
    }

    @SuppressWarnings("unchecked")
    private Consumer<SolrDocument> createUnlinkConsumer(String idVc) {
        return solrDoc -> {
            List<String> collections = (List<String>) solrDoc.getFieldValue(SolrField.COLLECTION);

            if ((collections != null) && (collections.contains(idVc))) {
                collections.remove(idVc);
                String uuid = (String) solrDoc.getFieldValue(SolrField.UUID);
                try {
                    setVcFor(uuid, collections);
                } catch (SolrServerException | IOException e) {
                    System.out.println("Cannot apply changes to " + uuid
                            + ", exception caught: '" + e.getMessage() + "'");
                }
            }
        };
    }

    private SolrQuery createQueryByRootUuid(String uuid) {
        String allPartsQueryStr = SolrClientUtils.wrapQueryStr(SolrField.UUID, uuid);

        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.COLLECTION);
        return solrQuery;
    }

    private void setVcFor(String uuid, Object collectionList) throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();

        inputDoc.addField(SolrField.UUID, uuid);
        SolrClientUtils.updateSolrFieldValue(inputDoc, SolrField.COLLECTION, collectionList);
        SolrClientUtils.updateSolrFieldValue(inputDoc, SolrField.MODIFIED_DATE, new Date());
        solrClient.add(inputDoc);
    }



}
