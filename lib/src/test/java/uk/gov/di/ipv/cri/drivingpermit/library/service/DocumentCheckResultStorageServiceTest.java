package uk.gov.di.ipv.cri.drivingpermit.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCheckResultStorageServiceTest {

    @Mock DataStore<DocumentCheckResultItem> mockDataStore;

    private DocumentCheckResultStorageService documentCheckResultStorageService;

    @BeforeEach
    void setUp() {
        documentCheckResultStorageService = new DocumentCheckResultStorageService(mockDataStore);
    }

    @Test
    void shouldSaveDocumentCheckResultItem() {

        assertDoesNotThrow(
                () ->
                        documentCheckResultStorageService.saveDocumentCheckResult(
                                new DocumentCheckResultItem()));
    }

    @Test
    void shouldGetDocumentCheckResult() {

        UUID testUUID = UUID.randomUUID();

        DocumentCheckResultItem testItem = new DocumentCheckResultItem();

        when(mockDataStore.getItem(testUUID.toString())).thenReturn(testItem);

        DocumentCheckResultItem returnedItem =
                assertDoesNotThrow(
                        () -> documentCheckResultStorageService.getDocumentCheckResult(testUUID));

        assertEquals(testItem, returnedItem);
    }
}
