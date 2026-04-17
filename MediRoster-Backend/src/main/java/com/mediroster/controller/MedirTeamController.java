package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.TeamUpsertRequest;
import com.mediroster.dto.response.TeamResponse;
import com.mediroster.service.MedirTeamService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 班组 API。
 *
 * @author tongguo.li
 */
@Tag(name = "班组")
@RestController
@RequestMapping("/api/v1/medir/teams")
@RequiredArgsConstructor
public class MedirTeamController {

    private final MedirTeamService teamService;

    @Operation(summary = "班组列表")
    @GetMapping
    public ApiResponse<List<TeamResponse>> list() {
        return ApiResponse.ok(teamService.listAll());
    }

    @Operation(summary = "班组详情")
    @GetMapping("/{id}")
    public ApiResponse<TeamResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(teamService.getById(id));
    }

    @Operation(summary = "创建班组")
    @PostMapping
    public ApiResponse<TeamResponse> create(@Valid @RequestBody TeamUpsertRequest req) {
        return ApiResponse.ok(teamService.create(req));
    }

    @Operation(summary = "更新班组")
    @PutMapping("/{id}")
    public ApiResponse<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamUpsertRequest req) {
        return ApiResponse.ok(teamService.update(id, req));
    }

    @Operation(summary = "删除班组")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ApiResponse.ok();
    }
}
