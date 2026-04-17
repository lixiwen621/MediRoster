package com.mediroster.mapper;

import com.mediroster.entity.MedirRuleMeta;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * medir_rule_meta。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirRuleMetaMapper {

    List<MedirRuleMeta> findAllForUi();
}
