package com.mediroster.dto.response;

import java.time.LocalDateTime;

/**
 * 人员返回。
 *
 * @author tongguo.li
 */
public record StaffResponse(
        Long id,
        Long teamId,
        String name,
        String employeeNo,
        String phone,
        String email,
        String memberType,
        Integer sortOrder,
        Integer status,
        Long fixedPostId,
        String remark,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
