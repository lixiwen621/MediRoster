package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_post。
 *
 * @author tongguo.li
 */
@Data
public class MedirPost {

    private Long id;
    private String postCode;
    private String postName;
    private String description;
    private Integer sortOrder;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
