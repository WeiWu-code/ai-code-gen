package xd.ww.wwaicodegen.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xd.ww.wwaicodegen.guardrail.PromptSafetyInputGuardrail;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author wei
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AI代码生成类型路由服务实例
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }

    /**
     * 多例创建
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        // 使用多例模式的StreamingChatModel
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);

        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .inputGuardrails(new PromptSafetyInputGuardrail())
                .build();
    }
}
