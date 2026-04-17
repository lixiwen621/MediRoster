package com.mediroster.service;

import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.ConfigUpsertRequest;
import com.mediroster.dto.response.ConfigResponse;
import com.mediroster.entity.MedirConfig;
import com.mediroster.mapper.MedirConfigMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 键值规则配置（medir_config）。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirConfigService {


    private final MedirConfigMapper configMapper;

    /**
     * 列出配置；{@code teamId} 为 {@code null} 时返回全部（含各班组与 teamId=0 全局）。
     */
    public List<ConfigResponse> list(Long teamId) {
        List<MedirConfig> rows =
                teamId == null ? configMapper.findAll() : configMapper.findByTeamId(teamId);
        return rows.stream().map(this::toResponse).toList();
    }

    public ConfigResponse getById(Long id) {
        MedirConfig c = I18nPreconditions.checkNotNull(
                configMapper.findById(id), NOT_FOUND, "error.config.notFound");
        return toResponse(c);
    }

    @Transactional
    public ConfigResponse create(ConfigUpsertRequest req) {
        MedirConfig exist = configMapper.findByTeamIdAndConfigKey(req.teamId(), req.configKey());
        I18nPreconditions.checkArgument(exist == null, CONFLICT, "error.config.keyExists");
        MedirConfig c = new MedirConfig();
        apply(c, req);
        configMapper.insert(c);
        return toResponse(configMapper.findById(c.getId()));
    }

    @Transactional
    public ConfigResponse update(Long id, ConfigUpsertRequest req) {
        MedirConfig c = I18nPreconditions.checkNotNull(
                configMapper.findById(id), NOT_FOUND, "error.config.notFound");
        MedirConfig other = configMapper.findByTeamIdAndConfigKey(req.teamId(), req.configKey());
        I18nPreconditions.checkArgument(
                other == null || other.getId().equals(id), CONFLICT, "error.config.keyConflict");
        apply(c, req);
        configMapper.updateById(c);
        return toResponse(configMapper.findById(id));
    }

    @Transactional
    public void delete(Long id) {
        I18nPreconditions.checkNotNull(configMapper.findById(id), NOT_FOUND, "error.config.notFound");
        configMapper.deleteById(id);
    }

    private void apply(MedirConfig c, ConfigUpsertRequest req) {
        c.setTeamId(req.teamId());
        c.setConfigKey(req.configKey());
        c.setConfigValue(req.configValue());
        c.setValueType(req.valueType());
        c.setCategory(req.category());
        c.setDescription(req.description());
        c.setSortOrder(req.sortOrder());
        c.setEnabled(req.enabled());
    }

    private ConfigResponse toResponse(MedirConfig c) {
        return new ConfigResponse(
                c.getId(), c.getTeamId(), c.getConfigKey(), c.getConfigValue(), c.getValueType(),
                c.getCategory(), c.getDescription(), c.getSortOrder(), c.getEnabled(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
