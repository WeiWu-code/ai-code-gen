package xd.ww.wwaicodegen.saver;

import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

/**
 * 文件保存执行入口
 * 根据要保存的类型，自动调用相应的保存器
 * @author wei
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();


    /**
     * 执行代码保存
     * @param result 代码对象内容
     * @param codeGenTypeEnum 代码类型枚举
     * @return 返回保存的文件
     */
    public static File executorSaver(Object result, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) result);
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) result);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型： " + codeGenTypeEnum);
        };
    }
}
