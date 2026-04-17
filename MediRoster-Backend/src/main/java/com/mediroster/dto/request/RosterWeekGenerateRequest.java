package com.mediroster.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 排班周自动生成请求。
 *
 * @author tongguo.li
 */
public record RosterWeekGenerateRequest(
        @Size(max = 32) String strategy,
        Integer respectManualConfirmed,
        Integer dryRun,
        @Size(max = 512) String reason
) {
}
