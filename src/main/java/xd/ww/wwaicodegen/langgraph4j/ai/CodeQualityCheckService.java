package xd.ww.wwaicodegen.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import xd.ww.wwaicodegen.langgraph4j.model.QualityResult;

public interface CodeQualityCheckService {

    /**
     * 检查代码质量
     * AI 会分析代码并返回质量检查结果
     */
    @UserMessage("{{userMessage}}")
    @SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(@V("userMessage") String userMessage);
}
