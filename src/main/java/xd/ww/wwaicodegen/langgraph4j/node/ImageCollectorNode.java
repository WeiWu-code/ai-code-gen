package xd.ww.wwaicodegen.langgraph4j.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import xd.ww.wwaicodegen.langgraph4j.ai.ImageCollectionService;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SpringContextUtil;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            String prompt = context.getOriginalPrompt();
            String imageListStr = "";
            // 实际执行图片收集逻辑
            // 获取AI图片收集服务
            try{
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                imageListStr = imageCollectionService.collectImages(prompt);
            }catch (Exception e){
                log.error("图片收集失败：{}", e.getMessage());
            }
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            log.info("图片收集完成");
            return WorkflowContext.saveContext(context);
        });
    }
}
