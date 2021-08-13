package cz.mzk.scripts.services.solrutils;

import cz.mzk.scripts.model.FedoraModel;
import cz.mzk.scripts.model.SolrField;
import org.apache.solr.client.solrj.SolrQuery;

public class SolrQueryUtils {

    public static SolrQuery createQueryByYearOfPeriodical(String fromYear, String toYear) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(
                SolrClientUtils.wrapQueryStr(SolrField.MODEL, FedoraModel.PERIODICAL) + " dnnt:true");
        solrQuery.setFilterQueries(
                SolrClientUtils.wrapFilterQueryStr(SolrField.YEAR, fromYear, toYear));

        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.IDENTIFIER);
        solrQuery.addField(SolrField.DNNT_LABELS);
        return solrQuery;
    }

    public static SolrQuery createQueryByRootUuid(String uuid) {
        String allPartsQueryStr = SolrClientUtils.wrapQueryStr(SolrField.UUID, uuid);

        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.COLLECTION);
        return solrQuery;
    }

    public static SolrQuery createQueryByFedoraModel(String model) {
        String allPartsQueryStr = SolrClientUtils.wrapQueryStr(SolrField.MODEL, model);

        SolrQuery solrQuery = new SolrQuery(allPartsQueryStr);
        solrQuery.addField(SolrField.UUID);
        return solrQuery;
    }
}
