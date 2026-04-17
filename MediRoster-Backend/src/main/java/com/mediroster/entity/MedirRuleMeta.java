package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应表 medir_rule_meta。
 *
 * @author tongguo.li
 */
@Data
public class MedirRuleMeta {

    private Long id;
    private String ruleCode;
    private String category;
    private String labelZh;
    private String valueType;
    private String defaultValue;
    private String optionsJson;
    private String helpText;
    private Integer sortOrder;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
