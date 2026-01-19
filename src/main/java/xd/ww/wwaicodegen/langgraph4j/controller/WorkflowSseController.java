package xd.ww.wwaicodegen.langgraph4j.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.langgraph4j.CodeGenWorkflow;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;

/**
 * 工作流 SSE 控制器
 * 演示 LangGraph4j 工作流的流式输出功能
 */
@RestController
@RequestMapping("/workflow")
@Slf4j
public class WorkflowSseController {

    /**
     * 同步执行工作流
     */
    @PostMapping("/execute")
    public WorkflowContext executeWorkflow(@RequestParam String prompt) {
        log.info("收到同步工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflow(prompt);
    }

    /**
     * SSE 流式执行工作流
     */
    @GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeWorkflowWithSse(@RequestParam String prompt, @RequestParam Long appId) {
        log.info("收到 Flux 工作流执行请求: {}", prompt);
        return new CodeGenWorkflow().executeWorkflowWithFlux(prompt, appId);
    }
}
