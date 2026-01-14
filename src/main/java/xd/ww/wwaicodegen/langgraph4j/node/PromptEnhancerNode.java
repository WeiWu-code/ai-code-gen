package xd.ww.wwaicodegen.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import xd.ww.wwaicodegen.langgraph4j.model.ImageResource;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder.sendEndSseEvent;

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            try{
                SseContextHolder.sendProcessing("提示词增强");
                // 获取原始prompt和收集的图片
                String originPrompt = context.getOriginalPrompt();
                String imageListStr = context.getImageListStr();
                List<ImageResource> imageList = context.getImageList();
                // 构建增强后的提示词
                StringBuilder enhancerPrompt = new StringBuilder();
                enhancerPrompt.append(originPrompt);
                // 如果有图片资源，则让其主动用上
                if(!CollUtil.isEmpty(imageList) || StrUtil.isNotBlank(imageListStr)){
                    enhancerPrompt.append("\n\n##可用图片素材\n");
                    enhancerPrompt.append("请在生成网站使用以下图片资源，将这些图片合理的嵌入到网站的响应位置中。\n");
                    if(CollUtil.isNotEmpty(imageList)){
                        for(ImageResource image : imageList){
                            enhancerPrompt.append("- ");
                            enhancerPrompt.append(image.getCategory().getText());
                            enhancerPrompt.append(": ");
                            enhancerPrompt.append(image.getDescription());
                            enhancerPrompt.append("(");
                            enhancerPrompt.append(image.getUrl());
                            enhancerPrompt.append(") \n");
                        }
                    }else{
                        enhancerPrompt.append(imageListStr);
                    }
                }
                String enhancerPromptStr = enhancerPrompt.toString();
                // 更新状态
                context.setCurrentStep("提示词增强");
                context.setEnhancedPrompt(enhancerPromptStr);
                log.info("提示词增强完成, 增强后长度 {} 字符", enhancerPromptStr.length());
                sendEndSseEvent(2, "提示词增强");
            }catch (Exception e){
                log.error("提示词增强失败: {}", e.getMessage(), e);
            }

            return WorkflowContext.saveContext(context);
        });
    }
}
