package xd.ww.wwaicodegen.langgraph4j;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.ai.model.AiResponseMessage;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.langgraph4j.model.BuildResult;
import xd.ww.wwaicodegen.langgraph4j.node.*;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.util.Map;
import java.util.function.Consumer;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Slf4j
public class CodeGenWorkflow {

    /**
     * 创建完整的工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // 添加节点 - 使用完整实现的节点
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())

                    // 添加边
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addConditionalEdges("code_generator",
                            edge_async(this::routeBuildOrSkip),
                            Map.of(
                                    "build", "project_builder",
                                    "skip_build", END
                            )
                    )
                    .addConditionalEdges("project_builder",
                            edge_async(this::routeAfterBuilder),
                            Map.of(
                                    "success", END,            // 构建成功
                                    "fail", "code_generator"      // 构建失败，重新生成
                            ))
                    // 编译工作流
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    private String routeAfterBuilder(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        BuildResult buildResult = context.getBuildResult();
        if (buildResult == null) {
            return "success";
        }
        if(buildResult.getIsValid()){
            return "success";
        }
        return "fail";
    }


    /**
     * 执行工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化 WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("代码生成工作流执行完成！");
        return finalContext;
    }

    private String routeBuildOrSkip(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenTypeEnum generationType = context.getGenerationType();
        // HTML 和 MULTI_FILE 类型不需要构建，直接结束
        if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
            return "skip_build";
        }
        // VUE_PROJECT 需要构建
        return "build";
    }


    /**
     * 执行工作流（Flux 原生流式输出版本）
     * 直接输出 String，节点内部可通过 context.emit() 发送
     */
    public Flux<String> executeWorkflowWithFlux(String originalPrompt, Long appId) {
        return Flux.create(sink -> {
            Thread.ofVirtual().start(() -> {
                try {
                    // 1. 定义发射器：直接把字符串推给 sink
                    Consumer<String> emitter = sink::next;

                    SseContextHolder.setEmitter(emitter);
                    CompiledGraph<MessagesState<String>> workflow = createWorkflow();

                    // 2. 初始化 Context，注入 emitter
                    WorkflowContext initialContext = WorkflowContext.builder()
                            .appId(appId)
                            .originalPrompt(originalPrompt)
                            .currentStep("初始化")
                            .build();

                    // 发送开始消息
                    AiResponseMessage initMessage = new AiResponseMessage("工作流初始化\n");
                    SseContextHolder.emit(JSONUtil.toJsonStr(initMessage));

                    GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                    log.info("工作流图: \n{}", graph.content());

                    // 3. 执行循环
                    for (NodeOutput<MessagesState<String>> step : workflow.stream(
                            Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {

                        if (START.equals(step.node()) || END.equals(step.node())) {
                            continue;
                        }

                        // 获取当前步骤的 State (Context)
                        WorkflowContext currentContext = WorkflowContext.getContext(step.state());

                    }

                    AiResponseMessage endMessage = new AiResponseMessage("工作流执行完成\n");
                    SseContextHolder.emit(JSONUtil.toJsonStr(endMessage));
                    sink.complete(); // 结束流

                } catch (Exception e) {
                    log.error("工作流执行失败", e);
                    sink.error(e);
                } finally {
                    // 清理ThreadLocal
                    SseContextHolder.clear();
                }
            });
        });
    }

}
