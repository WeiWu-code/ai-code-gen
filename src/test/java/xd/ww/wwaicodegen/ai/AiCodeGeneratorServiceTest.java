package xd.ww.wwaicodegen.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    AiCodeGeneratorService aiCodeGeneratorService;
    
    @Test
    void generateCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateCode("你好,帮我做个吴玮的个人博客，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiCode("你好");
        Assertions.assertNotNull(result);
    }
}