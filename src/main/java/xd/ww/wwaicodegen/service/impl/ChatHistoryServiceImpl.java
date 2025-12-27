package xd.ww.wwaicodegen.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import xd.ww.wwaicodegen.constant.UserConstant;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.exception.ThrowUtils;
import xd.ww.wwaicodegen.model.emums.ChatHistoryMessageTypeEnum;
import xd.ww.wwaicodegen.model.entity.App;
import xd.ww.wwaicodegen.model.entity.ChatHistory;
import xd.ww.wwaicodegen.mapper.ChatHistoryMapper;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.model.request.chathistory.ChatHistoryQueryRequest;
import xd.ww.wwaicodegen.service.AppService;
import xd.ww.wwaicodegen.service.ChatHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author xd 吴玮
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用Id不合法");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型为空");
        ThrowUtils.throwIf(userId == null || userId < 0, ErrorCode.PARAMS_ERROR, "用户Id不合法");

        // 校验messageType
        ChatHistoryMessageTypeEnum chatHistoryMessageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(chatHistoryMessageTypeEnum == null, ErrorCode.PARAMS_ERROR,"不支持的消息类型" + messageType);

        // 构建存储对象
        ChatHistory chatHistory = ChatHistory.builder()
                .message(message)
                .appId(appId)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public boolean removeById(ChatHistory entity) {
        return super.removeById(entity);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 对话历史请求类
     * @return QueryWrapper包装类
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }


    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据，返回第一页
        return this.page(Page.of(1, pageSize), queryWrapper);
    }


}
