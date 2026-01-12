package xd.ww.wwaicodegen.langgraph4j.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class SseContextHolder {
    private static final ThreadLocal<SseEmitter> EMITTER_HOLDER = new ThreadLocal<>();

    public static void set(SseEmitter emitter) {
        EMITTER_HOLDER.set(emitter);
    }

    public static SseEmitter get() {
        return EMITTER_HOLDER.get();
    }

    public static void clear() {
        EMITTER_HOLDER.remove();
    }
    
    // 快捷发送 "正在执行" 事件的方法
    public static void sendProcessing(String stepName) {
        SseEmitter emitter = get();
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("step_processing")
                        .data(Map.of("stepName", stepName)));
            } catch (Exception e) {
                // 忽略发送失败，避免影响主流程
            }
        }
    }

    /**
     * 发送 SSE 事件的辅助方法
     */
    public static void sendEndSseEvent(int stepCounter, String currentStep) {
        SseEmitter emitter = get();
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("step_completed")
                        .data(Map.of(
                                "stepNumber", stepCounter,
                                "currentStep", currentStep)));
            } catch (IOException e) {
                log.error("发送 SSE 事件失败: {}", e.getMessage(), e);
            }
        }

    }

    /**
     * 发送 SSE 事件的辅助方法
     */
    public static void sendSseEvent(String status, Object data) {
        SseEmitter emitter = get();
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(status)
                        .data(data));
            } catch (IOException e) {
                log.error("发送 SSE 事件失败: {}", e.getMessage(), e);
            }
        }

    }
}