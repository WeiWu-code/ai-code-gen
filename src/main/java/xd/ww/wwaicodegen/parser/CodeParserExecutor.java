package xd.ww.wwaicodegen.parser;

import xd.ww.wwaicodegen.exception.BusinessException;
import xd.ww.wwaicodegen.exception.ErrorCode;
import xd.ww.wwaicodegen.model.emums.CodeGenTypeEnum;

/**
 * 代码解析执行入口
 * 根据要解析的类型，自动调用相应的解析器
 * @author wei
 */
public class CodeParserExecutor {
    private static final HtmpCodeParser htmpCodeParser = new HtmpCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码类型枚举
     * @return 根据枚举，返回对应的对象
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            case HTML -> htmpCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " +  codeGenTypeEnum);
        };
    }
}
