package org.damai.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RagAvailabilityTest {
    @Test void embeddingFailureDoesNotPreventApplicationStartup() {
        RagAvailability availability = new RagAvailability();
        VectorStore store = mock(VectorStore.class);
        List<Document> docs = List.of(new Document("test"));
        doThrow(new IllegalStateException("offline")).when(store).add(docs);
        assertDoesNotThrow(() -> availability.initialize(store, docs));
        assertFalse(availability.isReady());
        reset(store);
        availability.initialize(store, docs);
        assertTrue(availability.isReady());
        availability.initialize(store, List.of());
        assertFalse(availability.isReady());
    }
}
