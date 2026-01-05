package xd.ww.wwaicodegen.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = -6911330388894127661L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
