package com.mediroster.common.i18n;

import com.mediroster.common.exception.BusinessException;

/**
 * 国际化业务断言，API 风格对齐 Guava {@code com.google.common.base.Preconditions}，
 * 不满足条件时抛出 {@link BusinessException}（由全局异常处理按 {@code messageKey} 解析文案）。
 *
 * @author tongguo.li
 */
public final class I18nPreconditions {

    private I18nPreconditions() {
    }

    /**
     * 与 Guava {@code checkArgument} 对应：入参/调用方约定不满足时抛出。
     */
    public static void checkArgument(boolean expression, int errorCode, String messageKey, Object... messageArgs) {
        if (!expression) {
            throw new BusinessException(errorCode, messageKey, messageArgs);
        }
    }

    /**
     * 与 Guava {@code checkState} 对应：对象内部状态不满足时抛出。
     */
    public static void checkState(boolean expression, int errorCode, String messageKey, Object... messageArgs) {
        if (!expression) {
            throw new BusinessException(errorCode, messageKey, messageArgs);
        }
    }

    /**
     * 与 Guava {@code checkNotNull} 对应：{@code null} 时抛出，否则返回引用。
     */
    public static <T> T checkNotNull(T reference, int errorCode, String messageKey, Object... messageArgs) {
        if (reference == null) {
            throw new BusinessException(errorCode, messageKey, messageArgs);
        }
        return reference;
    }
}
