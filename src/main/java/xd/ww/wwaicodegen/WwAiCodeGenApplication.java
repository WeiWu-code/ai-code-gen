package xd.ww.wwaicodegen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("xd.ww.wwaicodegen.mapper")
public class WwAiCodeGenApplication {

    public static void main(String[] args) {
        SpringApplication.run(WwAiCodeGenApplication.class, args);
    }

}
