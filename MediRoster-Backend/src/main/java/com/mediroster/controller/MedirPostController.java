package com.mediroster.controller;

import com.mediroster.common.api.ApiResponse;
import com.mediroster.dto.request.PostUpsertRequest;
import com.mediroster.dto.response.PostResponse;
import com.mediroster.service.MedirPostService;
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
 * 岗位字典 API。
 *
 * @author tongguo.li
 */
@Tag(name = "岗位")
@RestController
@RequestMapping("/api/v1/medir/posts")
@RequiredArgsConstructor
public class MedirPostController {

    private final MedirPostService postService;

    @GetMapping
    @Operation(summary = "岗位列表")
    public ApiResponse<List<PostResponse>> list() {
        return ApiResponse.ok(postService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "岗位详情")
    public ApiResponse<PostResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(postService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建岗位")
    public ApiResponse<PostResponse> create(@Valid @RequestBody PostUpsertRequest req) {
        return ApiResponse.ok(postService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新岗位")
    public ApiResponse<PostResponse> update(@PathVariable Long id, @Valid @RequestBody PostUpsertRequest req) {
        return ApiResponse.ok(postService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除岗位")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.ok();
    }
}
