package com.mediroster.common.exception;

/**
 * 国际化业务异常（与 {@link BusinessException} 等价，便于在代码中区分「需走 i18n 的业务错误」）。
 *
 * @author tongguo.li
 */
public class I18nMessageException extends BusinessException {

    public I18nMessageException(String errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, messageArgs);
    }
}
