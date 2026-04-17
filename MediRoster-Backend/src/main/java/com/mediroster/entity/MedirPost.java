package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_post。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
