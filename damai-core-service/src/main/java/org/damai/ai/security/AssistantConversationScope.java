package org.damai.ai.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class AssistantConversationScope {
    private AssistantConversationScope() {}

    public static String prefix(String token) {
        try {
            String identity = token == null || token.isBlank() ? "anonymous" : token;
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
            return "assistant-" + HexFormat.of().formatHex(digest) + ":";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String scope(String token, String chatId) {
        if (chatId == null || !chatId.matches("[A-Za-z0-9_-]{1,100}")) {
            throw new IllegalArgumentException("invalid conversation id");
        }
        return prefix(token) + chatId;
    }

    public static boolean isScoped(String chatId) {
        return chatId != null && chatId.startsWith("assistant-");
    }
}
