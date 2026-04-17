package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.RosterCellReplaceRequest;
import com.mediroster.dto.request.RosterStaffPostReplaceRequest;
import com.mediroster.dto.request.RosterWeekGenerateRequest;
import com.mediroster.dto.request.RosterWeekWeekendStatReplaceRequest;
import com.mediroster.dto.request.RosterWeekCreateRequest;
import com.mediroster.dto.request.RosterWeekUpdateRequest;
import com.mediroster.dto.response.RosterCellResponse;
import com.mediroster.dto.response.RosterWeekExcelExportResult;
import com.mediroster.dto.response.RosterWeekGenerateResponse;
import com.mediroster.dto.response.RosterWeekResponse;
import com.mediroster.dto.response.RosterWeekStaffPostResponse;
import com.mediroster.dto.response.RosterWeekWeekendStatResponse;
import com.mediroster.service.MedirRosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * 排班周、单元格、周岗位摘要。
 *
 * @author tongguo.li
 */
@Tag(name = "排班周")
@RestController
@RequestMapping("/api/v1/medir/roster-weeks")
@RequiredArgsConstructor
public class MedirRosterWeekController {

    private final MedirRosterService rosterService;

    @GetMapping
    @Operation(summary = "按班组与年份列出排班周")
    public ApiResponse<List<RosterWeekResponse>> list(
            @RequestParam Long teamId,
            @RequestParam Integer year) {
        return ApiResponse.ok(rosterService.listByTeamAndYear(teamId, year));
    }

    @GetMapping("/{id}")
    @Operation(summary = "排班周详情")
    public ApiResponse<RosterWeekResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(rosterService.getWeek(id));
    }

    @PostMapping
    @Operation(summary = "创建排班周（周一起算）")
    public ApiResponse<RosterWeekResponse> create(@Valid @RequestBody RosterWeekCreateRequest req) {
        return ApiResponse.ok(rosterService.createWeek(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新排班周（携带 version 乐观锁）")
    public ApiResponse<RosterWeekResponse> update(@PathVariable Long id, @Valid @RequestBody RosterWeekUpdateRequest req) {
        return ApiResponse.ok(rosterService.updateWeek(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除排班周及下属单元格、周岗位摘要")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        rosterService.deleteWeek(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{weekId}/cells")
    @Operation(summary = "周单元格列表")
    public ApiResponse<List<RosterCellResponse>> listCells(@PathVariable Long weekId) {
        return ApiResponse.ok(rosterService.listCells(weekId));
    }

    @PutMapping("/{weekId}/cells")
    @Operation(summary = "覆盖整周单元格（先删后插）")
    public ApiResponse<Void> replaceCells(@PathVariable Long weekId, @Valid @RequestBody RosterCellReplaceRequest req) {
        rosterService.replaceCells(weekId, req);
        return ApiResponse.ok();
    }

    @PostMapping("/{weekId}/generate")
    @Operation(summary = "自动生成排班（可仅补未确认）")
    public ApiResponse<RosterWeekGenerateResponse> generate(
            @PathVariable Long weekId,
            @Valid @RequestBody(required = false) RosterWeekGenerateRequest req) {
        return ApiResponse.ok(rosterService.generateWeek(weekId, req));
    }

    @GetMapping(value = "/{weekId}/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "导出当周排班 Excel（.xlsx，A4 纵向）")
    public ResponseEntity<byte[]> exportWeek(
            @PathVariable Long weekId,
            @RequestParam(required = false) String filename) {
        RosterWeekExcelExportResult result = rosterService.exportWeekExcel(weekId, filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(result.downloadFilename()))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(result.content());
    }

    private String buildContentDisposition(String utf8Filename) {
        String safeAscii = toAsciiFilename(utf8Filename);
        String encoded = URLEncoder.encode(utf8Filename, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + safeAscii + "\"; filename*=UTF-8''" + encoded;
    }

    private String toAsciiFilename(String input) {
        String source = (input == null || input.isBlank()) ? "roster.xlsx" : input.trim();
        int dot = source.lastIndexOf('.');
        String base = dot > 0 ? source.substring(0, dot) : source;
        String ext = dot > 0 ? source.substring(dot).toLowerCase() : ".xlsx";
        if (!".xlsx".equals(ext)) {
            ext = ".xlsx";
        }
        String asciiBase = base.replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (asciiBase.isBlank()) {
            asciiBase = "roster";
        }
        return asciiBase + ext;
    }

    @GetMapping("/{weekId}/staff-posts")
    @Operation(summary = "周岗位摘要列表")
    public ApiResponse<List<RosterWeekStaffPostResponse>> listStaffPosts(@PathVariable Long weekId) {
        return ApiResponse.ok(rosterService.listStaffPosts(weekId));
    }

    @PutMapping("/{weekId}/staff-posts")
    @Operation(summary = "覆盖周岗位摘要")
    public ApiResponse<Void> replaceStaffPosts(
            @PathVariable Long weekId,
            @Valid @RequestBody RosterStaffPostReplaceRequest req) {
        rosterService.replaceStaffPosts(weekId, req);
        return ApiResponse.ok();
    }

    @GetMapping("/{weekId}/weekend-stats")
    @Operation(summary = "周末统计两列列表（自动值与最终值）")
    public ApiResponse<List<RosterWeekWeekendStatResponse>> listWeekendStats(@PathVariable Long weekId) {
        return ApiResponse.ok(rosterService.listWeekendStats(weekId));
    }

    @PutMapping("/{weekId}/weekend-stats")
    @Operation(summary = "覆盖周末统计两列")
    public ApiResponse<Void> replaceWeekendStats(
            @PathVariable Long weekId,
            @Valid @RequestBody RosterWeekWeekendStatReplaceRequest req) {
        rosterService.replaceWeekendStats(weekId, req);
        return ApiResponse.ok();
    }
}
