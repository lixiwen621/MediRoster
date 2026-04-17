package com.mediroster.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_roster_week。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirRosterWeek {

    private Long id;
    private Long teamId;
    private LocalDate weekStartDate;
    private Integer yearLabel;
    private Integer status;
    private Integer version;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
