package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_roster_week_staff_post。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirRosterWeekStaffPost {

    private Long id;
    private Long rosterWeekId;
    private Long staffId;
    private Long displayPostId;
    private String displayLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
