package xd.ww.wwaicodegen.core.handler;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.exception.ThrowUtils;
import xd.ww.wwaicodegen.model.emums.ChatHistoryMessageTypeEnum;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.service.ChatHistoryService;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux.map(chunk -> {
            // 收集AI响应内容
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(()->{
            // 响应完成后，添加AI消息到对话历史
            String aiResponse = aiResponseBuilder.toString();
            if(StrUtil.isNotBlank(aiResponse)){
                boolean res = chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "addChatMessage失败");
            }
        }).doOnError(error->{
            // 记录错误信息
            String errorMessage = "AI 回复失败: " + error.getMessage();
            boolean res = chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
            ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "addChatMessage失败");
        });
    }
}
