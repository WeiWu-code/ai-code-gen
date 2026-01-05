package xd.ww.wwaicodegen.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文档的html(包含css,js)分离文件AI结构化输出类
 * @author wei
 */
@Data
@Description("生成分离式多文档html的结构")
public class MultiFileCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("css代码")
    private String cssCode;

    @Description("js代码")
    private String jsCode;

    @Description("对生成的代码的描述")
    private String description;
}
