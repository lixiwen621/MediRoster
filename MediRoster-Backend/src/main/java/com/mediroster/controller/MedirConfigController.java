package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.ConfigUpsertRequest;
import com.mediroster.dto.response.ConfigResponse;
import com.mediroster.service.MedirConfigService;
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
 * 键值规则配置 API（teamId=0 为全局）。
 *
 * @author tongguo.li
 */
@Tag(name = "排班规则配置")
@RestController
@RequestMapping("/api/v1/medir/config")
@RequiredArgsConstructor
public class MedirConfigController {

    private final MedirConfigService configService;

    @GetMapping
    @Operation(summary = "列出配置；不传 teamId 表示全部；传 teamId 仅该班组（teamId=0 为全局）")
    public ApiResponse<List<ConfigResponse>> list(@RequestParam(required = false) Long teamId) {
        return ApiResponse.ok(configService.list(teamId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "配置详情")
    public ApiResponse<ConfigResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(configService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增配置")
    public ApiResponse<ConfigResponse> create(@Valid @RequestBody ConfigUpsertRequest req) {
        return ApiResponse.ok(configService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置")
    public ApiResponse<ConfigResponse> update(@PathVariable Long id, @Valid @RequestBody ConfigUpsertRequest req) {
        return ApiResponse.ok(configService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ApiResponse.ok();
    }
}
