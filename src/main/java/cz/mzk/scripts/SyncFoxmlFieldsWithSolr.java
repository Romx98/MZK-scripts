package cz.mzk.scripts;

import cz.mzk.constants.EnvironmentParam;
import cz.mzk.constants.SolrField;
import cz.mzk.rest.CustomSolrClient;
import cz.mzk.rest.FedoraRestClient;
import cz.mzk.service.scriptrunner.Script;
import cz.mzk.utils.FileWrapper;
import cz.mzk.utils.FoxmlUtils;
import cz.mzk.utils.SolrUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;

import java.util.*;
import java.util.function.BiConsumer;

public class SyncFoxmlFieldsWithSolr extends Script {

    private final ParamMap paramMap;
    private final static int MAX_DOCS_PER_SOLR_QUERY = 5_000;
    private static final FoxmlUtils foxmlUtils = new FoxmlUtils();
    private static final FileWrapper updateIssnCnbRootsFileWriter = new FileWrapper("updated-issn-cnb-roots");
    private static final FileWrapper missingDocInFedora = new FileWrapper("missing-doc-in-fedora");

    public static void main(String[] args) {
        final Map<String, Object> params = new HashMap<>() {{
            put(EnvironmentParam.FEDORA_HOST.name(), "");
            put(EnvironmentParam.FEDORA_USER.name(), "");
            put(EnvironmentParam.FEDORA_PSWD.name(), "");
            put(EnvironmentParam.SOLR_HOST.name(), "");
        }};
        final SyncFoxmlFieldsWithSolr script = new SyncFoxmlFieldsWithSolr(params);
        script.run();
    }

    public SyncFoxmlFieldsWithSolr(final Map<String, Object> params) {
        super(params);
        paramMap = new ParamMap(params);
    }

    @SneakyThrows
    @Override
    public void run() {
        final CustomSolrClient solrClient = new CustomSolrClient(paramMap.getSolrHost());
        final FedoraRestClient fedoraClient = new FedoraRestClient(paramMap.getFedoraHost(), paramMap.getFedoraUser(), paramMap.getFedoraPswd());

        final List<BiConsumer<Document, SolrInputDocument>> fieldSynchronizers = new ArrayList<>() {{
            add(createCNBSynchronizer());
            add(createISSNSynchronizer());
        }};

        final SolrQuery fetchAllRootsSolrQuery = createFetchAllRootsWithoutTheRequiredFieldsSolrQuery();

        solrClient.queryAndApply(fetchAllRootsSolrQuery, (solrDoc) -> {
            final String uuidDoc = (String) solrDoc.getFieldValue(SolrField.UUID);
            final SolrInputDocument solrInputDoc = createSolrInputDocument(solrDoc);
            final Optional<Document> foxml = fedoraClient.getDC(uuidDoc);

            foxml.ifPresentOrElse(doc -> fieldSynchronizers.forEach(
                            x -> x.accept(doc, solrInputDoc)),
                    () -> missingDocInFedora.writeLine(uuidDoc)
            );

            if (solrInputDoc.containsKey(SolrField.MODIFIED_DATE)) {
                solrClient.addSolrInputDocument(solrInputDoc);
                updateIssnCnbRootsFileWriter.writeLine(uuidDoc);
            }

        }, MAX_DOCS_PER_SOLR_QUERY);

        updateIssnCnbRootsFileWriter.closeFile();
        missingDocInFedora.closeFile();
        solrClient.close(true);
    }

    private static BiConsumer<Document, SolrInputDocument> createCNBSynchronizer() {
        return (foxml, solrInputDoc) -> {
            final List<String> listOfCNB = foxmlUtils.getListOfCNBFromFoxml(foxml);
            if (!listOfCNB.isEmpty()) {
                final Collection<Object> solrIdentifier = solrInputDoc.getFieldValues(SolrField.DC_IDENT);
                solrIdentifier.addAll(listOfCNB);

                SolrUtils.setModify(solrInputDoc, SolrField.DC_IDENT, solrIdentifier);
                SolrUtils.setModify(solrInputDoc, SolrField.MODIFIED_DATE, SolrUtils.getCurrentTimeDate());
            }
        };
    }

    private static BiConsumer<Document, SolrInputDocument> createISSNSynchronizer() {
        return (foxml, solrInputDoc) -> {
            final Optional<String> issn = foxmlUtils.getStrISSNFromFoxml(foxml);
            if (issn.isPresent()) {
                SolrUtils.setModify(solrInputDoc, SolrField.ISSN, issn);
                SolrUtils.setModify(solrInputDoc, SolrField.MODIFIED_DATE, SolrUtils.getCurrentTimeDate());
            }
        };
    }

    private static SolrQuery createFetchAllRootsWithoutTheRequiredFieldsSolrQuery() {
        final SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(
                (SolrUtils.queryNoFieldValue(SolrField.ISSN) + " OR " +
                        SolrUtils.queryNoStrFieldValueByRegex(SolrField.DC_IDENT, "cnb")) + " OR " +
                (SolrUtils.queryFieldValue(SolrField.ISSN, "") + " OR " +
                        SolrUtils.queryNoStrFieldValueByRegex(SolrField.DC_IDENT, "cnb")));
        solrQuery.addFilterQuery(SolrUtils.filterQueryRootPids());
        solrQuery.setRows(MAX_DOCS_PER_SOLR_QUERY);

        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.DC_IDENT);
        solrQuery.addField(SolrField.ISSN);
        return solrQuery;
    }

    private static SolrInputDocument createSolrInputDocument(final SolrDocument solrDoc) {
        final Collection<Object> existingDcIdent = solrDoc.getFieldValues(SolrField.DC_IDENT);
        final SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField(SolrField.DC_IDENT, existingDcIdent != null ? existingDcIdent : Collections.emptyList());
        return inputDoc;
    }

    @Getter
    private static final class ParamMap {
        final String fedoraHost;
        final String fedoraUser;
        final String fedoraPswd;
        final String solrHost;

        public ParamMap(final Map<String, Object> params) {
            fedoraHost = (String) params.getOrDefault(EnvironmentParam.FEDORA_HOST.name(), null);
            fedoraUser = (String) params.getOrDefault(EnvironmentParam.FEDORA_USER.name(), null);
            fedoraPswd = (String) params.getOrDefault(EnvironmentParam.FEDORA_PSWD.name(), null);
            solrHost = (String) params.getOrDefault(EnvironmentParam.SOLR_HOST.name(), null);
        }
    }
}
