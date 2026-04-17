package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 班组返回。
 *
 * @author tongguo.li
 */
public record TeamResponse(
        Long id,
        String teamCode,
        String teamName,
        String description,
        Integer enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
