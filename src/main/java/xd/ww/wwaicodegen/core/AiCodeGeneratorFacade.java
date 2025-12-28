package xd.ww.wwaicodegen.core;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.ai.AiCodeGeneratorService;
import xd.ww.wwaicodegen.ai.AiCodeGeneratorServiceFactory;
import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;
import xd.ww.wwaicodegen.parser.CodeParserExecutor;
import xd.ww.wwaicodegen.saver.CodeFileSaverExecutor;

import java.io.File;

/**
 * AI代码生成门面类，组合AI代码生成和保存
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口，根据不同的AI返回类型保存为文件
     *
     * @param userMessage     用户的提示词
     * @param codeGenTypeEnum 生成的html类型的枚举
     * @param appId 应用Id
     * @return 保存后的文件夹File类
     */
    public File generatorAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId不能为空");
        }

        // 根据appId获取相应的Ai服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateCode(userMessage);
                yield CodeFileSaverExecutor.executorSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiCode(userMessage);
                yield CodeFileSaverExecutor.executorSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的Html类型" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口，根据不同的AI返回类型保存为文件
     * 流式
     *
     * @param userMessage     用户的提示词
     * @param codeGenTypeEnum 生成的html类型的枚举
     * @param appId 应用Id
     * @return 保存后的文件夹File类
     */
    public Flux<String> generatorAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId不能为空");
        }

        // 根据appId获取相应的Ai服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> htmlCodeStream = aiCodeGeneratorService.generateCodeStream(userMessage);
                yield processCodeStream(htmlCodeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> multiCodeStream = aiCodeGeneratorService.generateMultiCodeStream(userMessage);
                yield processCodeStream(multiCodeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的Html类型" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }


    // 通用流式处理方法
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                // 收集code
                String code = codeBuilder.toString();
                // 使用执行器解析代码
                Object code_parser = CodeParserExecutor.executeParser(code, codeGenTypeEnum);
                // 使用执行器保存代码
                File file = CodeFileSaverExecutor.executorSaver(code_parser, codeGenTypeEnum, appId);
                log.info("html文件已保存: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.info("保存失败{}", e.getMessage());
            }
        });
    }
}
