package xd.ww.wwaicodegen.langgraph4j.node;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import xd.ww.wwaicodegen.core.builder.VueProjectBuilder;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.langgraph4j.model.BuildResult;
import xd.ww.wwaicodegen.langgraph4j.model.NodeResponseMessage;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            CodeGenTypeEnum codeType = context.getGenerationType();
            if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                NodeResponseMessage startMessage = new NodeResponseMessage("{项目构建}", "开始");
                SseContextHolder.emit(JSONUtil.toJsonStr(startMessage));
            }else{
                SseContextHolder.emit("\n\n开始{项目构建}\n\n");
            }

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
                    BuildResult buildResult = BuildResult.builder()
                            .isValid(true)
                            .errors(outLog.toString())
                            .build();
                    context.setBuildResult(buildResult);
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目构建失败");
                }

                if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                    NodeResponseMessage endMessage = new NodeResponseMessage("{项目构建}", "完成");
                    SseContextHolder.emit(JSONUtil.toJsonStr(endMessage));
                }else{
                    SseContextHolder.emit("\n\n完成{项目构建}\n\n");
                }
            } catch (Exception e) {
                log.error("项目构建异常：{}", outLog);
                BuildResult buildResult = BuildResult.builder()
                        .isValid(false)
                        .errors(outLog.toString())
                        .build();
                context.setBuildResult(buildResult);
                if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                    NodeResponseMessage endMessage = new NodeResponseMessage("{项目构建}，" + outLog, "失败");
                    SseContextHolder.emit(JSONUtil.toJsonStr(endMessage));
                }else{
                    SseContextHolder.emit("\n\n失败{项目构建}，" + outLog + "\n\n");
                }
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
