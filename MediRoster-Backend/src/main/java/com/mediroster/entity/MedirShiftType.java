package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_shift_type。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirShiftType {

    private Long id;
    private String typeCode;
    private String nameZh;
    private Integer sortOrder;
    private Integer isRest;
    private Integer isDutyZhong;
    private Integer isDutyDa;
    private Integer isQiban;
    private Integer isSmallNight;
    private Integer countsDaytimeHeadcount;
    private Integer countsWeekendFullDayStat;
    private Integer countsAsZhongForStructure;
    private Integer countsAsLinForStructure;
    private Integer nextDayMustRest;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
