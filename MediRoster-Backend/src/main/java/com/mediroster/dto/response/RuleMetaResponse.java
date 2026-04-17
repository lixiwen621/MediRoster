package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 规则元数据返回（配置页）。
 *
 * @author tongguo.li
 */
public record RuleMetaResponse(
        Long id,
        String ruleCode,
        String category,
        String labelZh,
        String valueType,
        String defaultValue,
        String optionsJson,
        String helpText,
        Integer sortOrder,
        Integer enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
