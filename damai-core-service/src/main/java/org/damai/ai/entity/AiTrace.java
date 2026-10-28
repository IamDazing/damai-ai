package org.damai.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.damai.ai.entity.base.BaseTableData;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("d_ai_trace")
public class AiTrace extends BaseTableData {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 追踪ID（用于关联同一会话的多次调用）
     */
    private String traceId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 使用的模型名称
     */
    private String modelName;
    
    /**
     * 请求类型: CHAT, RAG, FUNCTION_CALL
     */
    private String requestType;
    
    /**
     * 输入Token数
     */
    private Integer promptTokens;
    
    /**
     * 输出Token数
     */
    private Integer completionTokens;
    
    /**
     * 总Token数
     */
    private Integer totalTokens;
    
    /**
     * 响应延迟（毫秒）
     */
    private Long latencyMs;
    
    /**
     * 预估费用（单位：元）
     */
    private BigDecimal estimatedCost;
    
    /**
     * 用户输入（截断保存）
     */
    private String userInput;
    
    /**
     * AI输出（截断保存）
     */
    private String aiOutput;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 附加元数据（JSON格式）
     */
    private String metadata;
}
