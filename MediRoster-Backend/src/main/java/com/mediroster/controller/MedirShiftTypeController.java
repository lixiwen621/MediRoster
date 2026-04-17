package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.ShiftTypeUpdateRequest;
import com.mediroster.dto.response.ShiftTypeResponse;
import com.mediroster.service.MedirShiftTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 班次类型 API。
 *
 * @author tongguo.li
 */
@Tag(name = "班次类型")
@RestController
@RequestMapping("/api/v1/medir/shift-types")
@RequiredArgsConstructor
public class MedirShiftTypeController {

    private final MedirShiftTypeService shiftTypeService;

    @GetMapping
    @Operation(summary = "班次类型列表（含行为标志）")
    public ApiResponse<List<ShiftTypeResponse>> list() {
        return ApiResponse.ok(shiftTypeService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "班次类型详情")
    public ApiResponse<ShiftTypeResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(shiftTypeService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新班次类型")
    public ApiResponse<ShiftTypeResponse> update(@PathVariable Long id, @Valid @RequestBody ShiftTypeUpdateRequest req) {
        return ApiResponse.ok(shiftTypeService.update(id, req));
    }
}
