package cz.mzk.scripts.services;

import cz.mzk.scripts.model.SolrField;
import cz.mzk.scripts.services.solrutils.SolrClientUtils;
import cz.mzk.scripts.services.solrutils.SolrQueryUtils;
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

    public SolrDocumentList getSolrDocListResponse(String fromYear, String toYear)
            throws SolrServerException, IOException {
        SolrQuery solrQuery = SolrQueryUtils.createQueryByYearOfPeriodical(fromYear, toYear);
        return SolrClientUtils.getSolrDocListAndSetRows(solrQuery, solrClient);
    }

    public Optional<String> getDnntLabel(SolrDocument doc) {
        String str = SolrClientUtils.getFieldValueByFieldName(doc, SolrField.DNNT_LABELS);
        return fromArrayGetDnntLabel(str);
    }

    private Optional<String> fromArrayGetDnntLabel(String dnntLabel) {
        String[] fieldStr = dnntLabel.split(",");
        for (String str : fieldStr) {
            if (str.contains("dnnt")){
                return Optional.of(str.trim().replace("[", "").replace("]", ""));
            }
        }
        return Optional.empty();
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
                return Optional.of("cnb=" + s.trim().replace("[","").replace("]",""));
            }
        }
        return Optional.empty();
    }



}
