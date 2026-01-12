package xd.ww.wwaicodegen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class OpenAiStreamChatModelConfig {
    @Bean("openAiStreamingChatModelTaskExecutor")
    AsyncTaskExecutor openAiStreamingChatModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("my-LangChain4j-OpenAI-");
        taskExecutor.setCorePoolSize(6);
        return taskExecutor;
    }
}