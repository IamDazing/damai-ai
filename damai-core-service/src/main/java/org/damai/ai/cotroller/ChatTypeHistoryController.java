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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatTypeHistoryController {

    private final ChatTypeHistoryService chatHistoryService;

    private final ChatMemory chatMemory;
    
    @RequestMapping("/type/history/list")
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(@RequestParam("type") Integer type) {
        return chatHistoryService.getChatTypeHistoryList(type);
    }

    @RequestMapping("/history/message/list")
    public List<ChatHistoryMessageVO> getChatHistory(@RequestParam("chatId") String chatId,@RequestParam("type") Integer type) {
        List<Message> messages = chatMemory.get(chatId);
        return messages.stream().map(ChatHistoryMessageVO::new).toList();
    }
    
    @RequestMapping(value = "/delete")
    public ApiResponse<Void> delete(@RequestParam("type") Integer type, @RequestParam("chatId") String chatId){
        chatHistoryService.delete(type, chatId);
        return ApiResponse.ok();
    }
    
    
}
