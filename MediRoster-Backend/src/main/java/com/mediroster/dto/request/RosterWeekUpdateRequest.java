package com.mediroster.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新排班周（乐观锁 version）。
 *
 * @author tongguo.li
 */
public record RosterWeekUpdateRequest(
        @NotNull Integer yearLabel,
        @NotNull Integer status,
        @Size(max = 512) String remark,
        @NotNull Integer version
) {
}
