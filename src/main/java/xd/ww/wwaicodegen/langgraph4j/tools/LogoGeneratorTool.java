package xd.ww.wwaicodegen.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xd.ww.wwaicodegen.langgraph4j.model.ImageCategoryEnum;
import xd.ww.wwaicodegen.langgraph4j.model.ImageResource;
import xd.ww.wwaicodegen.manager.CosManager;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class LogoGeneratorTool {

    @Resource
    private CosManager cosManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        // 构建 Logo 设计提示词
        String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
//        String url = String.format("https://image.pollinations.ai/prompt/%s", logoPrompt);
        String url = "https://image.pollinations.ai/prompt/" + URLEncoder.encode(logoPrompt, StandardCharsets.UTF_8)
                + "?width=384&height=384&model=flux&seed=42";

        try (HttpResponse response = HttpRequest.get(url).timeout(30000).execute()) {
            if(!response.isOk()) {
                return logoList;
            }
            // 如果请求ok
            File imageFileTemp = FileUtil.createTempFile("logo", ".png", true);
            response.writeBody(imageFileTemp.getAbsolutePath());
            // 将imageFileTemp上传到COS
            // 上传到COS
            String keyName = String.format("/logo/%s/%s",
                    RandomUtil.randomString(5), imageFileTemp.getName());
            String cosUrl = cosManager.uploadFile(keyName, imageFileTemp);
            // 删除temp
            FileUtil.del(imageFileTemp);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.LOGO)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }
}
