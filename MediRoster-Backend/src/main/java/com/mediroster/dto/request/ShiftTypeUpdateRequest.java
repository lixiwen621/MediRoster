package com.mediroster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 班次类型更新（全量字段）。
 *
 * @author tongguo.li
 */
public record ShiftTypeUpdateRequest(
        @NotBlank @Size(max = 32) String typeCode,
        @NotBlank @Size(max = 32) String nameZh,
        @NotNull Integer sortOrder,
        @NotNull Integer isRest,
        @NotNull Integer isDutyZhong,
        @NotNull Integer isDutyDa,
        @NotNull Integer isQiban,
        @NotNull Integer isSmallNight,
        @NotNull Integer countsDaytimeHeadcount,
        @NotNull Integer countsWeekendFullDayStat,
        @NotNull Integer countsAsZhongForStructure,
        @NotNull Integer countsAsLinForStructure,
        @NotNull Integer nextDayMustRest,
        @NotNull Integer enabled
) {
}
