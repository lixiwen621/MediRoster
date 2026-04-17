package com.mediroster.service;

import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.CalendarDayUpsertRequest;
import com.mediroster.dto.response.CalendarDayResponse;
import com.mediroster.entity.MedirCalendarDay;
import com.mediroster.mapper.MedirCalendarDayMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日历日（节假日等）。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirCalendarDayService {


    private final MedirCalendarDayMapper calendarDayMapper;

    public List<CalendarDayResponse> listByRange(LocalDate from, LocalDate to) {
        return calendarDayMapper.findByRange(from, to).stream().map(this::toResponse).toList();
    }

    public CalendarDayResponse getById(Long id) {
        MedirCalendarDay d = I18nPreconditions.checkNotNull(
                calendarDayMapper.findById(id), NOT_FOUND, "error.calendar.notFound");
        return toResponse(d);
    }

    @Transactional
    public CalendarDayResponse create(CalendarDayUpsertRequest req) {
        MedirCalendarDay exist = calendarDayMapper.findByCalDate(req.calDate());
        I18nPreconditions.checkArgument(exist == null, CONFLICT, "error.calendar.dateExists");
        MedirCalendarDay d = new MedirCalendarDay();
        d.setCalDate(req.calDate());
        d.setDayType(req.dayType());
        d.setHolidayName(req.holidayName());
        calendarDayMapper.insert(d);
        return toResponse(calendarDayMapper.findById(d.getId()));
    }

    @Transactional
    public CalendarDayResponse update(Long id, CalendarDayUpsertRequest req) {
        MedirCalendarDay d = I18nPreconditions.checkNotNull(
                calendarDayMapper.findById(id), NOT_FOUND, "error.calendar.notFound");
        d.setCalDate(req.calDate());
        d.setDayType(req.dayType());
        d.setHolidayName(req.holidayName());
        calendarDayMapper.updateById(d);
        return toResponse(calendarDayMapper.findById(id));
    }

    @Transactional
    public void delete(Long id) {
        I18nPreconditions.checkNotNull(calendarDayMapper.findById(id), NOT_FOUND, "error.calendar.notFound");
        calendarDayMapper.deleteById(id);
    }

    private CalendarDayResponse toResponse(MedirCalendarDay d) {
        return new CalendarDayResponse(
                d.getId(), d.getCalDate(), d.getDayType(), d.getHolidayName(),
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
