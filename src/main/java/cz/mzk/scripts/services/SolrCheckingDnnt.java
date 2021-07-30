package cz.mzk.scripts.services;

import cz.mzk.scripts.clientapi.SolrClientUtils;
import cz.mzk.scripts.model.FedoraModel;
import cz.mzk.scripts.model.SolrField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;

public class SolrCheckingDnnt {

    public final SolrClient solrClient;

    public SolrCheckingDnnt(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SolrQuery createQueryByYearOfPeriodical(String fromYear, String toYear) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("dnnt:true");
        solrQuery.setQuery(SolrClientUtils.wrapQueryStr(SolrField.MODEL, FedoraModel.PERIODICAL));
        solrQuery.setFilterQueries(
                SolrClientUtils.wrapFilterQueryStr(SolrField.YEAR, fromYear, toYear));

        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.IDENTIFIER);
        return solrQuery;
    }

}
