package org.damai.ai.security;

import jakarta.servlet.http.Cookie;
import org.damai.ai.ai.function.MyOrdersTool;
import org.damai.ai.cotroller.SessionController;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssistantAuthenticationTest {
    static class Registry extends InterceptorRegistry {
        HandlerInterceptor interceptor() { return (HandlerInterceptor) getInterceptors().get(0); }
    }
    @Test void checksLoginAndDistinguishesServiceFailures() throws Exception {
        MyOrdersTool tool = mock(MyOrdersTool.class);
        when(tool.authenticate(null)).thenReturn(MyOrdersTool.Authentication.LOGIN_REQUIRED);
        when(tool.authenticate("valid")).thenReturn(MyOrdersTool.Authentication.AUTHENTICATED);
        when(tool.authenticate("unavailable")).thenReturn(MyOrdersTool.Authentication.UNAVAILABLE);
        Registry registry = new Registry();
        new AssistantAuthenticationConfig(tool).addInterceptors(registry);
        var mvc = MockMvcBuilders.standaloneSetup(new SessionController()).addInterceptors(registry.interceptor()).build();
        mvc.perform(get("/session")).andExpect(status().isUnauthorized());
        mvc.perform(get("/session").cookie(new Cookie("Admin-Token", "valid"))).andExpect(status().isOk());
        mvc.perform(get("/session").cookie(new Cookie("Admin-Token", "unavailable"))).andExpect(status().isServiceUnavailable());
    }
}
