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
 * 文件删除工具
 * 支持 AI 通过工具调用的方式删除文件
 * 注意避免删除目录外的文件
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool{

    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 将..打平
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName).toRealPath();
                // 打平，防止有..
                Path judgePath= projectRoot.resolve(relativeFilePath).normalize().toRealPath();

                // 再次确认仍在沙箱内
                if (!judgePath.startsWith(projectRoot)) {
                    return "错误：路径越界，无法删除 - " + relativeFilePath;
                }
                if (!Files.exists(judgePath)) {
                    return "警告：文件不存在，无需删除 - " + relativeFilePath;
                }
                if (!Files.isRegularFile(judgePath)) {
                    return "错误：指定路径不是文件，无法删除 - " + relativeFilePath;
                }
                if (Files.isSymbolicLink(path)) {
                    return "错误：不允许删除符号链接 - " + relativeFilePath;
                }
                // judgePath是修正后的真实路径
                // 安全检查：避免删除重要文件
                String fileName = judgePath.getFileName().toString();
                if (isImportantFile(fileName)) {
                    return "错误：不允许删除重要文件 - " + fileName;
                }
                Files.delete(judgePath);
                log.info("成功删除文件: {}", judgePath.toAbsolutePath());
                return "文件删除成功: " + relativeFilePath;
            }else{
                return "错误：路径不合法，应该使用相对路径，不允许绝对路径 - " + relativeFilePath;
            }
        } catch (IOException e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要文件，不允许删除
     */
    private boolean isImportantFile(String fileName) {
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for (String important : importantFiles) {
            if (important.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "删除文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", getDisplayName(), relativePath);
    }
}
