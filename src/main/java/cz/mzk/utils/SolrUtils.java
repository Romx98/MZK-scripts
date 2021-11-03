package cz.mzk.utils;

import org.apache.solr.common.SolrInputDocument;

import java.util.Collections;

public class SolrUtils {

    public static void setModify(final SolrInputDocument inputDocument, final String fieldKey, final Object fieldValue) {
        inputDocument.addField(fieldKey, Collections.singletonMap("set", fieldValue));
    }
}
