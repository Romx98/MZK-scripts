package cz.mzk.utils;


import cz.mzk.constants.SolrField;
import org.apache.solr.common.SolrInputDocument;

import java.util.Collections;

public class SolrUtils {

    public static void setModify(final SolrInputDocument inputDocument, final String fieldKey, final Object fieldValue) {
        inputDocument.addField(fieldKey, Collections.singletonMap("set", fieldValue));
    }

    public static String queryNoFieldValue(String fieldName) {
        return "-" + fieldName + ":[\"\" TO * ]";
    }

    public static String queryNoStrFieldValueByRegex(String fieldName, String searchText) {
        return "-" + fieldName + ":" + "/.*" + searchText + ".*/";
    }

    public static String filterQueryRootPid() {
        return "{!frange l=1 u=1 v=eq(" + SolrField.UUID +", " + SolrField.ROOT_PID + ")}";
    }

}
