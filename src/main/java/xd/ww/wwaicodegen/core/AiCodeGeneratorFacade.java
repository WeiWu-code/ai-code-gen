package xd.ww.wwaicodegen.core;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import xd.ww.wwaicodegen.ai.AiCodeGeneratorService;
import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

/**
 * AI代码生成门面类，组合AI代码生成和保存
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口，根据不同的AI返回类型保存为文件
     *
     * @param userMessage     用户的提示词
     * @param codeGenTypeEnum 生成的html类型的枚举
     * @return 保存后的文件夹File类
     */
    public File generatorAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
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
     * @return 保存后的文件夹File类
     */
    public Flux<String> generatorAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的Html类型" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成 HTML 模式的代码并保存
     * 流式
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateCodeStream(userMessage);
        // 字符串拼接器，用于流式返回后拼接成内容
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return result.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                // 保存
                String htmlCode = codeBuilder.toString();
                // 解析AI回复的字符串
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(htmlCode);
                // 保存代码到本地
                File file = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("html文件已保存: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.info("保存失败{}", e.getMessage());
            }
        });
    }

    /**
     * 生成多文件模式的代码并保存
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiCode(userMessage);
        return CodeFileSaver.saveMultiHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     * 流式
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiCodeStream(userMessage);
        // 字符串拼接器，用于流式返回后拼接成内容
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return result.doOnNext(chunk->{
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                // 保存
                String htmlCode = codeBuilder.toString();
                // 解析AI回复的字符串
                MultiFileCodeResult htmlCodeResult = CodeParser.parseMultiFileCode(htmlCode);
                // 保存代码到本地
                File file = CodeFileSaver.saveMultiHtmlCodeResult(htmlCodeResult);
                log.info("html文件已保存: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.info("保存失败{}", e.getMessage());
            }
        });
    }

}
