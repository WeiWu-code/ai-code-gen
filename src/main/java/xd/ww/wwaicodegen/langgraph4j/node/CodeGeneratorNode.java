package xd.ww.wwaicodegen.langgraph4j.node;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.constant.AppConstant;
import xd.ww.wwaicodegen.core.AiCodeGeneratorFacade;
import xd.ww.wwaicodegen.langgraph4j.model.BuildResult;
import xd.ww.wwaicodegen.langgraph4j.model.NodeResponseMessage;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.time.Duration;
import java.util.function.Consumer;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);

            // 检查是否有错误信息
            context.setEnhancedPrompt(buildUserMessage(context));
            CodeGenTypeEnum codeType = context.getGenerationType();

            if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                NodeResponseMessage startMessage = new NodeResponseMessage("{代码生成}", "开始");
                SseContextHolder.emit(JSONUtil.toJsonStr(startMessage));
            }else{
                SseContextHolder.emit("\n\n开始{代码生成}\n\n");
            }

            log.info("执行节点: 代码生成");
            // 使用增强后的提示词
            String userMessage = context.getEnhancedPrompt();
            // 获取AI门面服务
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {}", codeType.getText());
            // 先使用固定的AppId
            Long appId = context.getAppId();
            Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStream(userMessage, codeType, appId);
            // 同步发送给前端
            final Consumer<String> directEmitter = SseContextHolder.getCurrentEmitter();
            codeStream = codeStream.doOnNext(chunk -> {
                // （此时是 IO 线程），直接使用上面捕获的对象
                if (directEmitter != null) {
                    directEmitter.accept(chunk);
                } else {
                    // 理论上不会进这里
                    log.warn("发送器丢失: {}", chunk);
                }
            });

            // 等待流输出完成
            codeStream.blockLast(Duration.ofMinutes(10));
            // 设置输出目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,
                    codeType.getValue(), appId);

            if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                NodeResponseMessage endMessage = new NodeResponseMessage("{代码生成}", "结束");
                SseContextHolder.emit(JSONUtil.toJsonStr(endMessage));
            }else{
                SseContextHolder.emit("\n\n结束{代码生成}\n\n");
            }

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }


    /**
     * 构造用户消息，如果存在构建失败则添加错误修复信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        BuildResult buildResult = context.getBuildResult();
        if (isQualityCheckFailed(buildResult)) {
            // 直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(buildResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     */
    private static boolean isQualityCheckFailed(BuildResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(BuildResult qualityResult) {
        return "\n\n## 上次生成的代码存在以下问题，请修复：\n" +
                "\n 我将提供给你npm install 和 npm run build的日志： \n" +
                qualityResult.getErrors() +
                "\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。";
    }

}
