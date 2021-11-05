package cz.mzk.utils;


import org.apache.solr.common.SolrInputDocument;

import java.util.Collections;

public class SolrUtils {

    public static void setModify(final SolrInputDocument inputDocument, final String fieldKey, final Object fieldValue) {
        inputDocument.addField(fieldKey, Collections.singletonMap("set", fieldValue));
    }

    public static String wrapQueryStrForEmptyValue(String fieldName) {
        return "-" + fieldName + ":[\"\" TO * ]";
    }

    public static String wrapNegatedQueryByRegexStr(String fieldName, String searchText) {
        return "-" + fieldName + ":" + "/.*" + searchText + "*./";
    }

}
