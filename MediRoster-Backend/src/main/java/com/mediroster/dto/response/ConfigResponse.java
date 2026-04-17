package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 键值配置返回。
 *
 * @author tongguo.li
 */
public record ConfigResponse(
        Long id,
        Long teamId,
        String configKey,
        String configValue,
        String valueType,
        String category,
        String description,
        Integer sortOrder,
        Integer enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
