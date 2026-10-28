package org.damai.ai.vo.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.damai.ai.vo.UserDetailVo;
import org.damai.ai.vo.result.base.ApiResponse;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDetailResultVo extends ApiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private UserDetailVo data;

}
