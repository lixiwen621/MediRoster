package com.mediroster.dto.response;

/**
 * 排班周自动生成结果。
 *
 * @author tongguo.li
 */
public record RosterWeekGenerateResponse(
        Long weekId,
        String strategy,
        Integer generatedCellCount,
        Integer overwrittenCellCount,
        Integer skippedConfirmedCount,
        Integer dryRun,
        String message
) {
}
