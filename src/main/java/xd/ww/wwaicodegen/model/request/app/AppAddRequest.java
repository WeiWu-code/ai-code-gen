package xd.ww.wwaicodegen.model.request.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3920576780309733545L;
    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;
    
}
