package xd.ww.wwaicodegen.ai.tool;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 工具管理器
 * 统一管理所有的工具
 * @author wei
 */
@Slf4j
@Component
public class ToolManager {

    /**
     * 工具名称到工具类的映射
     */
    private final HashMap<String, BaseTool> toolsMap = new HashMap<>();

    /**
     * 自动注入所有的工具
     * -- GETTER --
     */
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化工具映射
     */
    @PostConstruct
    public void init() {
        for (BaseTool tool : tools) {
            toolsMap.put(tool.getToolName(), tool);
            log.info("注册工具 {} -> {}",  tool.getToolName(), tool.getDisplayName());
        }
        log.info("工具初始化完成，注册 {} 个工具",  toolsMap.size());
    }

    /**
     * 根据工具名称获取实例
     */
    public BaseTool getTool(String toolName) {
        return toolsMap.get(toolName);
    }

    /**
     * 获取工具集合
     * @return 工具集合
     */
    public BaseTool[] getAllTools() {
        return tools;
    }
}
