package org.damai.ai.ai.function;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.support.ToolCallbacks;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MyOrdersToolTest {
    @Test void noLoginDoesNotQueryService() {
        MyOrdersTool tool = new MyOrdersTool("http://127.0.0.1:1");
        assertTrue(tool.listMyOrders(new ToolContext(Map.of())).contains("登录"));
    }
    @Test void forwardsTokenAndOnlyReturnsSelectedFields() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/orders", exchange -> {
            boolean valid = "test-token".equals(exchange.getRequestHeaders().getFirst("token"));
            byte[] result = (valid ? "{\"code\":0,\"data\":[{\"orderNumber\":\"42\",\"programTitle\":\"Concert\",\"orderStatus\":3,\"userId\":\"private\"}]}" : "{\"code\":516}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, result.length); exchange.getResponseBody().write(result); exchange.close();
        });
        server.start();
        try {
            MyOrdersTool tool = new MyOrdersTool("http://127.0.0.1:" + server.getAddress().getPort() + "/orders");
            assertTrue(tool.isAuthenticated("test-token"));
            assertFalse(tool.isAuthenticated("expired-token"));
            var callback = ToolCallbacks.from(tool)[0];
            assertFalse(callback.getToolDefinition().inputSchema().contains("damaiToken"));
            String result = callback.call("{}", new ToolContext(Map.of("damaiToken", "test-token")));
            assertTrue(result.contains("Concert"));
            assertTrue(result.contains("已支付"));
            assertFalse(result.contains("private"));
            assertFalse(result.contains("test-token"));
        } finally { server.stop(0); }
    }
}
