package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.StaffUpsertRequest;
import com.mediroster.dto.response.StaffResponse;
import com.mediroster.service.MedirStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人员 API。
 *
 * @author tongguo.li
 */
@Tag(name = "人员")
@RestController
@RequestMapping("/api/v1/medir/staff")
@RequiredArgsConstructor
public class MedirStaffController {

    private final MedirStaffService staffService;

    @GetMapping
    @Operation(summary = "按班组列出人员")
    public ApiResponse<List<StaffResponse>> list(
            @RequestParam Long teamId,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.ok(staffService.listByTeam(teamId, includeDeleted));
    }

    @GetMapping("/{id}")
    @Operation(summary = "人员详情")
    public ApiResponse<StaffResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(staffService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增人员")
    public ApiResponse<StaffResponse> create(@Valid @RequestBody StaffUpsertRequest req) {
        return ApiResponse.ok(staffService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新人员")
    public ApiResponse<StaffResponse> update(@PathVariable Long id, @Valid @RequestBody StaffUpsertRequest req) {
        return ApiResponse.ok(staffService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "软删除人员")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        staffService.softDelete(id);
        return ApiResponse.ok();
    }
}
