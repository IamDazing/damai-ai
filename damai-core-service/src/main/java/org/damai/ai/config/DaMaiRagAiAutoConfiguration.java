package org.damai.ai.config;

import org.damai.ai.advisor.ChatTypeHistoryAdvisor;
import org.damai.ai.advisor.ChatTypeTitleAdvisor;
import org.damai.ai.advisor.QueryRewriteAdvisor;
import org.damai.ai.ai.rag.MarkdownLoader;
import org.damai.ai.enums.ChatType;
import org.damai.ai.advisor.AiObservabilityAdvisor;
import org.damai.ai.service.AiObservabilityService;
import org.damai.ai.service.ChatTypeHistoryService;
import org.damai.ai.service.HybridSearchService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

import static org.damai.ai.constants.DaMaiConstant.CHAT_TITLE_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.CHAT_TYPE_HISTORY_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.MARK_DOWN_SYSTEM_PROMPT;
import static org.damai.ai.constants.DaMaiConstant.MESSAGE_CHAT_MEMORY_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.OBSERVABILITY_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.RAG_VERSION;

@AutoConfigureAfter(DaMaiAiAutoConfiguration.class)
public class DaMaiRagAiAutoConfiguration {
    
    @Bean
    public MarkdownLoader markdownLoader(ResourcePatternResolver resourcePatternResolver){
        return new MarkdownLoader(resourcePatternResolver);
    }
    
    @Bean("markdownChatClient")
    @ConditionalOnProperty(name = RAG_VERSION, havingValue = "1",matchIfMissing = true)
    public ChatClient markdownChatClientV1(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore,
                                           MarkdownLoader markdownLoader, ChatTypeHistoryService chatTypeHistoryService,
                                           @Qualifier("titleChatClient")ChatClient titleChatClient,
                                           AiObservabilityService observabilityService, RagAvailability ragAvailability,
                                           @Value("${spring.ai.openai.chat.options.model:qwen-plus}") String modelName) {
        List<Document> documentList = markdownLoader.loadMarkdowns();
        ragAvailability.initialize(vectorStore, documentList);
        
        return ChatClient
                .builder(model)
                .defaultSystem(MARK_DOWN_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.3)
                                        .topK(8)
                                        .build())
                                .build(),
                        // AI增强: Observability可观测性
                        AiObservabilityAdvisor.builder(observabilityService)
                                .order(OBSERVABILITY_ADVISOR_ORDER)
                                .modelName(modelName)
                                .requestType(ChatType.MARKDOWN.getMsg())
                                .build()
                )
                .build();
    }

    @Bean("markdownChatClient")
    @ConditionalOnProperty(name = RAG_VERSION, havingValue = "2")
    public ChatClient markdownChatClientV2(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore,
                                           MarkdownLoader markdownLoader, ChatTypeHistoryService chatTypeHistoryService,
                                           @Qualifier("titleChatClient")ChatClient titleChatClient,
                                           HybridSearchService hybridSearchService,
                                           AiObservabilityService observabilityService, RagAvailability ragAvailability,
                                           @Value("${spring.ai.openai.chat.options.model:qwen-plus}") String modelName) {
        List<Document> documentList = markdownLoader.loadMarkdowns();
        ragAvailability.initialize(vectorStore, documentList);
        
        hybridSearchService.cacheDocuments(documentList);
        
        return ChatClient
                .builder(model)
                .defaultSystem(MARK_DOWN_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        QueryRewriteAdvisor.builder()
                                .order(Ordered.HIGHEST_PRECEDENCE + 50)
                                .enableLLMRewrite(false)  
                                .build(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        // AI增强: Observability可观测性
                        AiObservabilityAdvisor.builder(observabilityService)
                                .order(OBSERVABILITY_ADVISOR_ORDER)
                                .modelName(modelName)
                                .requestType(ChatType.MARKDOWN.getMsg())
                                .build()
                )
                .build();
    }
}
