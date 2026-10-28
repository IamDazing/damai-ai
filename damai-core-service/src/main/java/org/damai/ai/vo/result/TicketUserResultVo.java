package org.damai.ai.vo.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.damai.ai.vo.TicketUserVo;
import org.damai.ai.vo.result.base.ApiResponse;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TicketUserResultVo extends ApiResponse implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private List<TicketUserVo> data;
}
