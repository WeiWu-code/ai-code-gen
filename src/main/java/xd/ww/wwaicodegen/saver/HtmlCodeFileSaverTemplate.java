package xd.ww.wwaicodegen.saver;

import cn.hutool.core.util.StrUtil;
import xd.ww.wwaicodegen.ai.model.HtmlCodeResult;
import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected void saveFiles(HtmlCodeResult result, String uniqueDir) {
        // 保存html文件
        writeToFile(uniqueDir, "index.html", result.getHtmlCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return  CodeGenTypeEnum.HTML;
    }

    /**
     * 重写自己的校验
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // html代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "html不能为空");
        }
    }
}
