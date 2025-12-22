package xd.ww.wwaicodegen.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import xd.ww.wwaicodegen.model.entity.App;
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
}
