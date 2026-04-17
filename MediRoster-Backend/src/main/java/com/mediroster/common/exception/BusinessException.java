package com.mediroster.common.exception;

import java.util.Arrays;
import lombok.Getter;

/**
 * 业务异常：{@code errorCode} 对外稳定；{@code messageKey} 为 {@code messages_*.properties} 中的键，可选占位符参数。
 *
 * @author tongguo.li
 */
@Getter
public class BusinessException extends RuntimeException {

    public static final int NOT_FOUND = 404;
    public static final int OPTIMISTIC_LOCK = 409;
    public static final int CONFLICT = 409;
    public static final int VALIDATION_ERROR = 400;
    public static final int DUPLICATE_KEY = 409;
    public static final int INTERNAL_ERROR = 500;

    private final int errorCode;
    private final String messageKey;
    private final Object[] messageArgs;

    /**
     * @param errorCode 业务错误码（HTTP 状态码）
     * @param messageKey 国际化消息键
     * @param messageArgs 消息占位符参数（如 {0}）
     */
    public BusinessException(int errorCode, String messageKey, Object... messageArgs) {
        super(messageKey);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs != null ? Arrays.copyOf(messageArgs, messageArgs.length) : new Object[0];
    }
}
