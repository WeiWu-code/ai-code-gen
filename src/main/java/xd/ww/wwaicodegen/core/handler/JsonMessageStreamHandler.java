package xd.ww.wwaicodegen.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.ai.model.*;
import xd.ww.wwaicodegen.ai.tool.ToolManager;
import xd.ww.wwaicodegen.constant.AppConstant;
import xd.ww.wwaicodegen.core.builder.VueProjectBuilder;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.exception.ThrowUtils;
import xd.ww.wwaicodegen.langgraph4j.model.NodeResponseMessage;
import xd.ww.wwaicodegen.model.emums.ChatHistoryMessageTypeEnum;
import xd.ww.wwaicodegen.model.entity.ChatHistoryOriginal;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.service.ChatHistoryOriginalService;
import xd.ww.wwaicodegen.service.ChatHistoryService;
import xd.ww.wwaicodegen.ai.tool.BaseTool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux                 原始流
     * @param chatHistoryService         聊天历史服务
     * @param chatHistoryOriginalService 原始对话历史，记录了工具调用等额外信息
     * @param appId                      应用ID
     * @param loginUser                  登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               ChatHistoryOriginalService chatHistoryOriginalService, long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 收集用于对话记忆恢复的数据
        StringBuilder aiOriginResponseStringBuilder = new StringBuilder();
        // 每个Flux流可能包括多个工具调用和AI_RESPONSE相应信息，统一收集后批量入库
        List<ChatHistoryOriginal> originalList = new ArrayList<>();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder,aiOriginResponseStringBuilder,originalList ,seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                .doOnComplete(() -> {
                    // 工具调用信息入库
                    if(!originalList.isEmpty()){
                        // 完善ChatHistory信息
                        originalList.forEach(chatHistory -> {
                            chatHistory.setAppId(appId);
                            chatHistory.setUserId(loginUser.getId());
                        });
                        // 批量入库
                        chatHistoryOriginalService.addOriginalChatMessageBatch(originalList);
                    }
                    // AI 回复入库，1. 未调用工具的回复，2. 工具调用后的输出
                    String aiResponse = aiOriginResponseStringBuilder.toString();
                    chatHistoryOriginalService.addOriginalChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());

                    // 流式响应完成后，添加 AI 消息到对话历史
                    aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());

                    // 异步构建vue项目
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    chatHistoryOriginalService.addOriginalChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk,
                                          StringBuilder chatHistoryStringBuilder,
                                          StringBuilder aiOriginResponseStringBuilder,
                                          List<ChatHistoryOriginal> originalList,
                                          Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.SYSTEM_ERROR, "typeEnum is null");
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                // 与展示数据逻辑相同
                aiOriginResponseStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                // 检查是否是第一次看到这个工具 ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 第一次调用这个工具，记录 ID 并返回工具信息
                    seenToolIds.add(toolId);
                    // 根据工具名称获取工具实例
                    BaseTool tool = toolManager.getTool(toolName);
                    // 返回格式化的工具调用信息
                    return tool.generateToolRequestResponse();
                } else {
                    // 不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                // 处理AI记忆信息
                processToolExecutionMessage(aiOriginResponseStringBuilder, chunk, originalList);
                // 处理展示信息
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                String toolName = toolExecutedMessage.getName();
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                // 根据工具名称获取工具实例并生成相应的结果格式
                BaseTool tool = toolManager.getTool(toolName);
                String result = tool.generateToolExecutedResult(jsonObject);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            case NODE_RESPONSE -> {
                // 持久化，传给前端
                NodeResponseMessage message = JSONUtil.toBean(chunk, NodeResponseMessage.class);
                String data = String.format("\n\n%s %s\n\n", message.getState(), message.getName());
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                // 与展示数据逻辑相同
                aiOriginResponseStringBuilder.append(data);
                return data;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }

    /**
     * 解析处理工具调用相关信息
     * @param aiOriginResponseStringBuilder AI拼接的StringBuilder
     * @param chunk 当前块
     * @param originalList 收集的信息的列表
     */
    private void processToolExecutionMessage(StringBuilder aiOriginResponseStringBuilder, String chunk, List<ChatHistoryOriginal> originalList) {
        // 解析chunk
        ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
        // 构造工具调用请求对象
        String aiResponse = aiOriginResponseStringBuilder.toString();
        ToolRequestMessage toolRequestMessage = new ToolRequestMessage();
        toolRequestMessage.setId(toolExecutedMessage.getId());
        toolRequestMessage.setName(toolExecutedMessage.getName());
        toolRequestMessage.setArguments(toolExecutedMessage.getArguments());
        toolRequestMessage.setText(aiResponse);
        // 转换成Json
        String toolRequestStr = JSONUtil.toJsonStr(toolRequestMessage);
        // 构造ChatHistoryOrigin
        ChatHistoryOriginal requestOrigin = ChatHistoryOriginal.builder()
                .message(toolRequestStr)
                .messageType(ChatHistoryMessageTypeEnum.TOOL_EXECUTION_REQUEST.getValue())
                .build();
        originalList.add(requestOrigin);
        // 构造结果
        ChatHistoryOriginal resultOrigin = ChatHistoryOriginal.builder()
                .message(chunk)
                .messageType(ChatHistoryMessageTypeEnum.TOOL_EXECUTION_RESULT.getValue())
                .build();
        originalList.add(resultOrigin);

        // AI 响应内容暂时结束，置空 aiResponseStringBuilder
        aiOriginResponseStringBuilder.setLength(0);
    }
}
