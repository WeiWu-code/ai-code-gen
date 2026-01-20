package xd.ww.wwaicodegen.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import xd.ww.wwaicodegen.langgraph4j.model.ImageResource;
import xd.ww.wwaicodegen.langgraph4j.model.NodeResponseMessage;
import xd.ww.wwaicodegen.langgraph4j.state.WorkflowContext;
import xd.ww.wwaicodegen.langgraph4j.util.SseContextHolder;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            try{

                CodeGenTypeEnum codeType = context.getGenerationType();
                if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                    NodeResponseMessage startMessage = new NodeResponseMessage("{提示词增强}", "开始");
                    SseContextHolder.emit(JSONUtil.toJsonStr(startMessage));
                }else{
                    SseContextHolder.emit("\n\n开始{提示词增强}\n\n");
                }

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

                if(codeType.equals(CodeGenTypeEnum.VUE_PROJECT)){
                    NodeResponseMessage endMessage = new NodeResponseMessage("{提示词增强}", "完成");
                    SseContextHolder.emit(JSONUtil.toJsonStr(endMessage));
                }else{
                    SseContextHolder.emit("\n\n完成{提示词增强}\n\n");
                }
            }catch (Exception e){
                log.error("提示词增强失败: {}", e.getMessage(), e);
            }

            return WorkflowContext.saveContext(context);
        });
    }
}
