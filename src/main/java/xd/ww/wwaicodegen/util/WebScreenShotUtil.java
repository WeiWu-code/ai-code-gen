package xd.ww.wwaicodegen.util;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * 对网页截图的工具类
 */
@Slf4j
public class WebScreenShotUtil {
    // 驱动程序
    private static final WebDriver driver;

    // 全局静态块，避免重复初始化驱动
    static {
        final int DEFAULT_WIDTH = 1280;
        final int DEFAULT_HEIGHT = 720;
        driver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 析构函数，释放资源
     */
    @PreDestroy
    private void destroy() {
        driver.quit();
    }


    /**
     * 加载网页，并截取网站缩略图
     * @param webUrl 网站地址
     * @return 缩略图地址
     */
    public static String saveWebScreenShot(String webUrl){
        try {
            // 1. 参数校验
            if(StrUtil.isBlank(webUrl)){
                return webUrl;
            }
            // 2. 创建临时目录
            String rootDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator
                    + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkParentDirs(rootDir);
            // 图片后缀
            final String IMAGE_PREFIX = ".png";
            String imgPath = rootDir +  File.separator + RandomUtil.randomNumbers(5) + IMAGE_PREFIX;
            // 3. 访问网页
            driver.get(webUrl);
            // 4. 截图
            waitPageLoad(driver);
            byte[] screenShot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
            // 5. 保存
            saveImage(screenShot, imgPath);
            log.info("网页: {} 截图已经保存至 {}",  webUrl, imgPath);
            // 6. 压缩
            compressImage(imgPath, imgPath);
            log.info("压缩图片: {} 完成", imgPath);
            return imgPath;
        } catch (WebDriverException e) {
            log.error("网页截图失败 {}", e.getMessage());
            return null;
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            WebDriverManager.chromedriver().useMirror().setup();
            // 配置 Chrome 选项
            WebDriver driver = getWebDriver(width, height);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    private static WebDriver getWebDriver(int width, int height) {
        ChromeOptions options = new ChromeOptions();
        // 无头模式
        options.addArguments("--headless");
        // 禁用GPU（在某些环境下避免问题）
        options.addArguments("--disable-gpu");
        // 禁用沙盒模式（Docker环境需要）
        options.addArguments("--no-sandbox");
        // 禁用开发者shm使用
        options.addArguments("--disable-dev-shm-usage");
        // 设置窗口大小
        options.addArguments(String.format("--window-size=%d,%d", width, height));
        // 禁用扩展
        options.addArguments("--disable-extensions");
        // 设置用户代理
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        // 创建驱动
        return new ChromeDriver(options);
    }


    /**
     * 保存图片到文件
     * @param imageBytes 字节流
     * @param path 绝对路径
     */
    private static void saveImage(byte[] imageBytes, String path){
        try{
            FileUtil.writeBytes(imageBytes, path);
        }catch (Exception e){
            log.error("保存图片失败 ：{}", path);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     * @param originPath 原始图片地址
     * @param compressPath 压缩后的图片地址
     */
    private static void compressImage(String originPath, String compressPath){
        try{
            float IMAGE_COMPRESS_QUALITY = 0.3f;
            ImgUtil.compress(new File(originPath), new File(compressPath), IMAGE_COMPRESS_QUALITY);
        }catch (Exception e){
            log.error("保存压缩失败 ：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待网页加载
     * @param webDriver 驱动
     */
    private static void waitPageLoad(WebDriver webDriver) {
        try {
            // 最多等10s
            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            // 等待是否加载完成
            webDriverWait.until(driver -> ((JavascriptExecutor) driver).
                    executeScript("return document.readyState")
                    .equals("complete"));
            // 再等一会会
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "页面加载失败");
        }
    }
}
