package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_roster_week_staff_post。
 *
 * @author tongguo.li
 */
@Data
public class MedirRosterWeekStaffPost {

    private Long id;
    private Long rosterWeekId;
    private Long staffId;
    private Long displayPostId;
    private String displayLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
