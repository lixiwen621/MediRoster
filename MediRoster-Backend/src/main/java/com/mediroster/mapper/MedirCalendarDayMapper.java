package com.mediroster.mapper;

import com.mediroster.entity.MedirCalendarDay;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_calendar_day。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirCalendarDayMapper {

    int insert(MedirCalendarDay row);

    int updateById(MedirCalendarDay row);

    int deleteById(@Param("id") Long id);

    MedirCalendarDay findById(@Param("id") Long id);

    MedirCalendarDay findByCalDate(@Param("calDate") LocalDate calDate);

    List<MedirCalendarDay> findByRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
