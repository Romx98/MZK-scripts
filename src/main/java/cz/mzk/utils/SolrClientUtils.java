package cz.mzk.utils;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

public class SolrClientUtils {

    public static SolrClient createSolrClient(String solrHost) {
        return new HttpSolrClient.Builder(solrHost)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public static void singleRequestAndApply(SolrQuery solrQuery, SolrClient solrClient,
                                             Consumer<SolrDocument> consumer)
            throws SolrServerException, IOException {
        SolrDocumentList docs = queryForSolrDocList(solrQuery, solrClient);
        docs.forEach(consumer);
    }

    public static SolrDocumentList queryForSolrDocList(SolrQuery solrQuery, SolrClient solrClient)
            throws SolrServerException, IOException {
        return solrClient.query(solrQuery).getResults();
    }

    public static void commitAndClose(SolrClient solrClient)
            throws SolrServerException, IOException {
        solrClient.commit();
        solrClient.close();
    }
}
