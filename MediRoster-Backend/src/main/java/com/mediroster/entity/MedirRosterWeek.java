package com.mediroster.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_roster_week。
 *
 * @author tongguo.li
 */
@Data
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
