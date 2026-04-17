package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.response.RuleMetaResponse;
import com.mediroster.service.MedirRuleMetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 规则元数据（配置页渲染）。
 *
 * @author tongguo.li
 */
@Tag(name = "规则元数据")
@RestController
@RequestMapping("/api/v1/medir/rule-meta")
@RequiredArgsConstructor
public class MedirRuleMetaController {

    private final MedirRuleMetaService ruleMetaService;

    @GetMapping
    @Operation(summary = "规则表单定义列表")
    public ApiResponse<List<RuleMetaResponse>> list() {
        return ApiResponse.ok(ruleMetaService.listForUi());
    }
}
