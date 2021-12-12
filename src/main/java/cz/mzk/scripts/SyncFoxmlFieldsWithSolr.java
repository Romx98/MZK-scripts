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
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;

import java.util.*;
import java.util.function.BiConsumer;

@Slf4j
public class SyncFoxmlFieldsWithSolr extends Script {

    private final ParamMap paramMap;
    private final static int MAX_DOCS_PER_SOLR_QUERY = 5_000;
    private static final FoxmlUtils foxmlUtils = new FoxmlUtils();
    private static final FileWrapper updateIssnCnbRootsFileWriter = new FileWrapper("updated-issn-cnb-roots");
    private static final FileWrapper missingDocInFedora = new FileWrapper("missing-doc-in-fedora");
    private static final FileWrapper skipped = new FileWrapper("skipping-due-to-exception");

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
        final FedoraRestClient fedoraClient = new FedoraRestClient(
                paramMap.getFedoraHost(), paramMap.getFedoraUser(), paramMap.getFedoraPswd()
        );

        final List<BiConsumer<Document, SolrInputDocument>> fieldSynchronizers = new ArrayList<>() {{
            add(createCNBSynchronizer());
            add(createISSNSynchronizer());
        }};

        final SolrQuery fetchAllRootsSolrQuery = createFetchAllRootsWithoutRequiredFieldsSolrQuery();

        solrClient.queryAndApply(fetchAllRootsSolrQuery, (solrDoc) -> {
            final String uuid = (String) solrDoc.getFieldValue(SolrField.UUID);
            try {
                final SolrInputDocument inputDoc = createSolrInputDoc(solrDoc);
                final Optional<Document> dcDatastream = fedoraClient.getDC(uuid);

                log.info("Process " + uuid + "...");

                dcDatastream.ifPresentOrElse(dc ->
                        fieldSynchronizers.forEach(x -> x.accept(dc, inputDoc)),
                        () -> {
                            log.warn("No DC datastream for " + uuid + "!");
                            missingDocInFedora.writeLine(uuid);
                        }
                );

                if (isModified(inputDoc) && !paramMap.isDryMode()) {
                    log.info("Update " + uuid + " in destination Solr instance.");
                    solrClient.addSolrInputDocument(inputDoc);
                    updateIssnCnbRootsFileWriter.writeLine(uuid);
                }
            } catch (final Exception e) {
                log.warn("An exception occurred: " + e.getMessage());
                skipped.writeLine(uuid);
            }
        }, MAX_DOCS_PER_SOLR_QUERY);

        solrClient.close(true);
        updateIssnCnbRootsFileWriter.closeFile();
        missingDocInFedora.closeFile();
    }

    private static BiConsumer<Document, SolrInputDocument> createCNBSynchronizer() {
        return (dcDatastream, solrInputDoc) -> {
            final List<String> listOfCNB = foxmlUtils.getListOfCNBFromFoxml(dcDatastream);
            if (!listOfCNB.isEmpty()) {
                log.info("Found new CNB flags: " + listOfCNB);
                final Collection<Object> dcIdentifiers = solrInputDoc.getFieldValues(SolrField.DC_IDENT);
                dcIdentifiers.addAll(listOfCNB);

                SolrUtils.setModify(solrInputDoc, SolrField.DC_IDENT, dcIdentifiers);
                SolrUtils.setModify(solrInputDoc, SolrField.MODIFIED_DATE, SolrUtils.getCurrentTimeDate());
            }
        };
    }

    private static BiConsumer<Document, SolrInputDocument> createISSNSynchronizer() {
        return (dcDatastream, solrInputDoc) -> {
            final Optional<String> optionalIssn = foxmlUtils.getStrISSNFromFoxml(dcDatastream);
            optionalIssn.ifPresent(issn -> {
                log.info("Found new ISSN flag: " + issn);
                SolrUtils.setModify(solrInputDoc, SolrField.ISSN, issn);
                SolrUtils.setModify(solrInputDoc, SolrField.MODIFIED_DATE, SolrUtils.getCurrentTimeDate());
            });
        };
    }

    private static SolrQuery createFetchAllRootsWithoutRequiredFieldsSolrQuery() {
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

    private static SolrInputDocument createSolrInputDoc(final SolrDocument solrDoc) {
        final Collection<Object> existingDcIdent = solrDoc.getFieldValues(SolrField.DC_IDENT);
        final SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField(SolrField.DC_IDENT, existingDcIdent != null ? existingDcIdent : Collections.emptyList());
        return inputDoc;
    }

    private boolean isModified(final SolrInputDocument inputDoc) {
        return inputDoc.containsKey(SolrField.MODIFIED_DATE);
    }

    @Getter
    private static final class ParamMap {
        final String fedoraHost;
        final String fedoraUser;
        final String fedoraPswd;
        final String solrHost;
        final boolean dryMode;

        public ParamMap(final Map<String, Object> params) {
            fedoraHost = (String) params.getOrDefault(EnvironmentParam.FEDORA_HOST.name(), null);
            fedoraUser = (String) params.getOrDefault(EnvironmentParam.FEDORA_USER.name(), null);
            fedoraPswd = (String) params.getOrDefault(EnvironmentParam.FEDORA_PSWD.name(), null);
            solrHost = (String) params.getOrDefault(EnvironmentParam.SOLR_HOST.name(), null);
            dryMode = (boolean) params.getOrDefault("DRY_MODE", false);
        }
    }
}
