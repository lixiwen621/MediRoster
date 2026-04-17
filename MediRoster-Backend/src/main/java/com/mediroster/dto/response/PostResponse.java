package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 岗位返回。
 *
 * @author tongguo.li
 */
public record PostResponse(
        Long id,
        String postCode,
        String postName,
        String description,
        Integer sortOrder,
        Integer enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
