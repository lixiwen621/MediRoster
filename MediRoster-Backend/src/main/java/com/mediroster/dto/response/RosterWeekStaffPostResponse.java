package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 周岗位摘要返回。
 *
 * @author tongguo.li
 */
public record RosterWeekStaffPostResponse(
        Long id,
        Long rosterWeekId,
        Long staffId,
        Long displayPostId,
        String displayLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
