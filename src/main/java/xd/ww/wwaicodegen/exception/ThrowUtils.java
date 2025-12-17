package xd.ww.wwaicodegen.exception;

/**
 * 抛异常工具类
 *
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition 待检查的条件
     * @param runtimeException 要抛出的异常，RuntimeException类及其子类
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 待检查的条件
     * @param errorCode 错误码，见ErrorCode枚举类
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 待检查的条件
     * @param errorCode 错误码，见ErrorCode枚举类
     * @param message 额外描述信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
