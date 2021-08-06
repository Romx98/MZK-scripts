package cz.mzk.scripts.services;

import cz.mzk.scripts.model.FedoraModel;
import cz.mzk.scripts.model.SolrField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.Optional;

public class SolrCheckingDnnt {

    public final SolrClient solrClient;

    public SolrCheckingDnnt(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public void printAllPeriodicalDnnt(String fromYear, String toYear)
            throws SolrServerException, IOException {
        SolrQuery solrQuery = createQueryByYearOfPeriodical(fromYear, toYear);
        SolrDocumentList docs = SolrClientUtils.getSolrDocListAndSetRows(solrQuery, solrClient);
        docs.forEach(x -> System.out.println(x.getFieldValue(SolrField.IDENTIFIER).toString()));
    }

    public SolrDocumentList getSolrDocListResponse(String fromYear, String toYear)
            throws SolrServerException, IOException {
        SolrQuery solrQuery = createQueryByYearOfPeriodical(fromYear, toYear);
        return SolrClientUtils.getSolrDocListAndSetRows(solrQuery, solrClient);
    }

    public String getDnntLabel(SolrDocument doc) {
        return SolrClientUtils.getFieldValueByFieldName(doc, SolrField.DNNT_LABELS);
    }

    public String getUuidForUrlRequest(SolrDocument doc) {
        String uuid = SolrClientUtils.getFieldValueByFieldName(doc, SolrField.UUID);
        return replaceColonWithEqualSing(uuid);
    }

    private String replaceColonWithEqualSing(String str) {
        return str.replace(":", "=");
    }

    public Optional<String> getCcnbForUrlRequest(SolrDocument doc) {
        String identifier = SolrClientUtils.getFieldValueByFieldName(doc, SolrField.IDENTIFIER);
        return splitStringAndGetCcnb(identifier);
    }

    private Optional<String> splitStringAndGetCcnb(String str) {
        String[] fieldStr = str.split(",");
        for (String s : fieldStr) {
            if (s.contains("cnb") && !s.contains("ccnb")) {
                return Optional.of("cnb=" + s.trim().replace("[()]",""));
            }
        }
        return Optional.empty();
    }

    public SolrQuery createQueryByYearOfPeriodical(String fromYear, String toYear) {
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

}
