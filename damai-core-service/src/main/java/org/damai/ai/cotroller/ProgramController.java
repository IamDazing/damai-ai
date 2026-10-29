package org.damai.ai.cotroller;

import jakarta.annotation.Resource;
import org.damai.ai.ai.function.call.ProgramCall;
import org.damai.ai.ai.function.dto.ProgramSearchFunctionDto;
import org.damai.ai.dto.ProgramDetailDto;
import org.damai.ai.service.HybridSearchService;
import org.damai.ai.vo.ProgramSearchVo;
import org.damai.ai.vo.result.ProgramDetailResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CookieValue;
import org.damai.ai.security.AssistantConversationScope;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.stream.Collectors;

import static org.damai.ai.constants.DaMaiConstant.RAG_VERSION;

@RestController
@RequestMapping("/program")
public class ProgramController {

    private static final Logger log = LoggerFactory.getLogger(ProgramController.class);

    @Autowired
    private ProgramCall programCall;

    @Resource
    private ChatClient assistantChatClient;

    @Resource
    private ChatClient markdownChatClient;
    
    @Resource
    private ChatClient analysisChatClient;
    
    @Resource
    private HybridSearchService hybridSearchService;
    
    @Value("${"+RAG_VERSION+":1}")
    private Integer ragVersion;
    
    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                                @RequestParam("chatId") String chatId,
                                @CookieValue(value = "Admin-Token", required = false) String token) {
        return assistantChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, AssistantConversationScope.scope(token, chatId)))
                .toolContext(Map.of("damaiToken", token))
                .stream()
                .content()
                .timeout(Duration.ofSeconds(45))
                .switchIfEmpty(Flux.just("抱歉，暂时没有收到回复，请稍后再试。"))
                .onErrorResume(e -> Flux.just("\n\n抱歉，回复中断或服务暂时不可用，请稍后再试。"));
    }
    
    @RequestMapping(value = "/rag", produces = "text/html;charset=utf-8")
    public Flux<String> rag(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @CookieValue(value = "Admin-Token", required = false) String token) {
        String scopedChatId = AssistantConversationScope.scope(token, chatId);
        final Integer ragTwoVersionValue = 2;
        if (ragVersion.equals(ragTwoVersionValue)) {
            List<Document> documents = hybridSearchService.hybridSearch(prompt, 10, true);
            log.info("混合检索返回 {} 个文档", documents.size());
            
            String context = documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));
            
            String enhancedPrompt = """
                以下是检索到的相关上下文信息：
                ---------------------
                %s
                ---------------------
                请基于上述上下文信息回答用户问题。如果上下文中没有相关信息，请告知用户。
                
                用户问题：%s
                """.formatted(context, prompt);
            
            return markdownChatClient.prompt()
                    .user(enhancedPrompt)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, scopedChatId))
                    .stream()
                    .content();
        }
        return markdownChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, scopedChatId))
                .stream()
                .content();
    }
    
    @RequestMapping(value = "/chat/mcp", produces = "text/html;charset=utf-8")
    public Flux<String> chatMcp(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @CookieValue(value = "Admin-Token", required = false) String token) {
        String scopedChatId = AssistantConversationScope.scope(token, chatId);
        // 请求模型（MCP工具已在 analysisChatClient 中全局配置）
        return analysisChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, scopedChatId))
                .stream()
                .content();
    }
    
    @PostMapping(value = "/search")
    public List<ProgramSearchVo> search(@RequestBody ProgramSearchFunctionDto programSearchFunctionDto) {
        return programCall.search(programSearchFunctionDto);
    }

    @PostMapping(value = "/detail")
    public ProgramDetailResultVo search(@RequestBody ProgramDetailDto programDetailDto) {
        return programCall.detail(programDetailDto);
    }
}
