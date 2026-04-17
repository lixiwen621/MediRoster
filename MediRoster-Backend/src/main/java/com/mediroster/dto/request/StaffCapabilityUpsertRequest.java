package com.mediroster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 人员能力新增。
 *
 * @author tongguo.li
 */
public record StaffCapabilityUpsertRequest(
        @NotBlank @Size(max = 64) String capabilityCode,
        @NotNull Integer enabled
) {
}
