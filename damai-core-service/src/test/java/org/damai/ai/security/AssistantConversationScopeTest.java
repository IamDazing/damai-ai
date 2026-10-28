package org.damai.ai.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssistantConversationScopeTest {
    @Test void sameChatIdIsSeparateForDifferentTokens() {
        String first = AssistantConversationScope.scope("token-a", "12345");
        String second = AssistantConversationScope.scope("token-b", "12345");
        assertNotEquals(first, second);
        assertTrue(first.startsWith(AssistantConversationScope.prefix("token-a")));
        assertFalse(first.contains("token-a"));
    }
    @Test void scopedIdsCannotBeInjectedAsClientIds() {
        assertThrows(IllegalArgumentException.class, () -> AssistantConversationScope.scope("token-a", "assistant-foo:123"));
    }
}
