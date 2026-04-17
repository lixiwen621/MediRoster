package com.mediroster.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_calendar_day。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirCalendarDay {

    private Long id;
    private LocalDate calDate;
    private String dayType;
    private String holidayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
