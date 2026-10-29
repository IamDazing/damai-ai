package org.damai.ai.cotroller;

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
        ChatTypeHistoryVo first = new ChatTypeHistoryVo();
        first.setChatId(AssistantConversationScope.scope("token-a", "101"));
        when(service.getChatTypeHistoryList(2)).thenAnswer(inv -> {
            ChatTypeHistoryVo a = new ChatTypeHistoryVo(); a.setChatId(AssistantConversationScope.scope("token-a", "101"));
            ChatTypeHistoryVo b = new ChatTypeHistoryVo(); b.setChatId(AssistantConversationScope.scope("token-b", "202"));
            return List.of(a, b);
        });
        ChatTypeHistoryController controller = new ChatTypeHistoryController(service, memory);
        assertEquals(List.of("101"), controller.getChatTypeHistoryList(2, "token-a").stream().map(ChatTypeHistoryVo::getChatId).toList());
        assertTrue(controller.getChatTypeHistoryList(2, "token-bad").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> controller.getChatHistory(first.getChatId(), 1, "token-a"));
    }

    @Test void everyConversationTypeIsScopedForReadAndDelete() {
        ChatTypeHistoryService service = mock(ChatTypeHistoryService.class);
        ChatMemory memory = mock(ChatMemory.class);
        when(service.getChatTypeHistoryList(anyInt())).thenAnswer(inv -> {
            ChatTypeHistoryVo a = new ChatTypeHistoryVo(); a.setChatId(AssistantConversationScope.scope("token-a", "101"));
            ChatTypeHistoryVo b = new ChatTypeHistoryVo(); b.setChatId(AssistantConversationScope.scope("token-b", "202"));
            ChatTypeHistoryVo legacy = new ChatTypeHistoryVo(); legacy.setChatId("legacy");
            return List.of(a, b, legacy);
        });
        ChatTypeHistoryController controller = new ChatTypeHistoryController(service, memory);
        for (int type : new int[]{1, 2, 3}) {
            assertEquals(List.of("101"), controller.getChatTypeHistoryList(type, "token-a").stream().map(ChatTypeHistoryVo::getChatId).toList());
            when(memory.get(AssistantConversationScope.scope("token-a", "101"))).thenReturn(List.of());
            controller.getChatHistory("101", type, "token-a");
            controller.delete(type, "101", "token-a");
            verify(service).delete(type, AssistantConversationScope.scope("token-a", "101"));
            assertThrows(IllegalArgumentException.class, () -> controller.getChatHistory(AssistantConversationScope.scope("token-b", "202"), type, "token-a"));
        }
        verify(memory, times(3)).get(AssistantConversationScope.scope("token-a", "101"));
    }
}
