package com.mediroster.mapper;

import com.mediroster.entity.MedirTeam;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_team。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirTeamMapper {

    int insert(MedirTeam row);

    int updateById(MedirTeam row);

    int deleteById(@Param("id") Long id);

    MedirTeam findById(@Param("id") Long id);

    MedirTeam findByTeamCode(@Param("teamCode") String teamCode);

    List<MedirTeam> findAll();
}
