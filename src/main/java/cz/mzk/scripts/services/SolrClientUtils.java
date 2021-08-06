package cz.mzk.scripts.services;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class SolrClientUtils {

    public static SolrClient createSolrClient(String solrHost) {
        return new HttpSolrClient.Builder(solrHost)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public static void printResponse(SolrQuery solrQuery, SolrClient solrClient)
            throws SolrServerException, IOException {
        SolrDocumentList docs = queryForSolrDocList(solrQuery, solrClient);
        docs.forEach(System.out::println);
    }

    public static void singleRequestAndApply(SolrQuery solrQuery, SolrClient solrClient,
                                             Consumer<SolrDocument> consumer)
            throws SolrServerException, IOException {
        SolrDocumentList docs = queryForSolrDocList(solrQuery, solrClient);
        docs.forEach(consumer);
    }

    public static SolrDocumentList getSolrDocListAndSetRows(SolrQuery solrQuery, SolrClient solrClient)
            throws SolrServerException, IOException {
        long numbRow = queryForNumFound(solrQuery, solrClient);
        solrQuery.setRows(Math.toIntExact(numbRow));
        return queryForSolrDocList(solrQuery, solrClient);
    }

    public static long queryForNumFound(SolrQuery solrQuery, SolrClient solrClient)
            throws SolrServerException, IOException {
        return queryForSolrDocList(solrQuery, solrClient).getNumFound();
    }

    private static SolrDocumentList queryForSolrDocList(SolrQuery solrQuery, SolrClient solrClient)
            throws SolrServerException, IOException {
        return solrClient.query(solrQuery).getResults();
    }

    public static void updateSolrFieldValue(SolrInputDocument inputDoc, String fieldKey, Object fieldValue) {
        inputDoc.addField(fieldKey, Collections.singletonMap("set", fieldValue));
    }

    public static String wrapQueryStr(String fieldKey, String fieldValue) {
        return fieldKey + ":\"" + fieldValue.trim() + "\"";
    }

    public static String wrapFilterQueryStr(String fieldKey, String from, String to) {
        return fieldKey + ":[" + from + " TO " + to + "]";
    }

    public static void commit(SolrClient solrClient)
            throws SolrServerException, IOException {
        solrClient.commit();
        //solrClient.close();
    }

    public static void close(SolrClient solrClient) throws IOException {
        solrClient.close();
    }
}
