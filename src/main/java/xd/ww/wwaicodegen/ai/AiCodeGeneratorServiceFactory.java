package xd.ww.wwaicodegen.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import xd.ww.wwaicodegen.ai.tool.*;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;
import xd.ww.wwaicodegen.service.ChatHistoryOriginalService;
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
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private ToolManager toolManager;

    @Resource
    private ChatHistoryOriginalService chatHistoryOriginalService;

    /**
     * Ai服务实例缓存
     * 缓存策略：
     * - 最大 1000 个实例
     * - 写入后 30 分钟过期
     * - 响应后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> aiServiceCache = Caffeine.newBuilder()
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
        // 为了兼容之前的逻辑，HTML和MULTY可公用CodeGenTypeEnum.HTML
        return this.getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据AppId创建一个AiCodeGeneratorService
     * 如果缓存中有，则不创建
     * @param appId 应用Id
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeType) {
        String cacheKey = builderKey(appId, codeType);
        return aiServiceCache.get(cacheKey, key -> this.createAiCodeGeneratorService(appId, codeType));
    }

    private String builderKey(long appId, CodeGenTypeEnum codeType) {
        return appId + "_" + codeType.name();
    }

    /**
     * 根据AppId创建一个AiCodeGeneratorService
     * @param appId 应用Id
     * @param codeType 代码类型
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeType) {
        log.info("为 appId = {} 创建一个新的Ai服务实例", appId);
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .id(appId)
                .maxMessages(100)
                .build();

        // 根据代码类型，使用不同的Ai
        // 根据代码类型，加载不同的ChatHistory服务
        return switch (codeType) {
            case VUE_PROJECT -> {
                // 先加载历史消息
                int size = chatHistoryOriginalService.loadOriginalChatHistoryToMemory(appId, memory, 50);
                yield AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemory(memory)
                    .chatMemoryProvider(memoryId -> memory)
                    .tools(toolManager.getAllTools())
                    // 处理工具幻觉问题
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error: there is no tool called " + toolExecutionRequest.name()))
                    .maxSequentialToolsInvocations(15)
                    .build();
            }

            case HTML, MULTI_FILE -> {
                // 先加载历史消息
                int size = chatHistoryService.loadChatHistoryToMemory(appId, memory, 20);
                log.debug("加载 {} 条历史记录", size);
                yield  AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(memory)
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型");
        };
    }
}
