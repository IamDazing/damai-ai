package org.damai.ai.cotroller;

import lombok.RequiredArgsConstructor;
import org.damai.ai.common.ApiResponse;
import org.damai.ai.service.ChatTypeHistoryService;
import org.damai.ai.vo.ChatHistoryMessageVO;
import org.damai.ai.vo.ChatTypeHistoryVo;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RestController;
import org.damai.ai.security.AssistantConversationScope;
import org.damai.ai.ai.function.MyOrdersTool;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatTypeHistoryController {

    private final ChatTypeHistoryService chatHistoryService;

    private final ChatMemory chatMemory;

    private final MyOrdersTool myOrdersTool;
    
    @RequestMapping("/type/history/list")
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(@RequestParam("type") Integer type,
            @CookieValue(value = "Admin-Token", required = false) String token) {
        List<ChatTypeHistoryVo> histories = chatHistoryService.getChatTypeHistoryList(type);
        if (type != 2) return histories;
        token = myOrdersTool.isAuthenticated(token) ? token : null;
        String prefix = AssistantConversationScope.prefix(token);
        return histories.stream().filter(item -> item.getChatId() != null && item.getChatId().startsWith(prefix))
                .peek(item -> item.setChatId(item.getChatId().substring(prefix.length()))).toList();
    }

    @RequestMapping("/history/message/list")
    public List<ChatHistoryMessageVO> getChatHistory(@RequestParam("chatId") String chatId,@RequestParam("type") Integer type,
            @CookieValue(value = "Admin-Token", required = false) String token) {
        if (type != 2 && AssistantConversationScope.isScoped(chatId)) throw new IllegalArgumentException("invalid conversation id");
        if (type == 2 && !myOrdersTool.isAuthenticated(token)) token = null;
        List<Message> messages = chatMemory.get(type == 2 ? AssistantConversationScope.scope(token, chatId) : chatId);
        return messages.stream().map(ChatHistoryMessageVO::new).toList();
    }
    
    @RequestMapping(value = "/delete")
    public ApiResponse<Void> delete(@RequestParam("type") Integer type, @RequestParam("chatId") String chatId,
            @CookieValue(value = "Admin-Token", required = false) String token){
        if (type != 2 && AssistantConversationScope.isScoped(chatId)) throw new IllegalArgumentException("invalid conversation id");
        if (type == 2 && !myOrdersTool.isAuthenticated(token)) token = null;
        chatHistoryService.delete(type, type == 2 ? AssistantConversationScope.scope(token, chatId) : chatId);
        return ApiResponse.ok();
    }
    
    
}
