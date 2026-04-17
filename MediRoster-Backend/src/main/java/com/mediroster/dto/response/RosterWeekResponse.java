package com.mediroster.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班周返回。
 *
 * @author tongguo.li
 */
public record RosterWeekResponse(
        Long id,
        Long teamId,
        LocalDate weekStartDate,
        Integer yearLabel,
        Integer status,
        Integer version,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
