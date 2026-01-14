package xd.ww.wwaicodegen.ai.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xd.ww.wwaicodegen.constant.AppConstant;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * 文件目录读取工具
 * 使用 Hutool 简化文件操作
 */
@Slf4j
@Component
public class FileDirReadTool extends BaseTool {
    @Override
    public String getToolName() {
        return "readDir";
    }

    @Override
    public String getDisplayName() {
        return "读取目录";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeDirPath = arguments.getStr("relativeDirPath");
        if(relativeDirPath.isEmpty()) {
            relativeDirPath = "根目录";
        }
        return String.format("[工具调用]  %s %s", getDisplayName(), relativeDirPath);
    }

    /**
     * 需要忽略的文件和目录
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules", ".git", "dist", "build", ".DS_Store",
            ".env", "target", ".mvn", ".idea", ".vscode", "coverage"
    );

    /**
     * 需要忽略的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log", ".tmp", ".cache", ".lock"
    );

    @Tool("读取目录结构，获取指定目录下的所有文件和子目录信息")
    public String readDir(
            @P("目录的相对路径，为空则读取整个项目结构")
            String relativeDirPath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativeDirPath == null ? "" : relativeDirPath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                // 打平，防止有..
                path = projectRoot.resolve(relativeDirPath == null ? "" : relativeDirPath).normalize();
                // 判断是否在沙盒内部
                if(!path.startsWith(projectRoot)){
                    return "错误：路径越界 - " + relativeDirPath;
                }

                File targetDir = path.toFile();
                if (!targetDir.exists() || !targetDir.isDirectory()) {
                    return "错误：目录不存在或不是目录 - " + relativeDirPath;
                }
                StringBuilder structure = new StringBuilder();
                structure.append("项目目录结构:\n");

                // 使用 Hutool 递归获取所有文件
                List<File> allFiles = FileUtil.loopFiles(targetDir, file -> !shouldIgnore(targetDir.toPath(), file));

                // 按路径深度和名称排序显示
                allFiles.stream()
                        .sorted((f1, f2) -> {
                            int depth1 = getRelativeDepth(targetDir, f1);
                            int depth2 = getRelativeDepth(targetDir, f2);
                            if (depth1 != depth2) {
                                return Integer.compare(depth1, depth2);
                            }
                            return f1.getPath().compareTo(f2.getPath());
                        })
                        .forEach(file -> {
                            int depth = getRelativeDepth(targetDir, file);
                            // 添加上一级文件夹
                            if(depth > 0){
                                String parentDirName = targetDir.toPath().relativize(file.getParentFile().toPath()).toString();
                                structure.append(parentDirName).append(File.separator);
                            }
                            structure.append(file.getName()).append("\n");
                        });
                return structure.toString();
            }else{
                return "错误：路径不合法，应该使用相对路径，不允许绝对路径 - " + relativeDirPath;
            }

        } catch (Exception e) {
            String errorMessage = "读取目录结构失败: " + relativeDirPath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 计算文件相对于根目录的深度
     */
    private int getRelativeDepth(File root, File file) {
        Path rootPath = root.toPath();
        Path filePath = file.toPath();
        return rootPath.relativize(filePath).getNameCount() - 1;
    }

    /**
     * 判断是否应该忽略该文件或目录
     */
    private boolean shouldIgnore(Path path, File file) {
        // 计算file相对于targetDir的路径
        Path relativePath = path.relativize(file.toPath());
        for(Path name : relativePath){
            String nameStr = name.toString();
            // 1. 统一忽略某些名字（无论文件还是目录）
            if (IGNORED_NAMES.contains(nameStr)) {
                return true;
            }
        }
        // 2. 如果是文件，再额外忽略某些后缀
        return IGNORED_EXTENSIONS.stream().anyMatch(file.getName()::endsWith);
    }
}
