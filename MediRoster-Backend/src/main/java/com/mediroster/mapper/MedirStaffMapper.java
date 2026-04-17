package com.mediroster.mapper;

import com.mediroster.entity.MedirStaff;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_staff。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirStaffMapper {

    int insert(MedirStaff row);

    int updateById(MedirStaff row);

    int softDeleteById(@Param("id") Long id);

    MedirStaff findById(@Param("id") Long id);

    List<MedirStaff> findByTeamId(@Param("teamId") Long teamId, @Param("includeDeleted") boolean includeDeleted);
}
