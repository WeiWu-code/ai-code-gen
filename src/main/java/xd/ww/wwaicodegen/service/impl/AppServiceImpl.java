package xd.ww.wwaicodegen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.constant.AppConstant;
import xd.ww.wwaicodegen.core.AiCodeGeneratorFacade;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.exception.ThrowUtils;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;
import xd.ww.wwaicodegen.model.entity.App;
import xd.ww.wwaicodegen.mapper.AppMapper;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.model.request.app.AppQueryRequest;
import xd.ww.wwaicodegen.model.vo.AppVO;
import xd.ww.wwaicodegen.model.vo.UserVO;
import xd.ww.wwaicodegen.service.AppService;
import org.springframework.stereotype.Service;
import xd.ww.wwaicodegen.service.UserService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author xd 吴玮
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    UserService userService;

    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;

    public AppServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AppVO getAppVO(App app) {
        if(app == null){
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app,appVO);
        // 查询用户
        Long userId = app.getUserId();
        if(userId != null){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }

        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));

        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }



    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generatorAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用Id错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，只有本人可以部署自己的应用
        if(!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已经有deployKey，没有则生成
        String deployKey = app.getDeployKey();
        if(StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取原始代码路径，类型
        String codeType = app.getCodeGenType();
        String dirName = codeType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + dirName;
        // 6. 检查路径
        File sourceDir = new File(sourceDirPath);
        if(!sourceDir.exists() || !sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码路径不存在");
        }
        // 7. 更新数据库
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败");
        }
        // 8. 部署
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "应用部署更新失败");
        // 9. 返回Url
        return StrUtil.format("{}/{}", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

}
