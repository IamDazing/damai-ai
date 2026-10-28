package org.damai.ai.config;


import org.damai.ai.advisor.ChatTypeHistoryAdvisor;
import org.damai.ai.advisor.ChatTypeTitleAdvisor;
import org.damai.ai.ai.function.AiProgram;
import org.damai.ai.constants.DaMaiConstant;
import org.damai.ai.enums.ChatType;
import org.damai.ai.advisor.AiObservabilityAdvisor;
import org.damai.ai.service.AiObservabilityService;
import org.damai.ai.service.ChatTypeHistoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import static org.damai.ai.constants.DaMaiConstant.CHAT_TITLE_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.CHAT_TYPE_HISTORY_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.MESSAGE_CHAT_MEMORY_ADVISOR_ORDER;
import static org.damai.ai.constants.DaMaiConstant.OBSERVABILITY_ADVISOR_ORDER;

public class DaMaiAiAutoConfiguration {


    @Bean
    public ChatClient chatClient(DeepSeekChatModel model) {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一位智能助手，你的特点是温柔、善良，你的名字叫智能小艾，要结合你的特点积极的回答用户的问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient assistantChatClient(DeepSeekChatModel model, ChatMemory chatMemory, AiProgram aiProgram,
                                          ChatTypeHistoryService chatTypeHistoryService,
                                          @Qualifier("titleChatClient")ChatClient titleChatClient,
                                          AiObservabilityService observabilityService) {
        return ChatClient
                .builder(model)
                .defaultSystem(DaMaiConstant.DA_MAI_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.ASSISTANT.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.ASSISTANT.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        // AI增强: Observability可观测性 - Token统计、延迟监控
                        AiObservabilityAdvisor.builder(observabilityService)
                                .order(OBSERVABILITY_ADVISOR_ORDER)
                                .modelName("deepseek-chat")
                                .requestType(ChatType.ASSISTANT.getMsg())
                                .build()
                )
                .defaultTools(aiProgram)
                .build();
    }
    
    @Bean
    public ChatClient analysisChatClient(DeepSeekChatModel model, ChatMemory chatMemory,
                                          ChatTypeHistoryService chatTypeHistoryService,
                                          @Qualifier("titleChatClient")ChatClient titleChatClient,
                                          @Qualifier("mcpToolCallbackProvider") ToolCallbackProvider mcpToolCallbackProvider,
                                          AiObservabilityService observabilityService) {
        return ChatClient
                .builder(model)
                .defaultSystem(DaMaiConstant.DA_MAI_ANALYSIS_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.ANALYSIS.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.ANALYSIS.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        // AI增强: Observability可观测性 - Token统计、延迟监控
                        AiObservabilityAdvisor.builder(observabilityService)
                                .order(OBSERVABILITY_ADVISOR_ORDER)
                                .modelName("deepseek-chat")
                                .requestType(ChatType.ANALYSIS.getMsg())
                                .build()
                )
                // 使用 MCP 工具（日志查询等）
                .defaultToolCallbacks(mcpToolCallbackProvider)
                .build();
    }
    
    @Bean
    public ChatClient titleChatClient(DeepSeekChatModel model) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
    
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
