package xd.ww.wwaicodegen.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.model.entity.App;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.model.request.app.AppAddRequest;
import xd.ww.wwaicodegen.model.request.app.AppQueryRequest;
import xd.ww.wwaicodegen.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author xd 吴玮
 */
public interface AppService extends IService<App> {

    /**
     * 获取APP封装类
     * @param app 待封装的APP
     * @return 返回封装后的APP
     */
    AppVO getAppVO(App app);

    /**
     * 根据appList，返回整个appList的Vo封装列表
     * @param appList 待封装的app
     * @return 封装后的app列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造App的QueryWrapper
     * @param appQueryRequest app请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 应用聊天生成代码
     * @param appId 应用Id
     * @param message 用户提示词
     * @param loginUser 登录用户
     * @param isGraph 是否使用工作流模式
     * @return 流式返回
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser, boolean isGraph);


    /**
     * 部署应用
     * @param appId 应用ID
     * @param loginUser 当前登录用户
     * @return 部署后的网址
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 创建应用，利用AI只能选择
     * @param appAddRequest 创建应用的请求
     * @param loginUser 当前登录用户
     * @return 创建成功的应用Id
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);
}
