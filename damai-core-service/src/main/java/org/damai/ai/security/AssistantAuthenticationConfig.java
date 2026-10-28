package org.damai.ai.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.damai.ai.ai.function.MyOrdersTool;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AssistantAuthenticationConfig implements WebMvcConfigurer {
    private final MyOrdersTool orders;

    public AssistantAuthenticationConfig(MyOrdersTool orders) { this.orders = orders; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String token = null;
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("Admin-Token".equals(cookie.getName())) { token = cookie.getValue(); break; }
                    }
                }
                MyOrdersTool.Authentication state = orders.authenticate(token);
                if (state == MyOrdersTool.Authentication.AUTHENTICATED) return true;
                response.sendError(state == MyOrdersTool.Authentication.LOGIN_REQUIRED ? 401 : 503);
                return false;
            }
        }).addPathPatterns("/session", "/program/**", "/chat/**", "/simple/**", "/ai/enhance/**");
    }
}
