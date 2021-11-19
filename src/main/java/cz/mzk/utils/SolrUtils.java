package cz.mzk.utils;


import cz.mzk.constants.SolrField;
import org.apache.solr.common.SolrInputDocument;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class SolrUtils {

    public static void setModify(final SolrInputDocument inputDocument, final String fieldKey, final Object fieldValue) {
        inputDocument.addField(fieldKey, Collections.singletonMap("set", fieldValue));
    }

    public static String findOutCurrentTimeStr() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateFormat.format(new Date());
    }

    public static String queryFieldValue(final String fieldName, final String fieldValue) {
        return fieldName + ":\"" + fieldValue + "\"";
    }

    public static String queryNoFieldValue(final String fieldName) {
        return "-" + fieldName + ":*";
    }

    public static String queryNoStrFieldValueByRegex(final String fieldName, final String searchText) {
        return "-" + fieldName + ":" + "/.*" + searchText + ".*/";
    }

    public static String filterQueryRootPids() {
        return "{!frange l=1 u=1 v=eq(" + SolrField.UUID +", " + SolrField.ROOT_PID + ")}";
    }

}
