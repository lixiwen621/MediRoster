package com.mediroster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 岗位创建/更新。
 *
 * @author tongguo.li
 */
public record PostUpsertRequest(
        @NotBlank @Size(max = 32) String postCode,
        @NotBlank @Size(max = 64) String postName,
        @Size(max = 512) String description,
        @NotNull Integer sortOrder,
        @NotNull Integer enabled
) {
}
