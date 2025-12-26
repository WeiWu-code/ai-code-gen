package xd.ww.wwaicodegen.service;

import com.mybatisflex.core.service.IService;
import xd.ww.wwaicodegen.model.entity.ChatHistory;

/**
 * 对话历史 服务层。
 *
 * @author xd 吴玮
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加 对话历史信息到表
     * @param appId 当前应用Id
     * @param message 当前用户的提示词
     * @param messageType 消息类型，（AI回复或者用户提问）
     * @param userId 当前用户Id
     * @return 返回保存是否成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);
}
