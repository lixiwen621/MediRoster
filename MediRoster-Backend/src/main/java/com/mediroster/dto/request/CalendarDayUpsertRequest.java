package com.mediroster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 日历日创建/更新。
 *
 * @author tongguo.li
 */
public record CalendarDayUpsertRequest(
        @NotNull LocalDate calDate,
        @NotBlank @Size(max = 32) String dayType,
        @Size(max = 128) String holidayName
) {
}
