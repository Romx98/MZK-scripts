package cz.mzk.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class CustomSolrClient {

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int SOCKET_TIMEOUT = 60000;

    private final SolrClient solrClient;

    public CustomSolrClient(final String host) {
        Validate.notBlank(host);

        solrClient = new HttpSolrClient.Builder(host)
                .withConnectionTimeout(CONNECTION_TIMEOUT)
                .withSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    public SolrDocumentList query(final SolrQuery queryParams) {
        Validate.notNull(queryParams);

        try {
            return solrClient.query(queryParams).getResults();
        } catch (SolrServerException | IOException e) {
            log.warn("Can't query Solr for the document list! Return an empty result list!");
            e.printStackTrace();
            return new SolrDocumentList();
        }
    }

    public void queryAndApply(final SolrQuery queryParams, final Consumer<SolrDocument> consumer, final int maxDocForSingleRequest) {
        Validate.notNull(queryParams);
        Validate.notNull(consumer);

        if (getNumFound(queryParams) <= maxDocForSingleRequest) {
            query(queryParams).forEach(consumer);
        } else {
            paginate(queryParams, consumer);
        }
    }

    public void paginate(final SolrQuery queryParams, final Consumer<SolrDocument> consumer) {
        Validate.notNull(queryParams);
        Validate.notNull(consumer);

        try {
            String cursorMark = CursorMarkParams.CURSOR_MARK_START;
            boolean done = false;
            while (!done) {
                queryParams.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                final QueryResponse response = solrClient.query(queryParams);
                final String nextCursorMark = response.getNextCursorMark();
                response.getResults().forEach(consumer);
                if (cursorMark.equals(nextCursorMark)) {
                    done = true;
                }
                cursorMark = nextCursorMark;
            }
        } catch (SolrServerException | IOException e) {
            log.warn("Can't paginate a document list from Solr response due to exception! " + e.getMessage());
            e.printStackTrace();
        }
    }

    public long getNumFound(final SolrQuery queryParams) {
        Validate.notNull(queryParams);

        /* no need to get result document list, so remove result list size from query parameters  */
        /* but keep it to restore later because query parameters can be used later somewhere else */
        final int previousRows = queryParams.getRows();
        queryParams.setRows(0);
        long numFound = query(queryParams).getNumFound();
        queryParams.setRows(previousRows);
        return numFound;
    }

    public void addSolrInputDocument(final SolrInputDocument solrInputDoc)  {
        try {
            solrClient.add(solrInputDoc);
        } catch (SolrServerException | IOException e) {
            log.warn("Can't index a document to the Solr instance! " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close(final boolean commit) {
        try {
            if (commit) {
                solrClient.commit();
            }
            solrClient.close();
        } catch (SolrServerException e) {
            log.warn("Can't commit to the custom Solr client! " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.warn("Can't close the custom Solr client! " + e.getMessage());
            e.printStackTrace();
        }
    }
}
