package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_staff_capability。
 *
 * @author tongguo.li
 */
@Data
public class MedirStaffCapability {

    private Long id;
    private Long staffId;
    private String capabilityCode;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
