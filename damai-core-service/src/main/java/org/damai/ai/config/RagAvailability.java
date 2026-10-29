package org.damai.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RagAvailability {
    private static final Logger log = LoggerFactory.getLogger(RagAvailability.class);
    private volatile boolean ready;

    public void initialize(VectorStore store, List<Document> documents) {
        ready = false;
        if (documents == null || documents.isEmpty()) {
            log.warn("规则知识库没有可用文档");
            return;
        }
        try {
            store.add(documents);
            ready = true;
        } catch (RuntimeException e) {
            log.warn("规则知识库初始化失败，规则问答暂不可用；其他助手可继续使用。错误类型：{}", e.getClass().getSimpleName());
        }
    }

    public boolean isReady() { return ready; }
}
