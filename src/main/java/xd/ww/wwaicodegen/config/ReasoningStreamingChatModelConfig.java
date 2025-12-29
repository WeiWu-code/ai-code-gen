package xd.ww.wwaicodegen.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("langchain4j.open-ai.chat-model")
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;
    private String apiKey;
    private Integer maxTokens;
    private String modelName;

    /**
     * 推理流式模型 用于 vue 项目生成
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
