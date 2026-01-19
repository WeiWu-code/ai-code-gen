package xd.ww.wwaicodegen.langgraph4j.util;

import java.util.function.Consumer;

public class SseContextHolder {
    // 专门用来存放 String 发送器
    private static final ThreadLocal<Consumer<String>> EMITTER = new ThreadLocal<>();

    public static void setEmitter(Consumer<String> emitter) {
        EMITTER.set(emitter);
    }

    public static void emit(String content) {
        Consumer<String> emitter = EMITTER.get();
        if (emitter != null) {
            emitter.accept(content);
        }
    }

    public static void clear() {
        EMITTER.remove();
    }
}