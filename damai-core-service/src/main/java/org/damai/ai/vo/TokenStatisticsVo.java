package org.damai.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsVo {
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 总调用次数
     */
    private int totalCalls;
    
    /**
     * 总输入Token数
     */
    private int totalPromptTokens;
    
    /**
     * 总输出Token数
     */
    private int totalCompletionTokens;
    
    /**
     * 总Token数
     */
    private int totalTokens;
    
    /**
     * 总延迟（毫秒）
     */
    private long totalLatencyMs;
    
    /**
     * 平均延迟（毫秒）
     */
    private long avgLatencyMs;
    
    /**
     * 总费用（元）
     */
    private BigDecimal totalCost;
    
    /**
     * 成功率（百分比）
     */
    private double successRate;
}
