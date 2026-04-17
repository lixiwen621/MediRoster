package com.mediroster.service;

import com.mediroster.dto.response.RuleMetaResponse;
import com.mediroster.entity.MedirRuleMeta;
import com.mediroster.mapper.MedirRuleMetaMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 规则元数据（配置页表单）。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirRuleMetaService {

    private final MedirRuleMetaMapper ruleMetaMapper;

    public List<RuleMetaResponse> listForUi() {
        return ruleMetaMapper.findAllForUi().stream().map(this::toResponse).toList();
    }

    private RuleMetaResponse toResponse(MedirRuleMeta m) {
        return new RuleMetaResponse(
                m.getId(), m.getRuleCode(), m.getCategory(), m.getLabelZh(), m.getValueType(),
                m.getDefaultValue(), m.getOptionsJson(), m.getHelpText(), m.getSortOrder(), m.getEnabled(),
                m.getCreatedAt(), m.getUpdatedAt());
    }
}
