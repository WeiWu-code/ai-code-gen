package xd.ww.wwaicodegen.ai.tool;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xd.ww.wwaicodegen.constant.AppConstant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具
 * 支持 AI 通过工具调用的方式读取文件内容
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool {

    @Tool("读取指定路径的文件内容")
    public String readFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                // 打平，防止有..
                path = projectRoot.resolve(relativeFilePath).normalize();
                // 判断是否在沙盒内部
                if(!path.startsWith(projectRoot)){
                    return "错误：路径越界 - " + relativeFilePath;
                }

                if (!Files.exists(path) || !Files.isRegularFile(path)) {
                    return "错误：文件不存在或不是文件 - " + relativeFilePath;
                }

                String content = Files.readString(path);
                // 修复：如果内容为空或只包含空白字符，返回特定提示
                if (content.trim().isEmpty()) {
                    return "（该文件为空，没有内容）";
                }
                return content;

            }else{
                return "错误：路径不合法，应该使用相对路径，不允许绝对路径 - " + relativeFilePath;
            }
        } catch (IOException e) {
            String errorMessage = "读取文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFile";
    }

    @Override
    public String getDisplayName() {
        return "读取文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
