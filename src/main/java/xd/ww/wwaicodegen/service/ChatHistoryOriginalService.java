package xd.ww.wwaicodegen.service;

import java.util.*;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import xd.ww.wwaicodegen.model.entity.ChatHistoryOriginal;

/**
 * 原始对话历史 服务层。
 * 为 vue 工程模式恢复对话记忆(包含工具调用信息)
 *
 * @author wei
 */
public interface ChatHistoryOriginalService extends IService<ChatHistoryOriginal> {

    /**
     * 添加对话历史
     * @param appId 应用Id
     * @param message 信息
     * @param messageType 信息类型
     * @param userId 用户Id
     * @return 是否成功
     */
    boolean addOriginalChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 批量添加对话历史
     * @param chatHistoryOriginalList 对话历史信息
     * @return 是否成功
     */
    boolean addOriginalChatMessageBatch(List<ChatHistoryOriginal> chatHistoryOriginalList);

    /**
     * 根据 appId 关联删除对话历史记录
     * @param appId 应用Id
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 将 APP 的对话历史加载到缓存中
     * @param appId 应用Id
     * @param chatMemory 待加载的对话记忆
     * @param maxCount 最大数量
     * @return 加载的数量
     */
    int loadOriginalChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
