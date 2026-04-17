package com.mediroster.mapper;

import com.mediroster.entity.MedirRosterWeekWeekendStat;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_roster_weekend_stat。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirRosterWeekWeekendStatMapper {

    List<MedirRosterWeekWeekendStat> findByRosterWeekId(@Param("rosterWeekId") Long rosterWeekId);

    int upsert(MedirRosterWeekWeekendStat row);

    int deleteByWeekAndStaff(@Param("rosterWeekId") Long rosterWeekId, @Param("staffId") Long staffId);
}
