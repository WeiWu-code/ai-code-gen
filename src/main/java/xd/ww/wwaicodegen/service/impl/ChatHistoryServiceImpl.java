package xd.ww.wwaicodegen.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.exception.ThrowUtils;
import xd.ww.wwaicodegen.model.emums.ChatHistoryMessageTypeEnum;
import xd.ww.wwaicodegen.model.entity.ChatHistory;
import xd.ww.wwaicodegen.mapper.ChatHistoryMapper;
import xd.ww.wwaicodegen.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author xd 吴玮
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

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
}
