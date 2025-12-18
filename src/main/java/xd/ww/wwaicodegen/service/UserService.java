package xd.ww.wwaicodegen.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.model.request.UserQueryRequest;
import xd.ww.wwaicodegen.model.vo.LoginUserVO;
import xd.ww.wwaicodegen.model.vo.UserVO;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author xd 吴玮
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);

    /**
     * 用户信息脱敏包装
     * @param user 原始用户
     * @return 脱敏后的用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request HttpServletRequest
     * @return true
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request 更新Session
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request HttpServletRequest
     * @return 用户信息，未脱敏
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户信息脱敏包装
     * @param user 原始用户
     * @return 脱敏后的用户
     */
    UserVO getUserVO(User user);

    /**
     * 用户信息脱敏包装
     * @param userList 原始用户列表
     * @return 脱敏后的用户
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据userQueryRequest的值，生成QueryWrapper
     * @param userQueryRequest 待查询的request
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
