package com.mediroster.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mediroster.dto.request.RosterWeekGenerateRequest;
import com.mediroster.dto.request.RosterWeekWeekendStatReplaceRequest;
import com.mediroster.dto.response.RosterWeekGenerateResponse;
import com.mediroster.dto.response.RosterWeekWeekendStatResponse;
import com.mediroster.entity.MedirRosterCell;
import com.mediroster.entity.MedirRosterWeek;
import com.mediroster.entity.MedirRosterWeekWeekendStat;
import com.mediroster.entity.MedirShiftType;
import com.mediroster.entity.MedirStaff;
import com.mediroster.mapper.MedirConfigMapper;
import com.mediroster.mapper.MedirRosterCellMapper;
import com.mediroster.mapper.MedirRosterWeekMapper;
import com.mediroster.mapper.MedirRosterWeekStaffPostMapper;
import com.mediroster.mapper.MedirRosterWeekWeekendStatMapper;
import com.mediroster.mapper.MedirShiftTypeMapper;
import com.mediroster.mapper.MedirStaffMapper;
import com.mediroster.mapper.MedirTeamMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * 周末统计两列逻辑测试。
 *
 * @author tongguo.li
 */
class MedirRosterServiceWeekendStatTest {

    @Test
    void shouldMergeAutoStatsAndOverrides() {
        MedirRosterWeekMapper weekMapper = Mockito.mock(MedirRosterWeekMapper.class);
        MedirRosterCellMapper cellMapper = Mockito.mock(MedirRosterCellMapper.class);
        MedirRosterWeekStaffPostMapper staffPostMapper = Mockito.mock(MedirRosterWeekStaffPostMapper.class);
        MedirRosterWeekWeekendStatMapper weekendStatMapper = Mockito.mock(MedirRosterWeekWeekendStatMapper.class);
        MedirStaffMapper staffMapper = Mockito.mock(MedirStaffMapper.class);
        MedirShiftTypeMapper shiftTypeMapper = Mockito.mock(MedirShiftTypeMapper.class);
        MedirConfigMapper configMapper = Mockito.mock(MedirConfigMapper.class);
        MedirTeamMapper teamMapper = Mockito.mock(MedirTeamMapper.class);
        MedirRosterService service = new MedirRosterService(
                weekMapper, cellMapper, staffPostMapper, weekendStatMapper, staffMapper, shiftTypeMapper, configMapper,
                teamMapper);

        MedirRosterWeek week = new MedirRosterWeek();
        week.setId(1L);
        week.setTeamId(1L);
        week.setWeekStartDate(LocalDate.of(2026, 4, 13));
        when(weekMapper.findById(1L)).thenReturn(week);

        MedirStaff s1 = new MedirStaff();
        s1.setId(101L);
        s1.setTeamId(1L);
        MedirStaff s2 = new MedirStaff();
        s2.setId(102L);
        s2.setTeamId(1L);
        when(staffMapper.findByTeamId(1L, false)).thenReturn(List.of(s1, s2));

        MedirShiftType lin = new MedirShiftType();
        lin.setId(11L);
        lin.setTypeCode("LIN");
        lin.setCountsWeekendFullDayStat(1);
        MedirShiftType zhong = new MedirShiftType();
        zhong.setId(12L);
        zhong.setTypeCode("ZHONG");
        zhong.setCountsWeekendFullDayStat(0);
        when(shiftTypeMapper.findAll()).thenReturn(List.of(lin, zhong));

        MedirRosterCell c1 = new MedirRosterCell();
        c1.setRosterWeekId(1L);
        c1.setStaffId(101L);
        c1.setWorkDate(LocalDate.of(2026, 4, 18));
        c1.setShiftTypeId(11L);
        MedirRosterCell c2 = new MedirRosterCell();
        c2.setRosterWeekId(1L);
        c2.setStaffId(101L);
        c2.setWorkDate(LocalDate.of(2026, 4, 19));
        c2.setShiftTypeId(12L);
        MedirRosterCell c3 = new MedirRosterCell();
        c3.setRosterWeekId(1L);
        c3.setStaffId(102L);
        c3.setWorkDate(LocalDate.of(2026, 4, 18));
        c3.setShiftTypeId(12L);
        MedirRosterCell c4 = new MedirRosterCell();
        c4.setRosterWeekId(1L);
        c4.setStaffId(102L);
        c4.setWorkDate(LocalDate.of(2026, 4, 19));
        c4.setShiftTypeId(12L);
        when(cellMapper.findByRosterWeekId(1L)).thenReturn(List.of(c1, c2, c3, c4));

        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("stats.weekend_full_shift_types"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("stats.weekend_full_shift_types"))).thenReturn(null);

        MedirRosterWeekWeekendStat override = new MedirRosterWeekWeekendStat();
        override.setId(9001L);
        override.setRosterWeekId(1L);
        override.setStaffId(101L);
        override.setWeekendFullOverride(2);
        override.setLastWeekendOverride(null);
        override.setOverrideReason("manual");
        override.setUpdatedAt(LocalDateTime.of(2026, 4, 12, 10, 0));
        when(weekendStatMapper.findByRosterWeekId(1L)).thenReturn(List.of(override));

        List<RosterWeekWeekendStatResponse> rows = service.listWeekendStats(1L);
        assertThat(rows).hasSize(2);

        RosterWeekWeekendStatResponse r1 = rows.get(0);
        assertThat(r1.staffId()).isEqualTo(101L);
        assertThat(r1.weekendFullAuto()).isEqualTo(1);
        assertThat(r1.weekendFullFinal()).isEqualTo(2);
        assertThat(r1.lastWeekendAuto()).isEqualTo(0);
        assertThat(r1.lastWeekendFinal()).isEqualTo(0);
        assertThat(r1.isOverridden()).isEqualTo(1);

        RosterWeekWeekendStatResponse r2 = rows.get(1);
        assertThat(r2.staffId()).isEqualTo(102L);
        assertThat(r2.weekendFullAuto()).isEqualTo(0);
        assertThat(r2.weekendFullFinal()).isEqualTo(0);
        assertThat(r2.lastWeekendAuto()).isEqualTo(1);
        assertThat(r2.lastWeekendFinal()).isEqualTo(1);
        assertThat(r2.isOverridden()).isEqualTo(0);
    }

    @Test
    void shouldDeleteOverrideWhenAllFieldsNull() {
        MedirRosterWeekMapper weekMapper = Mockito.mock(MedirRosterWeekMapper.class);
        MedirRosterCellMapper cellMapper = Mockito.mock(MedirRosterCellMapper.class);
        MedirRosterWeekStaffPostMapper staffPostMapper = Mockito.mock(MedirRosterWeekStaffPostMapper.class);
        MedirRosterWeekWeekendStatMapper weekendStatMapper = Mockito.mock(MedirRosterWeekWeekendStatMapper.class);
        MedirStaffMapper staffMapper = Mockito.mock(MedirStaffMapper.class);
        MedirShiftTypeMapper shiftTypeMapper = Mockito.mock(MedirShiftTypeMapper.class);
        MedirConfigMapper configMapper = Mockito.mock(MedirConfigMapper.class);
        MedirTeamMapper teamMapper = Mockito.mock(MedirTeamMapper.class);
        MedirRosterService service = new MedirRosterService(
                weekMapper, cellMapper, staffPostMapper, weekendStatMapper, staffMapper, shiftTypeMapper, configMapper,
                teamMapper);

        MedirRosterWeek week = new MedirRosterWeek();
        week.setId(1L);
        week.setTeamId(1L);
        when(weekMapper.findById(1L)).thenReturn(week);
        MedirStaff staff = new MedirStaff();
        staff.setId(101L);
        staff.setTeamId(1L);
        when(staffMapper.findByTeamId(1L, false)).thenReturn(List.of(staff));

        RosterWeekWeekendStatReplaceRequest req = new RosterWeekWeekendStatReplaceRequest(List.of(
                new RosterWeekWeekendStatReplaceRequest.Item(101L, null, null, null)));
        service.replaceWeekendStats(1L, req);

        verify(weekendStatMapper).deleteByWeekAndStaff(1L, 101L);
        verify(weekendStatMapper, never()).upsert(Mockito.any());
        verify(configMapper, never()).findByTeamIdAndConfigKey(anyLong(), eq("stats.weekend_full_shift_types"));
    }

    @Test
    void shouldGenerateOnlyUnconfirmedCellsByDefault() {
        MedirRosterWeekMapper weekMapper = Mockito.mock(MedirRosterWeekMapper.class);
        MedirRosterCellMapper cellMapper = Mockito.mock(MedirRosterCellMapper.class);
        MedirRosterWeekStaffPostMapper staffPostMapper = Mockito.mock(MedirRosterWeekStaffPostMapper.class);
        MedirRosterWeekWeekendStatMapper weekendStatMapper = Mockito.mock(MedirRosterWeekWeekendStatMapper.class);
        MedirStaffMapper staffMapper = Mockito.mock(MedirStaffMapper.class);
        MedirShiftTypeMapper shiftTypeMapper = Mockito.mock(MedirShiftTypeMapper.class);
        MedirConfigMapper configMapper = Mockito.mock(MedirConfigMapper.class);
        MedirTeamMapper teamMapper = Mockito.mock(MedirTeamMapper.class);
        MedirRosterService service = new MedirRosterService(
                weekMapper, cellMapper, staffPostMapper, weekendStatMapper, staffMapper, shiftTypeMapper, configMapper,
                teamMapper);

        MedirRosterWeek week = new MedirRosterWeek();
        week.setId(1L);
        week.setTeamId(1L);
        week.setWeekStartDate(LocalDate.of(2026, 4, 13));
        when(weekMapper.findById(1L)).thenReturn(week);

        MedirStaff s1 = new MedirStaff();
        s1.setId(101L);
        s1.setTeamId(1L);
        s1.setSortOrder(1);
        MedirStaff s2 = new MedirStaff();
        s2.setId(102L);
        s2.setTeamId(1L);
        s2.setSortOrder(2);
        when(staffMapper.findByTeamId(1L, false)).thenReturn(List.of(s1, s2));

        MedirShiftType xiu = new MedirShiftType();
        xiu.setId(10L);
        xiu.setTypeCode("XIU");
        xiu.setCountsDaytimeHeadcount(0);
        xiu.setCountsAsZhongForStructure(0);
        xiu.setCountsAsLinForStructure(0);
        MedirShiftType zhong = new MedirShiftType();
        zhong.setId(11L);
        zhong.setTypeCode("ZHONG");
        zhong.setCountsDaytimeHeadcount(1);
        zhong.setCountsAsZhongForStructure(1);
        zhong.setCountsAsLinForStructure(0);
        MedirShiftType lin = new MedirShiftType();
        lin.setId(12L);
        lin.setTypeCode("LIN");
        lin.setCountsDaytimeHeadcount(1);
        lin.setCountsAsZhongForStructure(0);
        lin.setCountsAsLinForStructure(1);
        when(shiftTypeMapper.findAll()).thenReturn(List.of(xiu, zhong, lin));

        MedirRosterCell confirmed = new MedirRosterCell();
        confirmed.setRosterWeekId(1L);
        confirmed.setStaffId(101L);
        confirmed.setWorkDate(LocalDate.of(2026, 4, 13));
        confirmed.setShiftTypeId(11L);
        confirmed.setValidationExempt(0);
        when(cellMapper.findByRosterWeekId(1L)).thenReturn(List.of(confirmed));

        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_lin"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_lin"))).thenReturn(null);

        RosterWeekGenerateResponse response = service.generateWeek(1L, new RosterWeekGenerateRequest(null, null, null, "auto"));
        assertThat(response.generatedCellCount()).isEqualTo(13);
        assertThat(response.overwrittenCellCount()).isEqualTo(0);
        assertThat(response.skippedConfirmedCount()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedirRosterCell>> captor =
                (ArgumentCaptor<List<MedirRosterCell>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(cellMapper).insertBatch(captor.capture());
        List<MedirRosterCell> saved = captor.getValue();
        assertThat(saved).hasSize(14);
        MedirRosterCell mondayS1 = saved.stream()
                .filter(c -> c.getStaffId().equals(101L) && c.getWorkDate().equals(LocalDate.of(2026, 4, 13)))
                .findFirst()
                .orElseThrow();
        assertThat(mondayS1.getShiftTypeId()).isEqualTo(11L);
    }

    @Test
    void shouldNotPersistWhenDryRun() {
        MedirRosterWeekMapper weekMapper = Mockito.mock(MedirRosterWeekMapper.class);
        MedirRosterCellMapper cellMapper = Mockito.mock(MedirRosterCellMapper.class);
        MedirRosterWeekStaffPostMapper staffPostMapper = Mockito.mock(MedirRosterWeekStaffPostMapper.class);
        MedirRosterWeekWeekendStatMapper weekendStatMapper = Mockito.mock(MedirRosterWeekWeekendStatMapper.class);
        MedirStaffMapper staffMapper = Mockito.mock(MedirStaffMapper.class);
        MedirShiftTypeMapper shiftTypeMapper = Mockito.mock(MedirShiftTypeMapper.class);
        MedirConfigMapper configMapper = Mockito.mock(MedirConfigMapper.class);
        MedirTeamMapper teamMapper = Mockito.mock(MedirTeamMapper.class);
        MedirRosterService service = new MedirRosterService(
                weekMapper, cellMapper, staffPostMapper, weekendStatMapper, staffMapper, shiftTypeMapper, configMapper,
                teamMapper);

        MedirRosterWeek week = new MedirRosterWeek();
        week.setId(1L);
        week.setTeamId(1L);
        week.setWeekStartDate(LocalDate.of(2026, 4, 13));
        when(weekMapper.findById(1L)).thenReturn(week);

        MedirStaff s1 = new MedirStaff();
        s1.setId(101L);
        s1.setTeamId(1L);
        s1.setSortOrder(1);
        when(staffMapper.findByTeamId(1L, false)).thenReturn(List.of(s1));

        MedirShiftType xiu = new MedirShiftType();
        xiu.setId(10L);
        xiu.setTypeCode("XIU");
        xiu.setCountsDaytimeHeadcount(0);
        xiu.setCountsAsZhongForStructure(0);
        xiu.setCountsAsLinForStructure(0);
        MedirShiftType zhong = new MedirShiftType();
        zhong.setId(11L);
        zhong.setTypeCode("ZHONG");
        zhong.setCountsDaytimeHeadcount(1);
        zhong.setCountsAsZhongForStructure(1);
        zhong.setCountsAsLinForStructure(0);
        MedirShiftType lin = new MedirShiftType();
        lin.setId(12L);
        lin.setTypeCode("LIN");
        lin.setCountsDaytimeHeadcount(1);
        lin.setCountsAsZhongForStructure(0);
        lin.setCountsAsLinForStructure(1);
        when(shiftTypeMapper.findAll()).thenReturn(List.of(xiu, zhong, lin));
        when(cellMapper.findByRosterWeekId(1L)).thenReturn(List.of());

        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_lin"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_lin"))).thenReturn(null);

        RosterWeekGenerateResponse response = service.generateWeek(
                1L, new RosterWeekGenerateRequest("OVERWRITE_ALL", 0, 1, "preview"));
        assertThat(response.dryRun()).isEqualTo(1);
        verify(cellMapper, never()).deleteByRosterWeekId(1L);
        verify(cellMapper, never()).insertBatch(Mockito.any());
    }

    @Test
    void shouldNotAssignSamePersonAllZhongOrAllRestInWholeWeek() {
        MedirRosterWeekMapper weekMapper = Mockito.mock(MedirRosterWeekMapper.class);
        MedirRosterCellMapper cellMapper = Mockito.mock(MedirRosterCellMapper.class);
        MedirRosterWeekStaffPostMapper staffPostMapper = Mockito.mock(MedirRosterWeekStaffPostMapper.class);
        MedirRosterWeekWeekendStatMapper weekendStatMapper = Mockito.mock(MedirRosterWeekWeekendStatMapper.class);
        MedirStaffMapper staffMapper = Mockito.mock(MedirStaffMapper.class);
        MedirShiftTypeMapper shiftTypeMapper = Mockito.mock(MedirShiftTypeMapper.class);
        MedirConfigMapper configMapper = Mockito.mock(MedirConfigMapper.class);
        MedirTeamMapper teamMapper = Mockito.mock(MedirTeamMapper.class);
        MedirRosterService service = new MedirRosterService(
                weekMapper, cellMapper, staffPostMapper, weekendStatMapper, staffMapper, shiftTypeMapper, configMapper,
                teamMapper);

        MedirRosterWeek week = new MedirRosterWeek();
        week.setId(1L);
        week.setTeamId(1L);
        week.setWeekStartDate(LocalDate.of(2026, 4, 13));
        when(weekMapper.findById(1L)).thenReturn(week);

        List<MedirStaff> staffs = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            MedirStaff s = new MedirStaff();
            s.setId(100L + i);
            s.setTeamId(1L);
            s.setSortOrder(i);
            staffs.add(s);
        }
        when(staffMapper.findByTeamId(1L, false)).thenReturn(staffs);

        MedirShiftType xiu = new MedirShiftType();
        xiu.setId(10L);
        xiu.setTypeCode("XIU");
        xiu.setCountsDaytimeHeadcount(0);
        xiu.setCountsAsZhongForStructure(0);
        xiu.setCountsAsLinForStructure(0);
        MedirShiftType zhong = new MedirShiftType();
        zhong.setId(11L);
        zhong.setTypeCode("ZHONG");
        zhong.setCountsDaytimeHeadcount(1);
        zhong.setCountsAsZhongForStructure(1);
        zhong.setCountsAsLinForStructure(0);
        MedirShiftType lin = new MedirShiftType();
        lin.setId(12L);
        lin.setTypeCode("LIN");
        lin.setCountsDaytimeHeadcount(1);
        lin.setCountsAsZhongForStructure(0);
        lin.setCountsAsLinForStructure(1);
        when(shiftTypeMapper.findAll()).thenReturn(List.of(xiu, zhong, lin));
        when(cellMapper.findByRosterWeekId(1L)).thenReturn(List.of());

        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_lin"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_lin"))).thenReturn(null);

        service.generateWeek(1L, new RosterWeekGenerateRequest("OVERWRITE_ALL", 0, 0, "auto"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedirRosterCell>> captor =
                (ArgumentCaptor<List<MedirRosterCell>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(cellMapper).insertBatch(captor.capture());
        List<MedirRosterCell> saved = captor.getValue();
        assertThat(saved).hasSize(42);

        long firstAllZhongCount = saved.stream()
                .filter(c -> c.getStaffId().equals(101L) && c.getShiftTypeId().equals(11L))
                .count();
        long lastAllRestCount = saved.stream()
                .filter(c -> c.getStaffId().equals(106L) && c.getShiftTypeId().equals(10L))
                .count();
        assertThat(firstAllZhongCount).isLessThan(7);
        assertThat(lastAllRestCount).isLessThan(7);
    }

    @Test
    void shouldKeepAtLeastTwoRestDaysWhenPartialManualAssignmentsExist() {
        MedirRosterWeekMapper weekMapper = Mockito.mock(MedirRosterWeekMapper.class);
        MedirRosterCellMapper cellMapper = Mockito.mock(MedirRosterCellMapper.class);
        MedirRosterWeekStaffPostMapper staffPostMapper = Mockito.mock(MedirRosterWeekStaffPostMapper.class);
        MedirRosterWeekWeekendStatMapper weekendStatMapper = Mockito.mock(MedirRosterWeekWeekendStatMapper.class);
        MedirStaffMapper staffMapper = Mockito.mock(MedirStaffMapper.class);
        MedirShiftTypeMapper shiftTypeMapper = Mockito.mock(MedirShiftTypeMapper.class);
        MedirConfigMapper configMapper = Mockito.mock(MedirConfigMapper.class);
        MedirTeamMapper teamMapper = Mockito.mock(MedirTeamMapper.class);
        MedirRosterService service = new MedirRosterService(
                weekMapper, cellMapper, staffPostMapper, weekendStatMapper, staffMapper, shiftTypeMapper, configMapper,
                teamMapper);

        MedirRosterWeek week = new MedirRosterWeek();
        week.setId(1L);
        week.setTeamId(1L);
        week.setWeekStartDate(LocalDate.of(2026, 4, 13));
        when(weekMapper.findById(1L)).thenReturn(week);

        List<MedirStaff> staffs = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            MedirStaff s = new MedirStaff();
            s.setId(100L + i);
            s.setTeamId(1L);
            s.setSortOrder(i);
            staffs.add(s);
        }
        when(staffMapper.findByTeamId(1L, false)).thenReturn(staffs);

        MedirShiftType xiu = new MedirShiftType();
        xiu.setId(10L);
        xiu.setTypeCode("XIU");
        xiu.setIsRest(1);
        xiu.setCountsDaytimeHeadcount(0);
        xiu.setCountsAsZhongForStructure(0);
        xiu.setCountsAsLinForStructure(0);
        MedirShiftType zhong = new MedirShiftType();
        zhong.setId(11L);
        zhong.setTypeCode("ZHONG");
        zhong.setIsRest(0);
        zhong.setCountsDaytimeHeadcount(1);
        zhong.setCountsAsZhongForStructure(1);
        zhong.setCountsAsLinForStructure(0);
        MedirShiftType lin = new MedirShiftType();
        lin.setId(12L);
        lin.setTypeCode("LIN");
        lin.setIsRest(0);
        lin.setCountsDaytimeHeadcount(1);
        lin.setCountsAsZhongForStructure(0);
        lin.setCountsAsLinForStructure(1);
        MedirShiftType guisuiQuan = new MedirShiftType();
        guisuiQuan.setId(13L);
        guisuiQuan.setTypeCode("GUISUI_QUAN");
        guisuiQuan.setIsRest(0);
        guisuiQuan.setCountsDaytimeHeadcount(1);
        guisuiQuan.setCountsAsZhongForStructure(0);
        guisuiQuan.setCountsAsLinForStructure(1);
        when(shiftTypeMapper.findAll()).thenReturn(List.of(xiu, zhong, lin, guisuiQuan));

        MedirRosterCell manual1 = new MedirRosterCell();
        manual1.setRosterWeekId(1L);
        manual1.setStaffId(101L);
        manual1.setWorkDate(LocalDate.of(2026, 4, 13));
        manual1.setShiftTypeId(13L);
        MedirRosterCell manual2 = new MedirRosterCell();
        manual2.setRosterWeekId(1L);
        manual2.setStaffId(104L);
        manual2.setWorkDate(LocalDate.of(2026, 4, 18));
        manual2.setShiftTypeId(10L);
        when(cellMapper.findByRosterWeekId(1L)).thenReturn(List.of(manual1, manual2));

        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_25"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekday_134"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("headcount.weekend_holiday"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_zhong"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(1L), eq("structure.min_lin"))).thenReturn(null);
        when(configMapper.findByTeamIdAndConfigKey(eq(0L), eq("structure.min_lin"))).thenReturn(null);

        service.generateWeek(1L, new RosterWeekGenerateRequest(null, null, 0, "auto"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedirRosterCell>> captor =
                (ArgumentCaptor<List<MedirRosterCell>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(cellMapper).insertBatch(captor.capture());
        List<MedirRosterCell> saved = captor.getValue();

        for (MedirStaff s : staffs) {
            long restCount = saved.stream()
                    .filter(c -> c.getStaffId().equals(s.getId()) && c.getShiftTypeId().equals(10L))
                    .count();
            assertThat(restCount).isGreaterThanOrEqualTo(2);
        }
    }
}
