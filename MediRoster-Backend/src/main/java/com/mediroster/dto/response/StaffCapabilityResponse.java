package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 人员能力返回。
 *
 * @author tongguo.li
 */
public record StaffCapabilityResponse(
        Long id,
        Long staffId,
        String capabilityCode,
        Integer enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
