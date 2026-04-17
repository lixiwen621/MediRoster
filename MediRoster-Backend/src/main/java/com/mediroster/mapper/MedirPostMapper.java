package com.mediroster.mapper;

import com.mediroster.entity.MedirPost;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * medir_post。
 *
 * @author tongguo.li
 */
@Mapper
public interface MedirPostMapper {

    int insert(MedirPost row);

    int updateById(MedirPost row);

    int deleteById(@Param("id") Long id);

    MedirPost findById(@Param("id") Long id);

    MedirPost findByPostCode(@Param("postCode") String postCode);

    List<MedirPost> findAll();
}
