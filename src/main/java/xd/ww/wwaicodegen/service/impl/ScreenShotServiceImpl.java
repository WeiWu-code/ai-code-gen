package xd.ww.wwaicodegen.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.exception.ThrowUtils;
import xd.ww.wwaicodegen.manager.CosManager;
import xd.ww.wwaicodegen.service.ScreenShotService;
import xd.ww.wwaicodegen.util.WebScreenShotUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 实现ScreenShotService
 * @author wei
 */
@Slf4j
@Service
public class ScreenShotServiceImpl implements ScreenShotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenShot(String webUrl) {
        // 判断
        if(StrUtil.isBlank(webUrl)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入网址为空");
        }
        // 加载网页并截图
        String localScreenshotPath = WebScreenShotUtil.saveWebScreenShot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath),
                ErrorCode.SYSTEM_ERROR, "截图生成失败");
        // 上传到COS
        try{
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.SYSTEM_ERROR,
                    "Cos上传失败");
            log.info("网页 {} 截图上传成功 {}", webUrl, cosUrl);
            return cosUrl;
        }catch (Exception e){
            log.error("上传截图失败: {}", e.getMessage());
            // 不抛异常
            return null;
        }
        finally {
            // 不管成功与否，删除本地截图文件，防止占用过多内存
            cleanLocalFile(localScreenshotPath);
        }
    }

    /**
     * 清理上传后的截图文件
     * @param localScreenshotPath 截图文件地址
     */
    private void cleanLocalFile(String localScreenshotPath) {
        File file = new File(localScreenshotPath);
        if(file.exists()){
            File parentFile = file.getParentFile();
            FileUtil.del(parentFile);
            log.info("文件夹 {} 删除成功", parentFile.getName());
        }
    }

    /**
     * 上传本地截图到COS
     * @param localFile 本地截图地址
     * @return 上传后的可访问url，失败为null
     */
    private String uploadScreenshotToCos(String localFile){
        if(StrUtil.isBlank(localFile)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "本地截图为空");
        }
        // 检查文件是否存在
        File file = new File(localFile);
        ThrowUtils.throwIf(!file.exists(), ErrorCode.PARAMS_ERROR,
                "本地截图不存在");
        // 上传到COS存储
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compress.png";
        String key = generateCosKey(fileName);
        return cosManager.uploadFile(key, file);
    }

    /**
     * 生成Cos对象存储的Key
     * @param fileName 文件名字
     * @return 返回键，格式/screenshot/2026/01/01/filename.png
     */
    private String generateCosKey(String fileName) {
        String dataPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshot/%s/%s", dataPath, fileName);
    }
}
