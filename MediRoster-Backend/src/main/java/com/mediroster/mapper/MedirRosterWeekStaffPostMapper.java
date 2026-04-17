package com.mediroster.mapper;

import com.mediroster.entity.MedirRosterWeekStaffPost;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_roster_week_staff_post。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirRosterWeekStaffPostMapper {

    int deleteByRosterWeekId(@Param("rosterWeekId") Long rosterWeekId);

    int insertBatch(@Param("list") List<MedirRosterWeekStaffPost> list);

    List<MedirRosterWeekStaffPost> findByRosterWeekId(@Param("rosterWeekId") Long rosterWeekId);
}
