package xd.ww.wwaicodegen.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import xd.ww.wwaicodegen.service.ChatHistoryService;

import java.time.Duration;

/**
 * AI服务创建工厂
 * @author wei
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private ChatModel chatModel;

    @Resource
    RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    ChatHistoryService chatHistoryService;

    /**
     * Ai服务实例缓存
     * 缓存策略：
     * - 最大 1000 个实例
     * - 写入后 30 分钟过期
     * - 响应后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> aiServiceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener(((key, value, cause) -> log.debug("Ai 服务器实例 {} 被移除，原因： {}",key,cause)))
            .build();

    /**
     * 根据AppId创建一个AiCodeGeneratorService
     * 如果缓存中有，则不创建
     * @param appId 应用Id
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return aiServiceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 根据AppId创建一个AiCodeGeneratorService
     * @param appId 应用Id
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId = {} 创建一个新的Ai服务实例", appId);
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(20)
                .build();

        // 先加载历史消息
        int size = chatHistoryService.loadChatHistoryToMemory(appId, memory, 20);
        log.debug("加载 {} 条历史记录", size);

        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(memory)
                .build();
    }

}
