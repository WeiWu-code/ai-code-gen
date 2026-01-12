package xd.ww.wwaicodegen.langgraph4j.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import xd.ww.wwaicodegen.core.builder.VueProjectBuilder;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder.sendEndSseEvent;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");
            SseContextHolder.sendProcessing("项目构建");
            // 获取必要的参数
            String buildCodeDir;
            String generatorCodeDir = context.getGeneratedCodeDir();
            StringBuilder outLog = new StringBuilder();
            try {
                VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                // 执行构建
                boolean buildSuccess = vueProjectBuilder.buildProject(generatorCodeDir, outLog);
                if (buildSuccess) {
                    buildCodeDir = generatorCodeDir + File.separator + "dist";
                    log.info("构建成功, 构建目录{}", buildCodeDir);
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目构建失败");
                }
                sendEndSseEvent(6, "项目构建");
            } catch (Exception e) {
                log.error("项目构建异常：{}", outLog);
                // 异常时返回原路径
                buildCodeDir = generatorCodeDir;
            }

            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildCodeDir);
            log.info("项目构建完成，结果目录: {}", buildCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
