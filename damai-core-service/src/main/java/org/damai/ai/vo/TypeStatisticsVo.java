package org.damai.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeStatisticsVo {
    
    /**
     * 请求类型
     */
    private String requestType;
    
    /**
     * 调用次数
     */
    private int calls;
    
    /**
     * Token总数
     */
    private int totalTokens;
    
    /**
     * 预估费用
     */
    private BigDecimal totalCost;
}
