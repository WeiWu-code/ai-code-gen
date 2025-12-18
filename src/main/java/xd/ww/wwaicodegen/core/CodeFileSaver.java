package xd.ww.wwaicodegen.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

/**
 * 把生成的代码保存到本地
 * @author wei
 */
public class CodeFileSaver {
    // 文件保存的根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存Html网页
     * @param htmlCodeResult AI返回的标准HtmlCodeResult结构化输出
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String basedirName = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(basedirName, "index.html", htmlCodeResult.getHtmlCode());
        return new File(basedirName);
    }
    // 保存多文件网页代码
    /**
     * 保存Html网页
     * @param multiFileCodeResult AI返回的多文件结构化输出
     */
    public static File saveMultiHtmlCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String basedirName = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(basedirName, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(basedirName, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(basedirName, "script.js", multiFileCodeResult.getJsCode());
        return new File(basedirName);
    }

    // 构建文件夹名字： tmp/code_output/_业务类型_雪花ID
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }
    // 保存单个文件（通用方法）
    /**
     * 写入单个文件
     */
    private static void writeToFile(String dir, String fileName, String content) {
        String filePath = dir + File.separator + fileName;
        FileUtil.writeString(filePath, content, CharsetUtil.CHARSET_UTF_8);
    }
}
