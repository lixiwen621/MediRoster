package com.mediroster.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_roster_cell。
 *
 * @author tongguo.li
 */
@Data
public class MedirRosterCell {

    private Long id;
    private Long rosterWeekId;
    private Long staffId;
    private LocalDate workDate;
    private Long shiftTypeId;
    private Long postId;
    private String postLabel;
    private Integer validationExempt;
    private String exemptReason;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
