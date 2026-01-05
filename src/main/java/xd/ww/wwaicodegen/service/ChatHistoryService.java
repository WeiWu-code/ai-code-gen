package xd.ww.wwaicodegen.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import xd.ww.wwaicodegen.model.entity.ChatHistory;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.model.request.chathistory.ChatHistoryQueryRequest;

import java.time.LocalDateTime;

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

    /**
     * 根据AppId，删除所有的对话历史
     * @param appId 应用Id
     * @return 删除成功与否
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 对话历史请求类
     * @return QueryWrapper包装类
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据游标（lastCreateTime）和 appId，查询出一页的历史对话信息
     * @param appId 应用Id
     * @param pageSize 页大小
     * @param lastCreateTime 游标，只会查询比这个小的
     * @param loginUser 当前登录用户
     * @return 返回 Page页对象
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 从持久化数据库存储中加载对话历史到内存（Redis缓存）
     * @param appId 应用Id
     * @param messageWindowChatMemory 待加载的内存对话记忆
     * @param maxCount 最多加载多少条
     * @return 加载的数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory messageWindowChatMemory, int maxCount);
}
