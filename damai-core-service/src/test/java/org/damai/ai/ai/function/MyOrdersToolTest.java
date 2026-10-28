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
        assertTrue(tool.listMyOrders(new ToolContext(Map.of("loginProvided", true))).contains("无法验证登录状态"));
    }
    @Test void forwardsTokenAndOnlyReturnsSelectedFields() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/orders", exchange -> {
            String token = exchange.getRequestHeaders().getFirst("token");
            boolean valid = "test.token.valid".equals(token);
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String json = "test.token.unavailable".equals(token) ? "{\"code\":-100}" : valid ?
                    (body.contains("sessionOnly") ? "{\"code\":0,\"data\":[]}" : "{\"code\":0,\"data\":[{\"orderNumber\":\"42\",\"programTitle\":\"Concert\",\"orderStatus\":3,\"userId\":\"private\"}]}") : "{\"code\":516}";
            byte[] result = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, result.length); exchange.getResponseBody().write(result); exchange.close();
        });
        server.start();
        try {
            MyOrdersTool tool = new MyOrdersTool("http://127.0.0.1:" + server.getAddress().getPort() + "/orders");
            assertEquals(MyOrdersTool.Authentication.AUTHENTICATED, tool.authenticate("test.token.valid"));
            assertEquals(MyOrdersTool.Authentication.LOGIN_REQUIRED, tool.authenticate("test.token.expired"));
            assertEquals(MyOrdersTool.Authentication.LOGIN_REQUIRED, tool.authenticate(null));
            assertEquals(MyOrdersTool.Authentication.LOGIN_REQUIRED, tool.authenticate("malformed"));
            assertEquals(MyOrdersTool.Authentication.UNAVAILABLE, tool.authenticate("test.token.unavailable"));
            var callback = ToolCallbacks.from(tool)[0];
            assertFalse(callback.getToolDefinition().inputSchema().contains("damaiToken"));
            String result = callback.call("{}", new ToolContext(Map.of("damaiToken", "test.token.valid")));
            assertTrue(result.contains("Concert"));
            assertTrue(result.contains("已支付"));
            assertFalse(result.contains("private"));
            assertFalse(result.contains("test.token.valid"));
        } finally { server.stop(0); }
    }
}
