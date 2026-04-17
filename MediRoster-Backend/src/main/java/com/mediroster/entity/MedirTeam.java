package com.mediroster.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应表 medir_team。
 *
 * @author tongguo.li
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedirTeam {

    private Long id;
    private String teamCode;
    private String teamName;
    private String description;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
