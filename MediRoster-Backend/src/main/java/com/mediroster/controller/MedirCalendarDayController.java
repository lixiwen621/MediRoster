package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.CalendarDayUpsertRequest;
import com.mediroster.dto.response.CalendarDayResponse;
import com.mediroster.service.MedirCalendarDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
 * 日历日 API。
 *
 * @author tongguo.li
 */
@Tag(name = "日历日")
@RestController
@RequestMapping("/api/v1/medir/calendar-days")
@RequiredArgsConstructor
public class MedirCalendarDayController {

    private final MedirCalendarDayService calendarDayService;

    @GetMapping
    @Operation(summary = "按日期区间查询")
    public ApiResponse<List<CalendarDayResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(calendarDayService.listByRange(from, to));
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public ApiResponse<CalendarDayResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(calendarDayService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增")
    public ApiResponse<CalendarDayResponse> create(@Valid @RequestBody CalendarDayUpsertRequest req) {
        return ApiResponse.ok(calendarDayService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新")
    public ApiResponse<CalendarDayResponse> update(@PathVariable Long id, @Valid @RequestBody CalendarDayUpsertRequest req) {
        return ApiResponse.ok(calendarDayService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        calendarDayService.delete(id);
        return ApiResponse.ok();
    }
}
