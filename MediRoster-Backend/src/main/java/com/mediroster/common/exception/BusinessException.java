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

    private final String errorCode;
    private final String messageKey;
    private final Object[] messageArgs;

    /**
     * @param errorCode 业务错误码（如 NOT_FOUND）
     * @param messageKey 国际化消息键
     * @param messageArgs 消息占位符参数（如 {0}）
     */
    public BusinessException(String errorCode, String messageKey, Object... messageArgs) {
        super(messageKey);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs != null ? Arrays.copyOf(messageArgs, messageArgs.length) : new Object[0];
    }
}
