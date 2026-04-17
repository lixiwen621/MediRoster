package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 班次类型返回。
 *
 * @author tongguo.li
 */
public record ShiftTypeResponse(
        Long id,
        String typeCode,
        String nameZh,
        Integer sortOrder,
        Integer isRest,
        Integer isDutyZhong,
        Integer isDutyDa,
        Integer isQiban,
        Integer isSmallNight,
        Integer countsDaytimeHeadcount,
        Integer countsWeekendFullDayStat,
        Integer countsAsZhongForStructure,
        Integer countsAsLinForStructure,
        Integer nextDayMustRest,
        Integer enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
