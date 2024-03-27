package uk.gov.di.ipv.cri.drivingpermit.library.service;

import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.UUID;

public class DocumentCheckResultStorageService {

    private final DataStore<DocumentCheckResultItem> dataStore;

    DocumentCheckResultStorageService(DataStore<DocumentCheckResultItem> dataStore) {
        this.dataStore = dataStore;
    }

    public DocumentCheckResultStorageService(String documentCheckTableName) {

        this.dataStore =
                new DataStore<>(
                        documentCheckTableName,
                        DocumentCheckResultItem.class,
                        DataStore.getClient());
    }

    public DocumentCheckResultItem getDocumentCheckResult(UUID sessionId) {
        return dataStore.getItem(sessionId.toString());
    }

    public void saveDocumentCheckResult(DocumentCheckResultItem documentCheckResultItem) {
        dataStore.create(documentCheckResultItem);
    }
}
