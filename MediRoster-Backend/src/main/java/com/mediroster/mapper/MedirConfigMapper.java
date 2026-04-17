package com.mediroster.mapper;

import com.mediroster.entity.MedirConfig;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_config。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirConfigMapper {

    int insert(MedirConfig row);

    int updateById(MedirConfig row);

    int deleteById(@Param("id") Long id);

    MedirConfig findById(@Param("id") Long id);

    List<MedirConfig> findAll();

    List<MedirConfig> findByTeamId(@Param("teamId") Long teamId);

    MedirConfig findByTeamIdAndConfigKey(@Param("teamId") Long teamId, @Param("configKey") String configKey);
}
