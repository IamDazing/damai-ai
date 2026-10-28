package org.damai.ai.vo.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.damai.ai.vo.ProgramDetailVo;
import org.damai.ai.vo.result.base.ApiResponse;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProgramDetailResultVo extends ApiResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ProgramDetailVo data;
}
