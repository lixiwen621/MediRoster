package com.mediroster.service;

import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.PostUpsertRequest;
import com.mediroster.dto.response.PostResponse;
import com.mediroster.entity.MedirPost;
import com.mediroster.mapper.MedirPostMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 岗位字典。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirPostService {


    private final MedirPostMapper postMapper;

    public List<PostResponse> listAll() {
        return postMapper.findAll().stream().map(this::toResponse).toList();
    }

    public PostResponse getById(Long id) {
        MedirPost p = I18nPreconditions.checkNotNull(postMapper.findById(id), NOT_FOUND, "error.post.notFound");
        return toResponse(p);
    }

    @Transactional
    public PostResponse create(PostUpsertRequest req) {
        MedirPost p = new MedirPost();
        apply(p, req);
        postMapper.insert(p);
        return toResponse(postMapper.findById(p.getId()));
    }

    @Transactional
    public PostResponse update(Long id, PostUpsertRequest req) {
        MedirPost p = I18nPreconditions.checkNotNull(postMapper.findById(id), NOT_FOUND, "error.post.notFound");
        apply(p, req);
        postMapper.updateById(p);
        return toResponse(postMapper.findById(id));
    }

    @Transactional
    public void delete(Long id) {
        I18nPreconditions.checkNotNull(postMapper.findById(id), NOT_FOUND, "error.post.notFound");
        postMapper.deleteById(id);
    }

    private void apply(MedirPost p, PostUpsertRequest req) {
        p.setPostCode(req.postCode());
        p.setPostName(req.postName());
        p.setDescription(req.description());
        p.setSortOrder(req.sortOrder());
        p.setEnabled(req.enabled());
    }

    private PostResponse toResponse(MedirPost p) {
        return new PostResponse(
                p.getId(), p.getPostCode(), p.getPostName(), p.getDescription(),
                p.getSortOrder(), p.getEnabled(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
