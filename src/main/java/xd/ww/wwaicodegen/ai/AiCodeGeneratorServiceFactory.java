package xd.ww.wwaicodegen.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务创建工厂
 * @author wei
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI代码生成器服务
     * 由LangChain4j代理
     * @return AiCodeGeneratorService服务
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
