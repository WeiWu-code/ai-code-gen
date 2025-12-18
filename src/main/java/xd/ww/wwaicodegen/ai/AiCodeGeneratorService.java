package xd.ww.wwaicodegen.ai;

import dev.langchain4j.service.SystemMessage;
import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;

public interface AiCodeGeneratorService {

    /**
     * 生成单文件html代码
     * @param userMessage 用户的输入
     * @return AI返回的结果
     */
    @SystemMessage(value = "prompt/code-gen-html-system-prompt.txt")
    HtmlCodeResult generateCode(String userMessage);

    /**
     * 生成多文件html代码
     * @param userMessage 用户的输入
     * @return AI返回的结果
     */
    @SystemMessage(value = "prompt/code-gen-multi-html-system-prompt.txt")
    MultiFileCodeResult generateMultiCode(String userMessage);
}
