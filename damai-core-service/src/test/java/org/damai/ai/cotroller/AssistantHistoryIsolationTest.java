package org.damai.ai.cotroller;

import org.damai.ai.ai.function.MyOrdersTool;
import org.damai.ai.security.AssistantConversationScope;
import org.damai.ai.service.ChatTypeHistoryService;
import org.damai.ai.vo.ChatTypeHistoryVo;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssistantHistoryIsolationTest {
    @Test void historiesAreFilteredAndExpiredTokensCannotReadThem() {
        ChatTypeHistoryService service = mock(ChatTypeHistoryService.class);
        ChatMemory memory = mock(ChatMemory.class);
        MyOrdersTool tool = mock(MyOrdersTool.class);
        ChatTypeHistoryVo first = new ChatTypeHistoryVo();
        first.setChatId(AssistantConversationScope.scope("token-a", "101"));
        when(service.getChatTypeHistoryList(2)).thenAnswer(inv -> {
            ChatTypeHistoryVo a = new ChatTypeHistoryVo(); a.setChatId(AssistantConversationScope.scope("token-a", "101"));
            ChatTypeHistoryVo b = new ChatTypeHistoryVo(); b.setChatId(AssistantConversationScope.scope("token-b", "202"));
            return List.of(a, b);
        });
        when(tool.isAuthenticated("token-a")).thenReturn(true);
        ChatTypeHistoryController controller = new ChatTypeHistoryController(service, memory, tool);
        assertEquals(List.of("101"), controller.getChatTypeHistoryList(2, "token-a").stream().map(ChatTypeHistoryVo::getChatId).toList());
        assertTrue(controller.getChatTypeHistoryList(2, "expired-token").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> controller.getChatHistory(first.getChatId(), 1, "token-a"));
    }
}
