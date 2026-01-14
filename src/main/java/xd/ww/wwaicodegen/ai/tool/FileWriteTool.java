package xd.ww.wwaicodegen.ai.tool;

import cn.hutool.core.io.FileUtil;
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
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
public class FileWriteTool extends BaseTool{

    @Tool("写入文件到指定路径")
    public String writeFile(@P("待写入文件的相对路径") String relativePath,
                            @P("待写入文件的文件内容") String content,
                            @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativePath);
            if(!path.isAbsolute()){
                // 相对路径处理
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                // 打平，防止有..
                path = projectRoot.resolve(relativePath).normalize();
                // 判断是否在沙盒内部
                if(!path.startsWith(projectRoot)){
                    return "错误：路径越界 - " + relativePath;
                }

                // 创建父目录，如果不存在
                Path parentDir = path.getParent();
                if(parentDir != null){
                    Files.createDirectories(parentDir);
                }
                // 写入文件内容
                Files.write(path, content.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                // 返回相对路径，不暴露绝对路径
                log.info("文件写入成功: {}", path.toAbsolutePath());
                return "文件写入成功: " + relativePath;
            }else{
                return "错误：路径不合法，应该使用相对路径，不允许绝对路径 - " + relativePath;
            }
        } catch (IOException e) {
            String errorMessage = "文件写入失败： " + relativePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativePath");
        // 获取拓展名
        String suffix = FileUtil.getSuffix(relativeFilePath);
        // 获取内容
        String content = arguments.getStr("content");
        return String.format("""
         [工具调用] %s %s
         ```%s
         %s
         ```
         """, getDisplayName(),relativeFilePath, suffix, content);
    }
}
