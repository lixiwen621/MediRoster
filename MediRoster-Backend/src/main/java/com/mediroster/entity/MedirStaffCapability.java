package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_staff_capability。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirStaffCapability {

    private Long id;
    private Long staffId;
    private String capabilityCode;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
