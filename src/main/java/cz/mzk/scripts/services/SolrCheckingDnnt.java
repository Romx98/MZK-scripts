package cz.mzk.scripts.services;

import cz.mzk.scripts.client_api.SolrClientUtils;
import cz.mzk.scripts.model.FedoraModel;
import cz.mzk.scripts.model.SolrField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class SolrCheckingDnnt {

    public final SolrClient solrClient;

    public SolrCheckingDnnt(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void printAllPeriodicalDnnt(String fromYear, String toYear)
            throws SolrServerException, IOException {
        SolrQuery solrQuery = createQueryByYearOfPeriodical(fromYear, toYear);
        SolrClientUtils.printResponse(solrQuery, solrClient);
    }

    public SolrQuery createQueryByYearOfPeriodical(String fromYear, String toYear) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(
                SolrClientUtils.wrapQueryStr(SolrField.MODEL, FedoraModel.PERIODICAL) + " dnnt:true");
        solrQuery.setFilterQueries(
                SolrClientUtils.wrapFilterQueryStr(SolrField.YEAR, fromYear, toYear));

        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.IDENTIFIER);
        return solrQuery;
    }

}
