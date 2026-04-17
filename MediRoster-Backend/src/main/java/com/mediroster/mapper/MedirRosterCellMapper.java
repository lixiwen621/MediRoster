package com.mediroster.mapper;

import com.mediroster.entity.MedirRosterCell;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_roster_cell。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirRosterCellMapper {

    int deleteByRosterWeekId(@Param("rosterWeekId") Long rosterWeekId);

    int insertBatch(@Param("list") List<MedirRosterCell> list);

    List<MedirRosterCell> findByRosterWeekId(@Param("rosterWeekId") Long rosterWeekId);
}
