package xd.ww.wwaicodegen.model.request.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用部署请求
 */
@Data
public class AppDeployRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4517736037072836447L;
    /**
     * 应用 id
     */
    private Long appId;

}
