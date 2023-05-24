package uk.gov.di.ipv.cri.drivingpermit.api.service;

import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.UUID;

public class DocumentCheckRetrievalService {

    private final DataStore<DocumentCheckResultItem> dataStore;
    private final ConfigurationService configurationService;

    DocumentCheckRetrievalService(
            DataStore<DocumentCheckResultItem> dataStore,
            ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.dataStore = dataStore;
    }

    public DocumentCheckRetrievalService() {
        this.configurationService =
                new ConfigurationService(
                        ParamManager.getSecretsProvider(),
                        ParamManager.getSsmProvider(),
                        System.getenv("ENVIRONMENT"));
        this.dataStore =
                new DataStore<DocumentCheckResultItem>(
                        configurationService.getDocumentCheckResultTableName(),
                        DocumentCheckResultItem.class,
                        DataStore.getClient());
    }

    public DocumentCheckResultItem getDocumentCheckResult(UUID sessionId) {
        return dataStore.getItem(sessionId.toString());
    }
}
