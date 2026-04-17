package com.mediroster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 覆盖周末统计两列（周末全天、上周末）。
 *
 * @author tongguo.li
 */
public record RosterWeekWeekendStatReplaceRequest(
        @NotNull @Valid List<Item> items
) {
    public record Item(
            @NotNull Long staffId,
            @Min(0) Integer weekendFullOverride,
            @Min(0) Integer lastWeekendOverride,
            @Size(max = 512) String overrideReason
    ) {
    }
}
