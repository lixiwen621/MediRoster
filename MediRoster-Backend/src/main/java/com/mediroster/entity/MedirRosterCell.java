package com.mediroster.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_roster_cell。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
