package cz.mzk.scripts;

import cz.mzk.constants.SolrField;
import cz.mzk.rest.CustomSolrClient;
import cz.mzk.rest.FedoraRestClient;
import cz.mzk.utils.FoxmlUtils;
import cz.mzk.utils.SolrUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SyncFoxmlFieldsWithSolr {

    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        final String fedoraHost = "";
        final String fedoraUser = "";
        final String fedoraPswd = "";
        final String solrHost = "";

        final int MAX_DOCS_PER_SOLR_QUERY = 5_000;

        final CustomSolrClient solrClient = new CustomSolrClient(solrHost);
        final FedoraRestClient fedoraClient = new FedoraRestClient(fedoraHost, fedoraUser, fedoraPswd);

        final List<BiConsumer<Document, SolrInputDocument>> fieldSynchronizers = new ArrayList<>() {{
            add(createCNBSynchronizer());
            add(createISSNSynchronizer());
        }};

        final SolrQuery fetchAllRootsSolrQuery = createFetchAllRootsWithoutTheRequiredFieldsSolrQuery(MAX_DOCS_PER_SOLR_QUERY);

        solrClient.queryAndApply(fetchAllRootsSolrQuery, (solrDoc) -> {
            // TODO implementation
            // extract uuid from solrDoc
            // create SolrInputDocument object
            // fetch FOXML by the given root uuid
            // apply each field synchronizer
                // pass FOXML and SolrInputDocument object as parameters
            // if the result SolrInputDocument object has any field to update, send it to Solr
            String uuidDoc = (String) solrDoc.getFieldValue(SolrField.UUID);
            SolrInputDocument solrInputDoc = new SolrInputDocument();
            Optional<Document> foxml = fedoraClient.getDC(uuidDoc);

            foxml.ifPresent(doc -> fieldSynchronizers.forEach(x -> x.accept(doc, solrInputDoc)));

            if (!solrInputDoc.isEmpty()) {
                solrClient.sendSolrInputDocument(solrInputDoc);
            }

        }, MAX_DOCS_PER_SOLR_QUERY);

        solrClient.close(true);
    }

    private static BiConsumer<Document, SolrInputDocument> createCNBSynchronizer() {
        return (foxml, solrInputDoc) -> {
            // TODO implementation
            // add all required Solr field names and XPath expression parsing to SolrField.java and FoxmlUtils.java
            // parse the required field in FOXML
            // if exists and is not blank, update SolrInputDocument object by SolrUtils.java
            List<String> listOfCNB = new FoxmlUtils().getListOfCNBFromFoxml(foxml);

            if (!listOfCNB.isEmpty()) {
                Collection<Object> solrIdentifier = solrInputDoc.getFieldValues(SolrField.DC_IDENT);
                solrIdentifier.addAll(listOfCNB);

                SolrUtils.setModify(solrInputDoc, SolrField.DC_IDENT, solrIdentifier);
                SolrUtils.setModify(solrInputDoc, SolrField.MODIFIED_DATE, SolrUtils.findOutCurrentTimeStr());
            }
        };
    }

    private static BiConsumer<Document, SolrInputDocument> createISSNSynchronizer() {
        return (foxml, solrInputDoc) -> {
            // TODO implementation
            // the same as in the previous function for ISSN
            Optional<String> issn = new FoxmlUtils().getStrISSNFromFoxml(foxml);

            if (issn.isPresent()) {
                SolrUtils.setModify(solrInputDoc, SolrField.ISSN, issn);
                SolrUtils.setModify(solrInputDoc, SolrField.MODIFIED_DATE, SolrUtils.findOutCurrentTimeStr());
            }
        };
    }

    private static SolrQuery createFetchAllRootsWithoutTheRequiredFieldsSolrQuery(final int maxRows) {
        final SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(
                (SolrUtils.queryNoFieldValue(SolrField.ISSN) + " OR " +
                        SolrUtils.queryNoStrFieldValueByRegex(SolrField.DC_IDENT, "cnb")) + " OR " +
                (SolrUtils.queryFieldValue(SolrField.ISSN, "") + " OR " +
                        SolrUtils.queryNoStrFieldValueByRegex(SolrField.DC_IDENT, "cnb")));
        solrQuery.addFilterQuery(SolrUtils.filterQueryRootPids());
        solrQuery.setRows(maxRows);

        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.DC_IDENT);
        solrQuery.addField(SolrField.ISSN);
        solrQuery.addField(SolrField.MODIFIED_DATE);
        return solrQuery;
    }
}
