package com.mediroster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 键值配置创建/更新。
 *
 * @author tongguo.li
 */
public record ConfigUpsertRequest(
        @NotNull Long teamId,
        @NotBlank @Size(max = 128) String configKey,
        @NotBlank String configValue,
        @NotBlank @Size(max = 32) String valueType,
        @Size(max = 64) String category,
        @Size(max = 512) String description,
        @NotNull Integer sortOrder,
        @NotNull Integer enabled
) {
}
