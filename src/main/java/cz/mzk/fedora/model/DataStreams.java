package cz.mzk.fedora.model;

import java.util.Optional;

public enum DataStreams {

    RELS_EXT("RELS-EXT", "application/rdf+xml" , "true", "X", "A");

    public String name;
    public String mimeType;
    public String versionable;
    public String controlGroup;
    public String state;

    DataStreams(String dsName, String dsMimeType,
                String dsVersionable, String dsControlGroup, String dsState) {
        name = dsName;
        mimeType = dsMimeType;
        versionable = dsVersionable;
        controlGroup = dsControlGroup;
        state = dsState;
    }

    public DataStreams getDataStreams(String dsName) {
        DataStreams dataStreams = null;

        for (DataStreams ds : DataStreams.values()) {
            if (ds.name.equals(dsName)) {
                dataStreams = ds;
            }
        }
        return dataStreams;
    }
}
