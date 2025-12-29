package xd.ww.wwaicodegen.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import xd.ww.wwaicodegen.constant.AppConstant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(@P("待写入文件的相对路径") String relativePath,
                            @P("待写入文件的文件内容") String content,
                            @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativePath);
            if(!path.isAbsolute()){
                // 相对路径处理
                String projectDirName = "vue_project_" + appId;
                Path projectDir = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectDir.resolve(relativePath);
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
        } catch (IOException e) {
            String errorMessage = "文件写入失败： " + relativePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
