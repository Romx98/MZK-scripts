package cz.mzk.service.scriptrunner;

import cz.mzk.scripts.SyncFoxmlFieldsWithSolr;
import lombok.Getter;

@Getter
public enum ScriptEnum {

    FOXML2SOLR_SYNC(SyncFoxmlFieldsWithSolr.class.toString());

    private String name;
    private final String scriptClass;

    private ScriptEnum(final String scriptClass) {
        this.scriptClass = scriptClass;
    }
}
