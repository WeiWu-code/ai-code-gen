package xd.ww.wwaicodegen.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

@Configuration
@Data
@ConfigurationProperties("langchain4j.open-ai.reasoning-chat-model")
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;
    private String apiKey;
    private Integer maxTokens;
    private String modelName;
    private Boolean logRequests;
    private Boolean logResponses;
    private Duration timeout;
    private Double temperature;

    /**
     * 推理流式模型 用于 vue 项目生成
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingChatModelPrototype(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .timeout(timeout)
                .build();
    }
}
