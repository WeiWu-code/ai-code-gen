package xd.ww.wwaicodegen.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存模板类，抽象出了公共方法
 * @param <T>
 * @author wei
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存的根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板方法：保存代码的标准流程
     * @param result 要保存的结构化代码对象
     * @return 保存的文件
     */
    public final File saveCode(T result, Long appId){
        // 校验参数是否合法
        validateInput(result);
        // 构建唯一目录
        String uniqueDir = buildUniqueDir(appId);
        // 保存文件
        saveFiles(result, uniqueDir);
        // 返回
        return new File(uniqueDir);
    }

    /**
     * 保存文件，由字类实现
     * @param result 要保存的结构化对象
     * @param uniqueDir 要保存的文件夹
     */
    protected abstract void saveFiles(T result, String uniqueDir);

    /**
     * 校验输入，可由字类重写
     * @param result 代码生成结果
     */
    protected void validateInput(T result){
        if(result == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "result is null");
        }
    }

    /**
     * 根据业务类型，构建文件夹名字
     * @param appId appId
     * @return 文件夹名字
     */
    // 构建文件夹名字： tmp/code_output/_业务类型_appId
    protected final String buildUniqueDir(Long appId) {
        if(appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId is null");
        }
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     * @param dir 文件夹
     * @param fileName 文件名
     * @param content 文件内容
     */
    protected final void writeToFile(String dir, String fileName, String content) {
        if(StringUtils.isNotBlank(content)){
            String filePath = dir + File.separator + fileName;
            FileUtil.writeString(content, filePath, CharsetUtil.CHARSET_UTF_8);
        }
    }

    /**
     * 获取代码类型，由字类实现
     * @return 代码类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();
}
