package com.mediroster.mapper;

import com.mediroster.entity.MedirStaffCapability;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_staff_capability。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirStaffCapabilityMapper {

    int insert(MedirStaffCapability row);

    int deleteById(@Param("id") Long id);

    int deleteByStaffAndCode(@Param("staffId") Long staffId, @Param("capabilityCode") String capabilityCode);

    MedirStaffCapability findById(@Param("id") Long id);

    List<MedirStaffCapability> findByStaffId(@Param("staffId") Long staffId);
}
