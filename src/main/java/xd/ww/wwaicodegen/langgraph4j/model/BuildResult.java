package xd.ww.wwaicodegen.langgraph4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildResult implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 是否构建成功
     */
    private Boolean isValid;
    
    /**
     * 错误列表
     */
    private String errors;

}
