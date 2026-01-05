package xd.ww.wwaicodegen.service;

/**
 * 网页截图并上传到COS
 * @author wei
 */

public interface ScreenShotService {
    /**
     * 通用网页截图并上传至COS服务
     * @param webUrl 网页
     * @return 返回图片的访问路径
     */
    String generateAndUploadScreenShot(String webUrl);
}
