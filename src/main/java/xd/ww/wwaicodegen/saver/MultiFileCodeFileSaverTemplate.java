package xd.ww.wwaicodegen.saver;

import cn.hutool.core.util.StrUtil;
import xd.ww.wwaicodegen.ai.model.MultiFileCodeResult;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

import java.io.File;

public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected void saveFiles(MultiFileCodeResult result, String uniqueDir) {
        // 保存html文件
        writeToFile(uniqueDir, "index.html", result.getHtmlCode());
        // 保存css文件
        writeToFile(uniqueDir, "style.css", result.getCssCode());
        // 保存js文件
        writeToFile(uniqueDir, "script.js", result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有html，css，js可以为空
        // html代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "html不能为空");
        }
    }
}
