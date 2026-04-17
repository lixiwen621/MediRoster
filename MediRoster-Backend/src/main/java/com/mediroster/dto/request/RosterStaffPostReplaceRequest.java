package com.mediroster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 覆盖周岗位摘要行。
 *
 * @author tongguo.li
 */
public record RosterStaffPostReplaceRequest(
        @Valid List<Item> items
) {
    public record Item(
            @NotNull Long staffId,
            Long displayPostId,
            @Size(max = 64) String displayLabel
    ) {
    }
}
