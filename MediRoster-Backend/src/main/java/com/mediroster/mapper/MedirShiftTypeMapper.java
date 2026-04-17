package com.mediroster.mapper;

import com.mediroster.entity.MedirShiftType;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_shift_type。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirShiftTypeMapper {

    int insert(MedirShiftType row);

    int updateById(MedirShiftType row);

    MedirShiftType findById(@Param("id") Long id);

    MedirShiftType findByTypeCode(@Param("typeCode") String typeCode);

    List<MedirShiftType> findAll();
}
