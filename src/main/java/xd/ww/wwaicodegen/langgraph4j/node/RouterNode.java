package xd.ww.wwaicodegen.langgraph4j.node;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import xd.ww.wwaicodegen.ai.AiCodeGenTypeRoutingService;
import xd.ww.wwaicodegen.ai.AiCodeGenTypeRoutingServiceFactory;
import xd.ww.wwaicodegen.langgraph4j.model.NodeResponseMessage;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");
            NodeResponseMessage startMessage = new NodeResponseMessage("智能路由选择", "start");
            SseContextHolder.emit(JSONUtil.toJsonStr(startMessage));

            String originalPrompt = context.getOriginalPrompt();
            CodeGenTypeEnum generationType;
            try{
                // 获取bean
                AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory = SpringContextUtil.getBean(AiCodeGenTypeRoutingServiceFactory.class);
                AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
                // 根据原始提示词去判断路由
                generationType = routingService.routeCodeGenType(originalPrompt);
                NodeResponseMessage endMessage = new NodeResponseMessage("智能路由选择", "end");
                SseContextHolder.emit(JSONUtil.toJsonStr(endMessage));
            }catch (Exception e){
                log.error("AI路由失败，默认使用VUE");
                generationType = CodeGenTypeEnum.VUE_PROJECT;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}
