package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_roster_weekend_stat。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirRosterWeekWeekendStat {

    private Long id;
    private Long rosterWeekId;
    private Long staffId;
    private Integer weekendFullOverride;
    private Integer lastWeekendOverride;
    private String overrideReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
