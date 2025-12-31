package xd.ww.wwaicodegen.core.handler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;
import xd.ww.wwaicodegen.model.entity.User;
import xd.ww.wwaicodegen.service.ChatHistoryService;

/**
 * 统一的流式处理类执行器
 * 按照流的类型进行处理
 * - 如果是传统的 Flux<String> 流，则调用 SimpleTextStreamHandler 处理
 * - 如果是 TokenStream 流，则调用 JsonMessageStreamHandler 处理
 * @author wei
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 处理器执行类，处理Flux流，并保存对话历史
     * @param originFlux Flux<String>流，原始的或者TokenStream转化的
     * @param chatHistoryService 对话历史服务，由调用者传输
     * @param appId 应用 Id
     * @param user 登录用户
     * @param codeType 代码类型
     * @return 返回处理后的Flux<String>
     */
    public Flux<String> doExecutor(Flux<String> originFlux,
                                   ChatHistoryService chatHistoryService,
                                   long appId, User user, CodeGenTypeEnum codeType) {
        return switch (codeType){
            case HTML, MULTI_FILE -> // 不需要注入
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, user);
            case VUE_PROJECT -> // 使用注入器
                    jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, user);
        };
    }
}
