package org.damai.ai.ai.function;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyOrdersTool {
    private final String orderUrl;

    public MyOrdersTool(@Value("${damai.orders.url:http://127.0.0.1:6090/damai/order/order/select/list}") String orderUrl) {
        this.orderUrl = orderUrl;
    }

    private JSONObject query(String token) {
        String response = HttpRequest.post(orderUrl)
                .header("token", token)
                .header("Content-Type", "application/json")
                .body("{\"userId\":0}")
                .timeout(15000)
                .execute().body();
        return JSON.parseObject(response);
    }

    public boolean isAuthenticated(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            JSONObject result = query(token);
            return result != null && "0".equals(String.valueOf(result.get("code"))) && result.getJSONArray("data") != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Tool(description = "查询当前登录用户自己的真实订单。不要让用户提供用户ID、手机号或令牌；未登录或查询失败时如实告知，不要编造订单。")
    public String listMyOrders(ToolContext context) {
        Object token = context.getContext().get("damaiToken");
        if (!(token instanceof String) || ((String) token).isBlank()) {
            return "尚未登录大麦，请先返回主站登录后再查询订单。";
        }
        try {
            JSONObject result = query((String) token);
            if (result == null || !"0".equals(String.valueOf(result.get("code")))) {
                return "订单查询失败或登录已失效，请重新登录后重试。";
            }
            JSONArray orders = result.getJSONArray("data");
            if (orders == null) {
                return "订单查询返回异常，请稍后重试。";
            }
            List<JSONObject> safeOrders = new ArrayList<>();
            for (int i = 0; i < Math.min(orders.size(), 20); i++) {
                JSONObject order = orders.getJSONObject(i);
                JSONObject safe = new JSONObject(true);
                for (String key : List.of("orderNumber", "programTitle", "programPlace", "programShowTime", "orderPrice", "orderStatus", "ticketCount", "createOrderTime")) {
                    safe.put(key, order.get(key));
                }
                Integer status = order.getInteger("orderStatus");
                safe.put("orderStatusDescription", status == null ? "未知" : switch (status) {
                    case 1 -> "未支付";
                    case 2 -> "已取消";
                    case 3 -> "已支付";
                    case 4 -> "已退单";
                    default -> "未知";
                });
                safeOrders.add(safe);
            }
            return JSON.toJSONString(java.util.Map.of("total", orders.size(), "orders", safeOrders,
                    "notice", orders.size() > 20 ? "仅展示最近20条订单" : "查询结果来自订单系统"));
        } catch (Exception e) {
            return "订单服务暂时不可用，请稍后重试。";
        }
    }
}
