package com.mediroster.mapper;

import com.mediroster.entity.MedirRosterWeek;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_roster_week。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirRosterWeekMapper {

    int insert(MedirRosterWeek row);

    int updateById(MedirRosterWeek row);

    int deleteById(@Param("id") Long id);

    MedirRosterWeek findById(@Param("id") Long id);

    MedirRosterWeek findByTeamAndWeekStart(@Param("teamId") Long teamId, @Param("weekStartDate") LocalDate weekStartDate);

    List<MedirRosterWeek> findByTeamAndYear(@Param("teamId") Long teamId, @Param("yearLabel") Integer yearLabel);
}
