package com.mediroster.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日历日返回。
 *
 * @author tongguo.li
 */
public record CalendarDayResponse(
        Long id,
        LocalDate calDate,
        String dayType,
        String holidayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
