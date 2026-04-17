package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.StaffCapabilityUpsertRequest;
import com.mediroster.dto.response.StaffCapabilityResponse;
import com.mediroster.service.MedirStaffCapabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人员能力标签 API。
 *
 * @author tongguo.li
 */
@Tag(name = "人员能力")
@RestController
@RequestMapping("/api/v1/medir/staff/{staffId}/capabilities")
@RequiredArgsConstructor
public class MedirStaffCapabilityController {

    private final MedirStaffCapabilityService capabilityService;

    @GetMapping
    @Operation(summary = "能力列表")
    public ApiResponse<List<StaffCapabilityResponse>> list(@PathVariable Long staffId) {
        return ApiResponse.ok(capabilityService.listByStaff(staffId));
    }

    @PostMapping
    @Operation(summary = "新增能力")
    public ApiResponse<StaffCapabilityResponse> add(
            @PathVariable Long staffId,
            @Valid @RequestBody StaffCapabilityUpsertRequest req) {
        return ApiResponse.ok(capabilityService.add(staffId, req));
    }

    @DeleteMapping("/{capabilityId}")
    @Operation(summary = "删除能力")
    public ApiResponse<Void> delete(@PathVariable Long staffId, @PathVariable Long capabilityId) {
        capabilityService.delete(staffId, capabilityId);
        return ApiResponse.ok();
    }
}
