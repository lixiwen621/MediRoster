package com.mediroster.service;

import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.ShiftTypeUpdateRequest;
import com.mediroster.dto.response.ShiftTypeResponse;
import com.mediroster.entity.MedirShiftType;
import com.mediroster.mapper.MedirShiftTypeMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 班次类型。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirShiftTypeService {


    private final MedirShiftTypeMapper shiftTypeMapper;

    public List<ShiftTypeResponse> listAll() {
        return shiftTypeMapper.findAll().stream().map(this::toResponse).toList();
    }

    public ShiftTypeResponse getById(Long id) {
        MedirShiftType s = I18nPreconditions.checkNotNull(
                shiftTypeMapper.findById(id), NOT_FOUND, "error.shiftType.notFound");
        return toResponse(s);
    }

    @Transactional
    public ShiftTypeResponse update(Long id, ShiftTypeUpdateRequest req) {
        MedirShiftType s = I18nPreconditions.checkNotNull(
                shiftTypeMapper.findById(id), NOT_FOUND, "error.shiftType.notFound");
        s.setTypeCode(req.typeCode());
        s.setNameZh(req.nameZh());
        s.setSortOrder(req.sortOrder());
        s.setIsRest(req.isRest());
        s.setIsDutyZhong(req.isDutyZhong());
        s.setIsDutyDa(req.isDutyDa());
        s.setIsQiban(req.isQiban());
        s.setIsSmallNight(req.isSmallNight());
        s.setCountsDaytimeHeadcount(req.countsDaytimeHeadcount());
        s.setCountsWeekendFullDayStat(req.countsWeekendFullDayStat());
        s.setCountsAsZhongForStructure(req.countsAsZhongForStructure());
        s.setCountsAsLinForStructure(req.countsAsLinForStructure());
        s.setNextDayMustRest(req.nextDayMustRest());
        s.setEnabled(req.enabled());
        shiftTypeMapper.updateById(s);
        return toResponse(shiftTypeMapper.findById(id));
    }

    private ShiftTypeResponse toResponse(MedirShiftType s) {
        return new ShiftTypeResponse(
                s.getId(), s.getTypeCode(), s.getNameZh(), s.getSortOrder(),
                s.getIsRest(), s.getIsDutyZhong(), s.getIsDutyDa(), s.getIsQiban(), s.getIsSmallNight(),
                s.getCountsDaytimeHeadcount(), s.getCountsWeekendFullDayStat(),
                s.getCountsAsZhongForStructure(), s.getCountsAsLinForStructure(),
                s.getNextDayMustRest(), s.getEnabled(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
