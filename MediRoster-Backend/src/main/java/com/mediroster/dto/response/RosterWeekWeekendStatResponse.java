package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 周末统计两列返回（自动值 + 最终值）。
 *
 * @author tongguo.li
 */
public record RosterWeekWeekendStatResponse(
        Long id,
        Long rosterWeekId,
        Long staffId,
        Integer weekendFullAuto,
        Integer weekendFullFinal,
        Integer lastWeekendAuto,
        Integer lastWeekendFinal,
        Integer isOverridden,
        String overrideReason,
        LocalDateTime updatedAt
) {
}
