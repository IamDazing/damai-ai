package org.damai.ai.vo.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.damai.ai.vo.TicketCategoryDetailVo;
import org.damai.ai.vo.result.base.ApiResponse;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TicketCategoryListResultVo extends ApiResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<TicketCategoryDetailVo> data;
}
