package xd.ww.wwaicodegen.langgraph4j.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.constant.AppConstant;
import xd.ww.wwaicodegen.core.AiCodeGeneratorFacade;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");
            // 使用增强后的提示词
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum codeType = context.getGenerationType();
            // 获取AI门面服务
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {}", codeType.getText());
            // 先使用固定的AppId
            Long appId = 0L;
            Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStream(userMessage, codeType, appId);
            // 等待流输出完成
            codeStream.blockLast(Duration.ofMinutes(10));
            // 设置输出目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,
                    codeType.getValue(), appId);
            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
