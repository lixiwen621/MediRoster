package com.mediroster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * 覆盖某周全部单元格（先删后插）；cells 为空或 null 表示清空当周单元格。
 *
 * @author tongguo.li
 */
public record RosterCellReplaceRequest(
        @Valid List<CellItem> cells
) {
    public record CellItem(
            @NotNull Long staffId,
            @NotNull LocalDate workDate,
            @NotNull Long shiftTypeId,
            Long postId,
            @Size(max = 64) String postLabel,
            @NotNull Integer validationExempt,
            @Size(max = 512) String exemptReason,
            @Size(max = 512) String remark
    ) {
    }
}
