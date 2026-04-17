package com.mediroster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 人员创建/更新。
 *
 * @author tongguo.li
 */
public record StaffUpsertRequest(
        @NotNull Long teamId,
        @NotBlank @Size(max = 64) String name,
        @Size(max = 64) String employeeNo,
        @Size(max = 32) String phone,
        @Size(max = 128) String email,
        @NotBlank @Size(max = 32) String memberType,
        @NotNull Integer sortOrder,
        @NotNull Integer status,
        Long fixedPostId,
        @Size(max = 512) String remark
) {
}
