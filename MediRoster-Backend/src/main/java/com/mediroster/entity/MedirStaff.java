package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_staff。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirStaff {

    private Long id;
    private Long teamId;
    private String name;
    private String employeeNo;
    private String phone;
    private String email;
    private String memberType;
    private Integer sortOrder;
    private Integer status;
    private Long fixedPostId;
    private String remark;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
