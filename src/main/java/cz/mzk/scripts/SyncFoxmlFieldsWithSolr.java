package cz.mzk.scripts;

import cz.mzk.constants.EnvironmentParam;
import cz.mzk.constants.SolrField;
import cz.mzk.rest.CustomSolrClient;
import cz.mzk.rest.FedoraRestClient;
import cz.mzk.service.scriptrunner.Script;
import cz.mzk.service.scriptrunner.ScriptEnum;
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

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

@Slf4j
public class SyncFoxmlFieldsWithSolr extends Script {

    private final ParamMap paramMap;
    private static final String DRY_MODE_PARAM = "DRY_MODE";
    private static final int MAX_DOCS_PER_SOLR_QUERY = 5_000;
    private static final FoxmlUtils foxmlUtils = new FoxmlUtils();

    private final FileWrapper updateIssnCnbRootsFileWriter;
    private final FileWrapper missingDocInFedora;
    private final FileWrapper skipped;
    private final FileWrapper notUpdated;

    public static void main(String[] args) throws IOException {
        final Map<String, Object> params = new HashMap<>() {{
            put(EnvironmentParam.FEDORA_HOST.name(), "http://localhost:8080/fedora");
            put(EnvironmentParam.FEDORA_USER.name(), "fedoraAdmin");
            put(EnvironmentParam.FEDORA_PSWD.name(), "fedoraAdmin");
            put(EnvironmentParam.SOLR_HOST.name(), "http://localhost:8983/solr/kramerius");
            put(DRY_MODE_PARAM, false);
        }};
        final SyncFoxmlFieldsWithSolr script = new SyncFoxmlFieldsWithSolr(params);
        script.run();
    }

    public SyncFoxmlFieldsWithSolr(final Map<String, Object> params) throws IOException {
        super(ScriptEnum.FOXML2SOLR_SYNC, params);
        paramMap = new ParamMap(params);
        final String outputSubFolder = scriptType.getOutputSubFolder();
        updateIssnCnbRootsFileWriter = new FileWrapper("updated-issn-cnb-roots", outputSubFolder);
        missingDocInFedora = new FileWrapper("missing-doc-in-fedora", outputSubFolder);
        skipped = new FileWrapper("skipping-due-to-exception", outputSubFolder);
        notUpdated = new FileWrapper("not-updated", outputSubFolder);
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
                    solrClient.add(inputDoc);
                    updateIssnCnbRootsFileWriter.writeLine(uuid);
                } else if (!isModified(inputDoc)) {
                    log.info("Can't find ISSN nor CNB for " + uuid);
                    notUpdated.writeLine(uuid);
                } else {
                    log.info("Dry mode is on, don't update Solr instance.");
                }
            } catch (final Exception e) {
                log.warn("An exception occurred: " + e.getMessage());
                e.printStackTrace();
                skipped.writeLine(uuid);
            }
        }, MAX_DOCS_PER_SOLR_QUERY);

        solrClient.close(true);
        updateIssnCnbRootsFileWriter.closeFile();
        missingDocInFedora.closeFile();
        notUpdated.closeFile();
        skipped.closeFile();
    }

    private static BiConsumer<Document, SolrInputDocument> createCNBSynchronizer() {
        return (dcDatastream, inputDoc) -> {
            final List<String> listOfCNB = foxmlUtils.getListOfCNBFromFoxml(dcDatastream);
            if (!listOfCNB.isEmpty()) {
                log.info("Found new CNB flags: " + listOfCNB);
                final Collection<Object> dcIdentifiers = inputDoc.getFieldValues(SolrField.DC_IDENT);
                dcIdentifiers.addAll(listOfCNB);

                SolrUtils.setModify(inputDoc, SolrField.DC_IDENT, dcIdentifiers);
                SolrUtils.setModify(inputDoc, SolrField.MODIFIED_DATE, SolrUtils.getCurrentTimeDate());
            }
        };
    }

    private static BiConsumer<Document, SolrInputDocument> createISSNSynchronizer() {
        return (dcDatastream, inputDoc) -> {
            final Optional<String> optionalIssn = foxmlUtils.getStrISSNFromFoxml(dcDatastream);
            optionalIssn.ifPresent(issn -> {
                log.info("Found new ISSN flag: " + issn);
                SolrUtils.setModify(inputDoc, SolrField.ISSN, issn);
                SolrUtils.setModify(inputDoc, SolrField.MODIFIED_DATE, SolrUtils.getCurrentTimeDate());
            });
        };
    }

    private static SolrQuery createFetchAllRootsWithoutRequiredFieldsSolrQuery() {
        final SolrQuery solrQuery = new SolrQuery();
        final String queryStr = SolrUtils.queryBuilder()
                .complex(expr -> expr
                        .empty(SolrField.ISSN)
                        .or()
                        .notRegex(SolrField.DC_IDENT, "cnb")
                )
                .or()
                .complex(expr -> expr
                        .is(SolrField.ISSN, "")
                        .or()
                        .notRegex(SolrField.DC_IDENT, "cnb")
                )
                .build();
        solrQuery.setQuery(queryStr);
        solrQuery.addFilterQuery(SolrUtils.filterQueryRootPids());
        solrQuery.setRows(MAX_DOCS_PER_SOLR_QUERY);

        solrQuery.addField(SolrField.UUID);
        solrQuery.addField(SolrField.DC_IDENT);
        solrQuery.addField(SolrField.ISSN);
        return solrQuery;
    }

    private static SolrInputDocument createSolrInputDoc(final SolrDocument solrDoc) {
        final String uuid = (String) solrDoc.get(SolrField.UUID);
        final Collection<Object> existingDcIdent = solrDoc.getFieldValues(SolrField.DC_IDENT);
        final SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField(SolrField.UUID, uuid);
        inputDoc.addField(SolrField.DC_IDENT, existingDcIdent != null ? existingDcIdent : new ArrayList<>());
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
            dryMode = (boolean) params.getOrDefault(DRY_MODE_PARAM, false);
        }
    }
}
