package xd.ww.wwaicodegen.langgraph4j.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.constant.AppConstant;
import xd.ww.wwaicodegen.core.AiCodeGeneratorFacade;
import xd.ww.wwaicodegen.langgraph4j.model.QualityResult;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder.sendEndSseEvent;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);

            // 检查是否有错误信息
            buildUserMessage(context);
            SseContextHolder.sendProcessing("代码生成");
            log.info("执行节点: 代码生成");
            // 使用增强后的提示词
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum codeType = context.getGenerationType();
            // 获取AI门面服务
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {}", codeType.getText());
            // 先使用固定的AppId
            Long appId = 3L;
            Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStream(userMessage, codeType, appId);
            // 等待流输出完成
            codeStream.blockLast(Duration.ofMinutes(10));
            // 设置输出目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,
                    codeType.getValue(), appId);

            sendEndSseEvent(4, "代码生成");
            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }


    /**
     * 构造用户消息，如果存在质检失败结果则添加错误修复信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            // 直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }

}
