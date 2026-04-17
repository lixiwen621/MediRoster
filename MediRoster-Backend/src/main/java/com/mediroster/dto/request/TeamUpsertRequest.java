package com.mediroster.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 班组创建/更新请求。
 * <p>兼容前端常用字段名：{@code name}/{@code code}/{@code remark}（与 {@code teamName}/{@code teamCode}/{@code description} 等价）。
 *
 * @author tongguo.li
 */
public record TeamUpsertRequest(
        @JsonAlias("code") @NotBlank @Size(max = 64) String teamCode,
        @JsonAlias("name") @NotBlank @Size(max = 128) String teamName,
        @JsonAlias("remark") @Size(max = 512) String description,
        Integer enabled
) {
}
