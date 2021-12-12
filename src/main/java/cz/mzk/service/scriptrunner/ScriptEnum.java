package cz.mzk.service.scriptrunner;

import cz.mzk.scripts.SyncFoxmlFieldsWithSolr;
import lombok.Getter;

@Getter
public enum ScriptEnum {

    FOXML2SOLR_SYNC(SyncFoxmlFieldsWithSolr.class.toString(), "foxl2solrsync");

    private String name;
    private final String scriptClassName;
    private final String outputSubFolder;

    ScriptEnum(final String scriptClassName, final String outputSubFolder) {
        this.scriptClassName = scriptClassName;
        this.outputSubFolder = outputSubFolder;
    }
}
