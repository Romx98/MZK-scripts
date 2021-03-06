package cz.mzk.constants;

public enum DataStreams {

    RELS_EXT("RELS-EXT", "application/rdf+xml" , "true", "X", "A"),
    DC("DC", "text/xml", "false", "X", "A");

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
