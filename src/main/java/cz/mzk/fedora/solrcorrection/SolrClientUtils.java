package cz.mzk.fedora.solrcorrection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SolrClientUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static SolrClient createSolrClient(String solrHost) {
        return new HttpSolrClient.Builder(solrHost)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    public static void singleRequestAndApply(SolrQuery solrQuery, SolrClient solrClient,
                                             Consumer<SolrDocument> consumer)
            throws SolrServerException, IOException {
        long numFound = queryForNumFound(solrQuery, solrClient);

        if(numFound != 0) {
            solrQuery.setRows(Math.toIntExact(numFound));
            SolrDocumentList docs = queryForSolrDocList(solrQuery, solrClient);
            docs.forEach(consumer);
        }
    }

    public static long queryForNumFound(SolrQuery solrQuery, SolrClient solrClient)
            throws SolrServerException, IOException {
        Integer oldRows = solrQuery.getRows();
        solrQuery.setRows(0);
        SolrDocumentList docs = queryForSolrDocList(solrQuery, solrClient);
        solrQuery.setRows(oldRows);
        return docs.getNumFound();
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
