package xd.ww.wwaicodegen.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xd.ww.wwaicodegen.config.CosClientConfig;

import java.io.File;

/**
 * Cos对象存储上传对象类
 */
@Component
@Slf4j
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件到COS
     * @param key 唯一键
     * @param file 文件
     * @return 上传结果
     */
    private PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到COS，并返回url
     * @param key 唯一键
     * @param file 要上传的文件
     * @return 可访问的url
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResult putObjectResult = putObject(key, file);
        if(putObjectResult != null){
            // 构建访问url
            String url = String.format("%s%s", cosClientConfig.getHost(), key);
            log.info("upload file url:{}", url);
            return url;
        }else{
            log.error("upload file error");
            return null;
        }
    }
}
