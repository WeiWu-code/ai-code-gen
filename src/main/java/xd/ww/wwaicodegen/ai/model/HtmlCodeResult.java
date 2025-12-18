package xd.ww.wwaicodegen.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 单个html文档的AI返回类
 * @author wei
 */
@Data
@Description("生成html代码文件的结果")
public class HtmlCodeResult {

    @Description("html代码结果")
    private String htmlCode;

    @Description("对生成的html代码的描述")
    private String description;
}
