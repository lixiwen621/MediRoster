package com.mediroster.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班单元格返回。
 *
 * @author tongguo.li
 */
public record RosterCellResponse(
        Long id,
        Long rosterWeekId,
        Long staffId,
        LocalDate workDate,
        Long shiftTypeId,
        Long postId,
        String postLabel,
        Integer validationExempt,
        String exemptReason,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
