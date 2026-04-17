package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_config（业务键值规则）。
 *
 * @author tongguo.li
 */
@Data
public class MedirConfig {

    private Long id;
    private Long teamId;
    private String configKey;
    private String configValue;
    private String valueType;
    private String category;
    private String description;
    private Integer sortOrder;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
