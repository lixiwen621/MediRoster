package com.mediroster.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 创建排班周。
 *
 * @author tongguo.li
 */
public record RosterWeekCreateRequest(
        @NotNull Long teamId,
        @NotNull LocalDate weekStartDate,
        @NotNull Integer yearLabel,
        @NotNull Integer status,
        @Size(max = 512) String remark
) {
}
