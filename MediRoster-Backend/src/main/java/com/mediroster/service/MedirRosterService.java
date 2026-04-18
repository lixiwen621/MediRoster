package com.mediroster.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.RosterCellReplaceRequest;
import com.mediroster.dto.request.RosterStaffPostReplaceRequest;
import com.mediroster.dto.request.RosterWeekGenerateRequest;
import com.mediroster.dto.request.RosterWeekCreateRequest;
import com.mediroster.dto.request.RosterWeekUpdateRequest;
import com.mediroster.dto.request.RosterWeekWeekendStatReplaceRequest;
import com.mediroster.dto.response.RosterCellResponse;
import com.mediroster.dto.response.RosterWeekExcelExportResult;
import com.mediroster.dto.response.RosterWeekGenerateResponse;
import com.mediroster.dto.response.RosterWeekResponse;
import com.mediroster.dto.response.RosterWeekStaffPostResponse;
import com.mediroster.dto.response.RosterWeekWeekendStatResponse;
import com.mediroster.entity.MedirConfig;
import com.mediroster.entity.MedirRosterCell;
import com.mediroster.entity.MedirRosterWeek;
import com.mediroster.entity.MedirRosterWeekStaffPost;
import com.mediroster.entity.MedirRosterWeekWeekendStat;
import com.mediroster.entity.MedirShiftType;
import com.mediroster.entity.MedirStaff;
import com.mediroster.entity.MedirTeam;
import com.mediroster.mapper.MedirConfigMapper;
import com.mediroster.mapper.MedirRosterCellMapper;
import com.mediroster.mapper.MedirRosterWeekMapper;
import com.mediroster.mapper.MedirRosterWeekStaffPostMapper;
import com.mediroster.mapper.MedirRosterWeekWeekendStatMapper;
import com.mediroster.mapper.MedirShiftTypeMapper;
import com.mediroster.mapper.MedirStaffMapper;
import com.mediroster.mapper.MedirTeamMapper;
import com.mediroster.excel.RosterWeekExcelExporter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 排班周与单元格。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedirRosterService {

    private static final Gson GSON = new Gson();

    private static final String CONFIG_KEY_WEEKEND_FULL_SHIFT_TYPES = "stats.weekend_full_shift_types";
    private static final String CONFIG_KEY_HEADCOUNT_WEEKDAY_134 = "headcount.weekday_134";
    private static final String CONFIG_KEY_HEADCOUNT_TUESDAY = "headcount.weekday_2";
    private static final String CONFIG_KEY_HEADCOUNT_FRIDAY = "headcount.weekday_5";
    private static final String CONFIG_KEY_HEADCOUNT_WEEKEND_HOLIDAY = "headcount.weekend_holiday";
    private static final String CONFIG_KEY_STRUCTURE_MIN_ZHONG = "structure.min_zhong";
    /** 周一至周五：每天至少「结构临」数（骨髓全算临）。 */
    private static final String CONFIG_KEY_STRUCTURE_MIN_LIN = "structure.min_lin";
    /** 周六、周日：每天至少「结构临」数；与工作日分开配置，默认 1。 */
    private static final String CONFIG_KEY_STRUCTURE_MIN_LIN_WEEKEND = "structure.min_lin_weekend";
    /** 每人每周目标休息天数（默认 2，见需求 §20）。 */
    private static final String CONFIG_KEY_REST_TARGET_MIN_DAYS = "rest.target_min_days";
    /** 弹性减人头时周四最低白天人数。 */
    private static final String CONFIG_KEY_HEADCOUNT_FLEX_FLOOR_THU = "headcount.flex_floor_thursday";
    /** 弹性减人头时周五最低白天人数。 */
    private static final String CONFIG_KEY_HEADCOUNT_FLEX_FLOOR_FRI = "headcount.flex_floor_friday";
    /** 「周末全天」自动统计按姓名排除（JSON 数组，如 ["程海荣"]）。 */
    private static final String CONFIG_KEY_STATS_WEEKEND_FULL_EXCLUDE_NAMES = "stats.weekend_full_exclude_names";
    /** 周末排「临」时是否按 sort_order 自上而下轮序（1=是）。 */
    private static final String CONFIG_KEY_ROSTER_WEEKEND_LIN_ROTATE = "roster.weekend_lin_rotate_by_sort_order";
    private static final String STRATEGY_FILL_UNCONFIRMED = "FILL_UNCONFIRMED";
    private static final String STRATEGY_OVERWRITE_ALL = "OVERWRITE_ALL";
    private static final String CONFIG_KEY_EXPORT_TITLE = "export.title";
    private static final String CONFIG_KEY_EXPORT_FOOTER_SMALL_NIGHT = "export.footer.small_night";
    private static final String DEFAULT_EXPORT_TITLE = "临检组排班表";

    private final MedirRosterWeekMapper rosterWeekMapper;
    private final MedirRosterCellMapper rosterCellMapper;
    private final MedirRosterWeekStaffPostMapper rosterWeekStaffPostMapper;
    private final MedirRosterWeekWeekendStatMapper rosterWeekWeekendStatMapper;
    private final MedirStaffMapper staffMapper;
    private final MedirShiftTypeMapper shiftTypeMapper;
    private final MedirConfigMapper configMapper;
    private final MedirTeamMapper teamMapper;

    public List<RosterWeekResponse> listByTeamAndYear(Long teamId, Integer yearLabel) {
        return rosterWeekMapper.findByTeamAndYear(teamId, yearLabel).stream()
                .map(this::toWeekResponse)
                .toList();
    }

    public RosterWeekResponse getWeek(Long id) {
        MedirRosterWeek w = I18nPreconditions.checkNotNull(
                rosterWeekMapper.findById(id), NOT_FOUND, "error.rosterWeek.notFound");
        return toWeekResponse(w);
    }

    @Transactional
    public RosterWeekResponse createWeek(RosterWeekCreateRequest req) {
        MedirRosterWeek exist = rosterWeekMapper.findByTeamAndWeekStart(req.teamId(), req.weekStartDate());
        I18nPreconditions.checkArgument(exist == null, CONFLICT, "error.rosterWeek.exists");
        MedirRosterWeek w = new MedirRosterWeek();
        w.setTeamId(req.teamId());
        w.setWeekStartDate(req.weekStartDate());
        w.setYearLabel(req.yearLabel());
        w.setStatus(req.status());
        w.setVersion(0);
        w.setRemark(req.remark());
        rosterWeekMapper.insert(w);
        return toWeekResponse(rosterWeekMapper.findById(w.getId()));
    }

    @Transactional
    public RosterWeekResponse updateWeek(Long id, RosterWeekUpdateRequest req) {
        MedirRosterWeek w = I18nPreconditions.checkNotNull(
                rosterWeekMapper.findById(id), NOT_FOUND, "error.rosterWeek.notFound");
        w.setYearLabel(req.yearLabel());
        w.setStatus(req.status());
        w.setRemark(req.remark());
        w.setVersion(req.version());
        int n = rosterWeekMapper.updateById(w);
        I18nPreconditions.checkArgument(n != 0, OPTIMISTIC_LOCK, "error.optimisticLock");
        return toWeekResponse(rosterWeekMapper.findById(id));
    }

    @Transactional
    public void deleteWeek(Long id) {
        I18nPreconditions.checkNotNull(rosterWeekMapper.findById(id), NOT_FOUND, "error.rosterWeek.notFound");
        rosterCellMapper.deleteByRosterWeekId(id);
        rosterWeekStaffPostMapper.deleteByRosterWeekId(id);
        rosterWeekMapper.deleteById(id);
    }

    public List<RosterCellResponse> listCells(Long rosterWeekId) {
        ensureWeek(rosterWeekId);
        return rosterCellMapper.findByRosterWeekId(rosterWeekId).stream()
                .map(this::toCellResponse)
                .toList();
    }

    /**
     * 覆盖当周单元格（先删后插），并基于落库后的周六、日班次**即时重算**「周末全天」「上周末」自动值，与 {@link #listWeekendStats(Long)} 一致合并人工覆盖后返回。
     */
    @Transactional
    public List<RosterWeekWeekendStatResponse> replaceCells(Long rosterWeekId, RosterCellReplaceRequest req) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        Long teamId = week.getTeamId();
        List<MedirRosterCell> rows = new ArrayList<>();
        List<RosterCellReplaceRequest.CellItem> items = req.cells() == null ? List.of() : req.cells();
        for (RosterCellReplaceRequest.CellItem item : items) {
            MedirStaff staff = staffMapper.findById(item.staffId());
            I18nPreconditions.checkArgument(
                    staff != null && staff.getDeletedAt() == null && teamId.equals(staff.getTeamId()),
                    VALIDATION_ERROR, "error.roster.invalidStaff", item.staffId());
            I18nPreconditions.checkNotNull(
                    shiftTypeMapper.findById(item.shiftTypeId()),
                    VALIDATION_ERROR, "error.roster.invalidShift", item.shiftTypeId());
            MedirRosterCell c = new MedirRosterCell();
            c.setRosterWeekId(rosterWeekId);
            c.setStaffId(item.staffId());
            c.setWorkDate(item.workDate());
            c.setShiftTypeId(item.shiftTypeId());
            c.setPostId(item.postId());
            c.setPostLabel(item.postLabel());
            c.setValidationExempt(item.validationExempt());
            c.setExemptReason(item.exemptReason());
            c.setRemark(item.remark());
            rows.add(c);
        }
        rosterCellMapper.deleteByRosterWeekId(rosterWeekId);
        if (!CollectionUtils.isEmpty(rows)) {
            rosterCellMapper.insertBatch(rows);
        }
        return listWeekendStats(rosterWeekId);
    }

    @Transactional
    public RosterWeekGenerateResponse generateWeek(Long rosterWeekId, RosterWeekGenerateRequest req) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        GenerateOptions options = normalizeGenerateOptions(req);
        List<MedirStaff> staffs = new ArrayList<>(staffMapper.findByTeamId(week.getTeamId(), false));
        staffs.sort(Comparator.comparing(MedirStaff::getSortOrder).thenComparing(MedirStaff::getId));
        I18nPreconditions.checkArgument(!staffs.isEmpty(), CONFLICT, "error.roster.generate.noStaff");

        Map<Long, MedirShiftType> shiftById = new HashMap<>();
        Map<String, MedirShiftType> shiftByCode = new HashMap<>();
        for (MedirShiftType shiftType : shiftTypeMapper.findAll()) {
            shiftById.put(shiftType.getId(), shiftType);
            shiftByCode.put(shiftType.getTypeCode(), shiftType);
        }
        MedirShiftType xiu = I18nPreconditions.checkNotNull(
                shiftByCode.get("XIU"), CONFLICT, "error.roster.generate.missingShiftType", "XIU");
        MedirShiftType zhong = I18nPreconditions.checkNotNull(
                shiftByCode.get("ZHONG"), CONFLICT, "error.roster.generate.missingShiftType", "ZHONG");
        MedirShiftType lin = I18nPreconditions.checkNotNull(
                shiftByCode.get("LIN"), CONFLICT, "error.roster.generate.missingShiftType", "LIN");
        MedirShiftType guisuiQuan = shiftByCode.get("GUISUI_QUAN");

        List<MedirRosterCell> existingRows = rosterCellMapper.findByRosterWeekId(rosterWeekId);
        Map<String, MedirRosterCell> cellByStaffAndDate = new HashMap<>();
        for (MedirRosterCell row : existingRows) {
            cellByStaffAndDate.put(cellKey(row.getStaffId(), row.getWorkDate()), row);
        }

        boolean overwriteAll = STRATEGY_OVERWRITE_ALL.equals(options.strategy()) && options.respectManualConfirmed() == 0;
        // 补全前已落库的单元格：后处理（互换/升降班次）不得修改，否则会覆盖用户自定义（如骨髓全）。
        Set<String> frozenCellKeysForPostProcess;
        if (!overwriteAll) {
            frozenCellKeysForPostProcess = new HashSet<>();
            for (MedirRosterCell row : existingRows) {
                frozenCellKeysForPostProcess.add(cellKey(row.getStaffId(), row.getWorkDate()));
            }
        } else {
            frozenCellKeysForPostProcess = Collections.emptySet();
        }
        List<MedirRosterCell> finalRows = new ArrayList<>();
        if (!overwriteAll) {
            finalRows.addAll(existingRows);
        } else {
            cellByStaffAndDate.clear();
        }
        int overwrittenCellCount = overwriteAll ? existingRows.size() : 0;
        int skippedConfirmedCount = !overwriteAll && options.respectManualConfirmed() == 1 ? existingRows.size() : 0;
        log.info(
                "Roster gen priority policy weekId={} order=1frozenCells 2restMinDays+flexThuFri 3manualTwoFullDays 4weeklyStructureLin1to2 5dailyHeadcount+structure",
                rosterWeekId);
        int[] dailyFlexibleHeadcount = computeFlexibleDailyHeadcounts(week.getTeamId(), week.getWeekStartDate(), staffs.size());
        int sumWork = 0;
        for (int v : dailyFlexibleHeadcount) {
            sumWork += v;
        }
        int totalRestSlots = staffs.size() * 7 - sumWork;
        log.info(
                "Roster gen flexHeadcount weekId={} monToSun={},{},{},{},{},{},{} sumWork={} totalRestSlots={}",
                rosterWeekId,
                dailyFlexibleHeadcount[0],
                dailyFlexibleHeadcount[1],
                dailyFlexibleHeadcount[2],
                dailyFlexibleHeadcount[3],
                dailyFlexibleHeadcount[4],
                dailyFlexibleHeadcount[5],
                dailyFlexibleHeadcount[6],
                sumWork,
                totalRestSlots);
        Map<Long, Integer> targetRestByStaff = buildWeeklyRestTargets(week, staffs, dailyFlexibleHeadcount);
        Map<Long, WeekStaffStat> weeklyStats = initWeeklyStats(staffs, finalRows, shiftById);
        Map<Long, Integer> manualFullDayCountByStaff = new HashMap<>();
        for (MedirStaff s : staffs) {
            manualFullDayCountByStaff.put(s.getId(), 0);
        }
        for (MedirRosterCell row : existingRows) {
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                manualFullDayCountByStaff.merge(row.getStaffId(), 1, Integer::sum);
            }
        }

        int generatedCellCount = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate workDate = week.getWeekStartDate().plusDays(i);
            int requiredHeadcount = dailyFlexibleHeadcount[i];
            int requiredZhong = resolveIntConfig(week.getTeamId(), CONFIG_KEY_STRUCTURE_MIN_ZHONG, 2);
            int requiredLin = resolveRequiredStructureLin(week.getTeamId(), workDate);

            List<MedirRosterCell> dayRows = new ArrayList<>();
            for (MedirRosterCell row : finalRows) {
                if (workDate.equals(row.getWorkDate())) {
                    dayRows.add(row);
                }
            }
            int daytimeCount = countByFlag(dayRows, shiftById, MedirShiftType::getCountsDaytimeHeadcount);
            int zhongCount = countByFlag(dayRows, shiftById, MedirShiftType::getCountsAsZhongForStructure);
            int linCount = countByFlag(dayRows, shiftById, MedirShiftType::getCountsAsLinForStructure);

            List<MedirStaff> dayStaffs = rotateStaffOrder(staffs, i);
            List<MedirStaff> candidates = new ArrayList<>();
            for (MedirStaff staff : dayStaffs) {
                String key = cellKey(staff.getId(), workDate);
                if (cellByStaffAndDate.containsKey(key)) {
                    continue;
                }
                candidates.add(staff);
            }
            // 周末：按排班表行序（sort_order）轮「临」；工作日：本周全天已多者优先补中（与 pickShiftType 一致）。
            DayOfWeek dow = workDate.getDayOfWeek();
            boolean weekendLinByRosterOrder =
                    resolveIntConfig(week.getTeamId(), CONFIG_KEY_ROSTER_WEEKEND_LIN_ROTATE, 1) == 1
                            && (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            if (weekendLinByRosterOrder) {
                candidates.sort(Comparator.comparing((MedirStaff s) -> s.getSortOrder() == null ? Integer.MAX_VALUE : s.getSortOrder())
                        .thenComparing(MedirStaff::getId));
                log.info(
                        "Roster gen weekendCandidateOrder weekId={} workDate={} mode=RosterTableSortOrder",
                        rosterWeekId,
                        workDate);
            } else {
                candidates.sort((a, b) -> {
                    int fa = weeklyStats.get(a.getId()).fullDayCount;
                    int fb = weeklyStats.get(b.getId()).fullDayCount;
                    int cmp = Integer.compare(fb, fa);
                    if (cmp != 0) {
                        return cmp;
                    }
                    return Long.compare(a.getId(), b.getId());
                });
            }
            int workNeeded = Math.max(0, requiredHeadcount - daytimeCount);
            if (workNeeded > candidates.size()) {
                workNeeded = candidates.size();
            }
            Set<Long> workAssignees = pickWorkAssignees(candidates, workNeeded, weeklyStats, targetRestByStaff);

            for (MedirStaff staff : candidates) {
                MedirShiftType picked = workAssignees.contains(staff.getId())
                        ? pickShiftType(
                                requiredHeadcount, requiredZhong, requiredLin, daytimeCount, zhongCount, linCount,
                                weeklyStats.get(staff.getId()).fullDayCount,
                                manualFullDayCountByStaff.getOrDefault(staff.getId(), 0),
                                zhong, lin, guisuiQuan, xiu)
                        : xiu;
                MedirRosterCell generated = new MedirRosterCell();
                generated.setRosterWeekId(rosterWeekId);
                generated.setStaffId(staff.getId());
                generated.setWorkDate(workDate);
                generated.setShiftTypeId(picked.getId());
                generated.setPostId(null);
                generated.setPostLabel(null);
                generated.setValidationExempt(0);
                generated.setExemptReason(null);
                generated.setRemark(Strings.emptyToNull(options.reason()));
                finalRows.add(generated);
                generatedCellCount++;
                cellByStaffAndDate.put(cellKey(staff.getId(), workDate), generated);
                WeekStaffStat stat = weeklyStats.get(staff.getId());
                stat.assignedCount++;
                if (isRestShift(picked)) {
                    stat.restCount++;
                }
                if (picked.getCountsDaytimeHeadcount() != null && picked.getCountsDaytimeHeadcount() == 1) {
                    daytimeCount++;
                }
                if (picked.getCountsAsZhongForStructure() != null && picked.getCountsAsZhongForStructure() == 1) {
                    zhongCount++;
                }
                if (picked.getCountsAsLinForStructure() != null && picked.getCountsAsLinForStructure() == 1) {
                    linCount++;
                    stat.fullDayCount++;
                }
            }
        }

        Map<Long, String> staffIdToName = new HashMap<>();
        for (MedirStaff s : staffs) {
            staffIdToName.put(s.getId(), s.getName());
        }
        logStructureLinWeekAudit(
                "afterFillBeforePostProcess",
                rosterWeekId,
                week.getWeekStartDate(),
                staffs,
                finalRows,
                shiftById,
                week.getTeamId());
        logInfoRosterConstraintDiagnostics(
                "afterFillBeforePostProcess",
                rosterWeekId,
                week,
                staffs,
                finalRows,
                shiftById,
                targetRestByStaff,
                staffIdToName);

        // 后处理：保证每人每周至少 2 个全天（临/骨髓全，与 counts_as_lin_for_structure 一致）；不修改 frozenCellKeysForPostProcess
        ensureEachStaffExactlyTwoFullDays(
                week,
                finalRows,
                weeklyStats,
                shiftById,
                zhong,
                lin,
                frozenCellKeysForPostProcess,
                staffIdToName);

        logStructureLinWeekAudit(
                "afterPostProcess",
                rosterWeekId,
                week.getWeekStartDate(),
                staffs,
                finalRows,
                shiftById,
                week.getTeamId());
        logInfoRosterConstraintDiagnostics(
                "afterPostProcess",
                rosterWeekId,
                week,
                staffs,
                finalRows,
                shiftById,
                targetRestByStaff,
                staffIdToName);

        if (options.dryRun() == 0) {
            rosterCellMapper.deleteByRosterWeekId(rosterWeekId);
            if (!finalRows.isEmpty()) {
                rosterCellMapper.insertBatch(finalRows);
            }
            log.info(
                    "Roster generation committed. weekId={}, strategy={}, respectManualConfirmed={}, generatedCellCount={}, overwrittenCellCount={}, skippedConfirmedCount={}, reason={}",
                    rosterWeekId,
                    options.strategy(),
                    options.respectManualConfirmed(),
                    generatedCellCount,
                    overwrittenCellCount,
                    skippedConfirmedCount,
                    options.reason());
        } else {
            log.info(
                    "Roster generation dry-run. weekId={}, strategy={}, respectManualConfirmed={}, generatedCellCount={}, overwrittenCellCount={}, skippedConfirmedCount={}, reason={}",
                    rosterWeekId,
                    options.strategy(),
                    options.respectManualConfirmed(),
                    generatedCellCount,
                    overwrittenCellCount,
                    skippedConfirmedCount,
                    options.reason());
        }

        String message = options.dryRun() == 1
                ? "dry-run finished"
                : "generation finished";
        return new RosterWeekGenerateResponse(
                rosterWeekId,
                options.strategy(),
                generatedCellCount,
                overwrittenCellCount,
                skippedConfirmedCount,
                options.dryRun(),
                message);
    }

    public List<RosterWeekStaffPostResponse> listStaffPosts(Long rosterWeekId) {
        ensureWeek(rosterWeekId);
        return rosterWeekStaffPostMapper.findByRosterWeekId(rosterWeekId).stream()
                .map(this::toStaffPostResponse)
                .toList();
    }

    @Transactional
    public void replaceStaffPosts(Long rosterWeekId, RosterStaffPostReplaceRequest req) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        Long teamId = week.getTeamId();
        List<MedirRosterWeekStaffPost> rows = new ArrayList<>();
        if (req.items() != null) {
            for (RosterStaffPostReplaceRequest.Item item : req.items()) {
                MedirStaff staff = staffMapper.findById(item.staffId());
                I18nPreconditions.checkArgument(
                        staff != null && staff.getDeletedAt() == null && teamId.equals(staff.getTeamId()),
                        VALIDATION_ERROR, "error.roster.invalidStaff", item.staffId());
                MedirRosterWeekStaffPost r = new MedirRosterWeekStaffPost();
                r.setRosterWeekId(rosterWeekId);
                r.setStaffId(item.staffId());
                r.setDisplayPostId(item.displayPostId());
                r.setDisplayLabel(item.displayLabel());
                rows.add(r);
            }
        }
        rosterWeekStaffPostMapper.deleteByRosterWeekId(rosterWeekId);
        if (!rows.isEmpty()) {
            rosterWeekStaffPostMapper.insertBatch(rows);
        }
    }

    public List<RosterWeekWeekendStatResponse> listWeekendStats(Long rosterWeekId) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        List<MedirStaff> staffs = staffMapper.findByTeamId(week.getTeamId(), false);
        Map<Long, AutoWeekendStat> autoMap = computeWeekendAutoStats(week, staffs);
        Map<Long, MedirRosterWeekWeekendStat> overrideMap = new HashMap<>();
        for (MedirRosterWeekWeekendStat row : rosterWeekWeekendStatMapper.findByRosterWeekId(rosterWeekId)) {
            overrideMap.put(row.getStaffId(), row);
        }

        List<RosterWeekWeekendStatResponse> result = new ArrayList<>();
        for (MedirStaff staff : staffs) {
            AutoWeekendStat auto = autoMap.getOrDefault(staff.getId(), new AutoWeekendStat(0, 0));
            MedirRosterWeekWeekendStat override = overrideMap.get(staff.getId());
            Integer weekendFullFinal =
                    override != null && override.getWeekendFullOverride() != null
                            ? override.getWeekendFullOverride()
                            : auto.weekendFullAuto();
            Integer lastWeekendFinal =
                    override != null && override.getLastWeekendOverride() != null
                            ? override.getLastWeekendOverride()
                            : auto.lastWeekendAuto();
            Integer isOverridden =
                    override != null && (override.getWeekendFullOverride() != null || override.getLastWeekendOverride() != null)
                            ? 1
                            : 0;
            result.add(new RosterWeekWeekendStatResponse(
                    override != null ? override.getId() : null,
                    rosterWeekId,
                    staff.getId(),
                    auto.weekendFullAuto(),
                    weekendFullFinal,
                    auto.lastWeekendAuto(),
                    lastWeekendFinal,
                    isOverridden,
                    override != null ? override.getOverrideReason() : null,
                    override != null ? override.getUpdatedAt() : null));
        }
        return result;
    }

    @Transactional
    public void replaceWeekendStats(Long rosterWeekId, RosterWeekWeekendStatReplaceRequest req) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        Map<Long, MedirStaff> staffMap = new HashMap<>();
        for (MedirStaff staff : staffMapper.findByTeamId(week.getTeamId(), false)) {
            staffMap.put(staff.getId(), staff);
        }
        for (RosterWeekWeekendStatReplaceRequest.Item item : req.items()) {
            I18nPreconditions.checkNotNull(
                    staffMap.get(item.staffId()), VALIDATION_ERROR, "error.roster.invalidStaff", item.staffId());
            boolean clearOverride = item.weekendFullOverride() == null
                    && item.lastWeekendOverride() == null
                    && Strings.isNullOrEmpty(item.overrideReason());
            if (clearOverride) {
                rosterWeekWeekendStatMapper.deleteByWeekAndStaff(rosterWeekId, item.staffId());
                continue;
            }
            MedirRosterWeekWeekendStat row = new MedirRosterWeekWeekendStat();
            row.setRosterWeekId(rosterWeekId);
            row.setStaffId(item.staffId());
            row.setWeekendFullOverride(item.weekendFullOverride());
            row.setLastWeekendOverride(item.lastWeekendOverride());
            row.setOverrideReason(Strings.emptyToNull(item.overrideReason()));
            rosterWeekWeekendStatMapper.upsert(row);
        }
    }

    /**
     * 导出当周排班为 Excel（A4 纵向；表头标题默认「临检组排班表」，可被 {@code export.title} 覆盖）。
     */
    public RosterWeekExcelExportResult exportWeekExcel(Long rosterWeekId, String filenameHint) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        MedirTeam team = I18nPreconditions.checkNotNull(
                teamMapper.findById(week.getTeamId()), NOT_FOUND, "error.team.notFound");
        List<MedirStaff> staffs = new ArrayList<>(staffMapper.findByTeamId(week.getTeamId(), false));
        staffs.sort(Comparator.comparing(MedirStaff::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MedirStaff::getId));
        List<Long> staffOrder = staffs.stream().map(MedirStaff::getId).toList();
        Map<Long, String> staffNameById = new HashMap<>();
        for (MedirStaff s : staffs) {
            staffNameById.put(s.getId(), s.getName());
        }
        Map<Long, String> shiftNameById = new HashMap<>();
        for (MedirShiftType st : shiftTypeMapper.findAll()) {
            shiftNameById.put(st.getId(), st.getNameZh() != null ? st.getNameZh() : st.getTypeCode());
        }
        List<RosterCellResponse> cells = rosterCellMapper.findByRosterWeekId(rosterWeekId).stream()
                .map(this::toCellResponse)
                .toList();
        List<RosterWeekStaffPostResponse> staffPosts = rosterWeekStaffPostMapper.findByRosterWeekId(rosterWeekId).stream()
                .map(this::toStaffPostResponse)
                .toList();
        List<RosterWeekWeekendStatResponse> weekendStats = listWeekendStats(rosterWeekId);
        String title = resolveExportTitle(week.getTeamId());
        String footer = resolveExportFooter(week.getTeamId());
        try {
            byte[] bytes = RosterWeekExcelExporter.export(
                    week,
                    title,
                    footer,
                    shiftNameById,
                    cells,
                    staffPosts,
                    weekendStats,
                    staffOrder,
                    staffNameById);
            return new RosterWeekExcelExportResult(bytes, buildExportDownloadFilename(week, team, filenameHint));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String resolveExportTitle(Long teamId) {
        MedirConfig scoped = configMapper.findByTeamIdAndConfigKey(teamId, CONFIG_KEY_EXPORT_TITLE);
        if (scoped != null && !Strings.isNullOrEmpty(scoped.getConfigValue())) {
            return scoped.getConfigValue().trim();
        }
        MedirConfig global = configMapper.findByTeamIdAndConfigKey(0L, CONFIG_KEY_EXPORT_TITLE);
        if (global != null && !Strings.isNullOrEmpty(global.getConfigValue())) {
            return global.getConfigValue().trim();
        }
        return DEFAULT_EXPORT_TITLE;
    }

    private String resolveExportFooter(Long teamId) {
        MedirConfig scoped = configMapper.findByTeamIdAndConfigKey(teamId, CONFIG_KEY_EXPORT_FOOTER_SMALL_NIGHT);
        if (scoped != null && scoped.getConfigValue() != null && !scoped.getConfigValue().isBlank()) {
            return scoped.getConfigValue().trim();
        }
        MedirConfig global = configMapper.findByTeamIdAndConfigKey(0L, CONFIG_KEY_EXPORT_FOOTER_SMALL_NIGHT);
        if (global != null && global.getConfigValue() != null && !global.getConfigValue().isBlank()) {
            return global.getConfigValue().trim();
        }
        return "";
    }

    private String buildExportDownloadFilename(MedirRosterWeek week, MedirTeam team, String requested) {
        if (!Strings.isNullOrEmpty(requested)) {
            String s = requested.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
            if (!s.toLowerCase().endsWith(".xlsx")) {
                s = s + ".xlsx";
            }
            return s;
        }
        String teamPart = team.getTeamName() != null ? team.getTeamName() : "排班";
        return teamPart + "_" + week.getWeekStartDate() + "_v" + week.getVersion() + ".xlsx";
    }

    private Map<Long, AutoWeekendStat> computeWeekendAutoStats(MedirRosterWeek week, List<MedirStaff> staffs) {
        LocalDate saturday = week.getWeekStartDate().plusDays(5);
        LocalDate sunday = week.getWeekStartDate().plusDays(6);
        Set<String> weekendFullShiftTypeCodes = resolveWeekendFullShiftTypeCodes(week.getTeamId());
        Set<String> weekendFullExcludedNames = resolveWeekendFullExcludedNames(week.getTeamId());

        Map<Long, String> shiftTypeCodeMap = new HashMap<>();
        for (MedirShiftType shiftType : shiftTypeMapper.findAll()) {
            shiftTypeCodeMap.put(shiftType.getId(), shiftType.getTypeCode());
        }

        Map<Long, Map<LocalDate, String>> staffShiftMap = new HashMap<>();
        for (MedirRosterCell cell : rosterCellMapper.findByRosterWeekId(week.getId())) {
            if (!saturday.equals(cell.getWorkDate()) && !sunday.equals(cell.getWorkDate())) {
                continue;
            }
            String shiftTypeCode = shiftTypeCodeMap.get(cell.getShiftTypeId());
            if (shiftTypeCode == null) {
                continue;
            }
            staffShiftMap
                    .computeIfAbsent(cell.getStaffId(), ignored -> new HashMap<>())
                    .put(cell.getWorkDate(), shiftTypeCode);
        }

        Map<Long, AutoWeekendStat> autoMap = new HashMap<>();
        for (MedirStaff staff : staffs) {
            Map<LocalDate, String> daily = staffShiftMap.getOrDefault(staff.getId(), Map.of());
            String satCode = daily.get(saturday);
            String sunCode = daily.get(sunday);
            int weekendFullAuto = 0;
            boolean excludeWeekendFull =
                    staff.getName() != null && weekendFullExcludedNames.contains(staff.getName().trim());
            if (!excludeWeekendFull) {
                if (satCode != null && weekendFullShiftTypeCodes.contains(satCode)) {
                    weekendFullAuto++;
                }
                if (sunCode != null && weekendFullShiftTypeCodes.contains(sunCode)) {
                    weekendFullAuto++;
                }
            } else {
                log.info("Roster weekendStat exclude weekendFull staffId={} name={}", staff.getId(), staff.getName());
            }
            int lastWeekendAuto = "ZHONG".equals(satCode) && "ZHONG".equals(sunCode) ? 1 : 0;
            autoMap.put(staff.getId(), new AutoWeekendStat(weekendFullAuto, lastWeekendAuto));
        }
        return autoMap;
    }

    /** 按姓名从「周末全天」自动统计中排除（需求 §20 REQ-GEN-07）。 */
    private Set<String> resolveWeekendFullExcludedNames(Long teamId) {
        Set<String> merged = new HashSet<>();
        MedirConfig scoped = configMapper.findByTeamIdAndConfigKey(teamId, CONFIG_KEY_STATS_WEEKEND_FULL_EXCLUDE_NAMES);
        if (scoped != null && !Strings.isNullOrEmpty(scoped.getConfigValue())) {
            merged.addAll(parseJsonArrayConfig(scoped.getConfigValue()));
        }
        MedirConfig global = configMapper.findByTeamIdAndConfigKey(0L, CONFIG_KEY_STATS_WEEKEND_FULL_EXCLUDE_NAMES);
        if (global != null && !Strings.isNullOrEmpty(global.getConfigValue())) {
            merged.addAll(parseJsonArrayConfig(global.getConfigValue()));
        }
        Set<String> trimmed = new HashSet<>();
        for (String n : merged) {
            if (n != null && !n.isBlank()) {
                trimmed.add(n.trim());
            }
        }
        return trimmed;
    }

    private Set<String> resolveWeekendFullShiftTypeCodes(Long teamId) {
        MedirConfig scoped = configMapper.findByTeamIdAndConfigKey(teamId, CONFIG_KEY_WEEKEND_FULL_SHIFT_TYPES);
        if (scoped != null) {
            Set<String> fromScoped = parseJsonArrayConfig(scoped.getConfigValue());
            if (!fromScoped.isEmpty()) {
                return fromScoped;
            }
        }
        MedirConfig global = configMapper.findByTeamIdAndConfigKey(0L, CONFIG_KEY_WEEKEND_FULL_SHIFT_TYPES);
        if (global != null) {
            Set<String> fromGlobal = parseJsonArrayConfig(global.getConfigValue());
            if (!fromGlobal.isEmpty()) {
                return fromGlobal;
            }
        }
        Set<String> fallback = new HashSet<>();
        for (MedirShiftType shiftType : shiftTypeMapper.findAll()) {
            if (shiftType.getCountsWeekendFullDayStat() != null && shiftType.getCountsWeekendFullDayStat() == 1) {
                fallback.add(shiftType.getTypeCode());
            }
        }
        if (!fallback.isEmpty()) {
            return fallback;
        }
        return Set.of("LIN", "GUISUI_QUAN");
    }

    private Set<String> parseJsonArrayConfig(String raw) {
        if (Strings.isNullOrEmpty(raw)) {
            return Set.of();
        }
        try {
            String[] values = GSON.fromJson(raw, String[].class);
            if (values == null || values.length == 0) {
                return Set.of();
            }
            Set<String> result = new HashSet<>();
            for (String value : values) {
                if (!Strings.isNullOrEmpty(value)) {
                    result.add(value);
                }
            }
            return result;
        } catch (Exception ignored) {
            return Set.of();
        }
    }

    private GenerateOptions normalizeGenerateOptions(RosterWeekGenerateRequest req) {
        String strategy = req == null || Strings.isNullOrEmpty(req.strategy())
                ? STRATEGY_FILL_UNCONFIRMED
                : req.strategy();
        I18nPreconditions.checkArgument(
                STRATEGY_FILL_UNCONFIRMED.equals(strategy) || STRATEGY_OVERWRITE_ALL.equals(strategy),
                CONFLICT,
                "error.roster.generate.invalidStrategy",
                strategy);
        int respectManualConfirmed = req != null && req.respectManualConfirmed() != null
                ? req.respectManualConfirmed()
                : 1;
        if (STRATEGY_FILL_UNCONFIRMED.equals(strategy)) {
            respectManualConfirmed = 1;
        }
        int dryRun = req != null && req.dryRun() != null ? req.dryRun() : 0;
        String reason = req != null ? req.reason() : null;
        return new GenerateOptions(strategy, respectManualConfirmed, dryRun, reason);
    }

    /**
     * 先按 {@link #computeFlexibleDailyHeadcounts} 得到每日人头，再分配每人目标休；优先满足 {@link #CONFIG_KEY_REST_TARGET_MIN_DAYS}。
     */
    private int[] computeFlexibleDailyHeadcounts(Long teamId, LocalDate weekStart, int staffCount) {
        int[] hc = new int[7];
        for (int i = 0; i < 7; i++) {
            hc[i] = resolveRequiredHeadcount(teamId, weekStart.plusDays(i));
        }
        int minRestPerPerson = resolveIntConfig(teamId, CONFIG_KEY_REST_TARGET_MIN_DAYS, 2);
        int needRestSlots = minRestPerPerson * staffCount;
        int floorThu = resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_FLEX_FLOOR_THU, 3);
        int floorFri = resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_FLEX_FLOOR_FRI, 3);
        int safety = 0;
        while (safety < 24) {
            safety++;
            int totalWork = 0;
            for (int v : hc) {
                totalWork += v;
            }
            int totalRest = staffCount * 7 - totalWork;
            if (totalRest >= needRestSlots) {
                break;
            }
            boolean reduced = false;
            if (hc[4] > floorFri) {
                hc[4]--;
                reduced = true;
            } else if (hc[3] > floorThu) {
                hc[3]--;
                reduced = true;
            }
            if (!reduced) {
                log.info(
                        "Roster gen flexHeadcount stopReduce teamId={} floorThu={} floorFri={} hcThu={} hcFri={}",
                        teamId,
                        floorThu,
                        floorFri,
                        hc[3],
                        hc[4]);
                break;
            }
        }
        return hc;
    }

    private Map<Long, Integer> buildWeeklyRestTargets(MedirRosterWeek week, List<MedirStaff> staffs, int[] dailyHc) {
        Map<Long, Integer> targetMap = new HashMap<>();
        if (staffs.isEmpty()) {
            return targetMap;
        }
        int totalHeadcount = 0;
        for (int v : dailyHc) {
            totalHeadcount += v;
        }
        int totalSlots = staffs.size() * 7;
        int totalRests = Math.max(0, totalSlots - totalHeadcount);
        int minPer = resolveIntConfig(week.getTeamId(), CONFIG_KEY_REST_TARGET_MIN_DAYS, 2);
        int needAtLeast = minPer * staffs.size();
        int offset = Math.floorMod((int) week.getWeekStartDate().toEpochDay(), staffs.size());
        if (totalRests >= needAtLeast) {
            int surplus = totalRests - needAtLeast;
            int baseExtra = surplus / staffs.size();
            int remExtra = surplus % staffs.size();
            for (int i = 0; i < staffs.size(); i++) {
                int rotated = Math.floorMod(i - offset, staffs.size());
                int target = minPer + baseExtra + (rotated < remExtra ? 1 : 0);
                targetMap.put(staffs.get(i).getId(), target);
            }
        } else {
            log.info(
                    "Roster gen restBudget deficit weekId={} totalRestSlots={} needForMinRestPerPerson={} usingProportionalSplit",
                    week.getId(),
                    totalRests,
                    needAtLeast);
            int base = totalRests / staffs.size();
            int extra = totalRests % staffs.size();
            for (int i = 0; i < staffs.size(); i++) {
                int rotated = Math.floorMod(i - offset, staffs.size());
                int target = base + (rotated < extra ? 1 : 0);
                targetMap.put(staffs.get(i).getId(), target);
            }
        }
        return targetMap;
    }

    private Map<Long, WeekStaffStat> initWeeklyStats(
            List<MedirStaff> staffs, List<MedirRosterCell> existingRows, Map<Long, MedirShiftType> shiftById) {
        Map<Long, WeekStaffStat> stats = new HashMap<>();
        for (MedirStaff staff : staffs) {
            stats.put(staff.getId(), new WeekStaffStat());
        }
        for (MedirRosterCell row : existingRows) {
            WeekStaffStat stat = stats.get(row.getStaffId());
            if (stat == null) {
                continue;
            }
            stat.assignedCount++;
            if (isRestShift(row.getShiftTypeId(), shiftById)) {
                stat.restCount++;
            }
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                stat.fullDayCount++;
            }
        }
        return stats;
    }

    private boolean isRestShift(MedirShiftType shiftType) {
        return shiftType != null && shiftType.getIsRest() != null && shiftType.getIsRest() == 1;
    }

    private boolean isRestShift(Long shiftTypeId, Map<Long, MedirShiftType> shiftById) {
        MedirShiftType shiftType = shiftById.get(shiftTypeId);
        return isRestShift(shiftType);
    }

    private Set<Long> pickWorkAssignees(
            List<MedirStaff> candidates,
            int workNeeded,
            Map<Long, WeekStaffStat> weeklyStats,
            Map<Long, Integer> targetRestByStaff) {
        if (workNeeded <= 0 || candidates.isEmpty()) {
            return Set.of();
        }
        List<MedirStaff> sorted = new ArrayList<>(candidates);
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < candidates.size(); i++) {
            orderMap.put(candidates.get(i).getId(), i);
        }
        sorted.sort((a, b) -> {
            WeekStaffStat sa = weeklyStats.get(a.getId());
            WeekStaffStat sb = weeklyStats.get(b.getId());
            int ta = targetRestByStaff.getOrDefault(a.getId(), 0);
            int tb = targetRestByStaff.getOrDefault(b.getId(), 0);
            boolean aMustRest = mustRestToday(sa, ta);
            boolean bMustRest = mustRestToday(sb, tb);
            if (aMustRest != bMustRest) {
                return Boolean.compare(aMustRest, bMustRest);
            }
            int aSurplus = sa.restCount - ta;
            int bSurplus = sb.restCount - tb;
            if (aSurplus != bSurplus) {
                return Integer.compare(bSurplus, aSurplus);
            }
            return Integer.compare(orderMap.get(a.getId()), orderMap.get(b.getId()));
        });
        Set<Long> selected = new HashSet<>();
        for (int i = 0; i < workNeeded && i < sorted.size(); i++) {
            selected.add(sorted.get(i).getId());
        }
        return selected;
    }

    private boolean mustRestToday(WeekStaffStat stat, int targetRest) {
        int restNeeded = Math.max(0, targetRest - stat.restCount);
        int remainingAfterToday = Math.max(0, 7 - (stat.assignedCount + 1));
        return restNeeded > remainingAfterToday;
    }

    /**
     * 周一至周五使用 {@link #CONFIG_KEY_STRUCTURE_MIN_LIN}（默认 2）；周六、周日使用 {@link #CONFIG_KEY_STRUCTURE_MIN_LIN_WEEKEND}（默认 1）。
     */
    private int resolveRequiredStructureLin(Long teamId, LocalDate workDate) {
        DayOfWeek dow = workDate.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return resolveIntConfig(teamId, CONFIG_KEY_STRUCTURE_MIN_LIN_WEEKEND, 1);
        }
        return resolveIntConfig(teamId, CONFIG_KEY_STRUCTURE_MIN_LIN, 2);
    }

    private int resolveRequiredHeadcount(Long teamId, LocalDate workDate) {
        DayOfWeek dayOfWeek = workDate.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.TUESDAY) {
            return resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_TUESDAY, 6);
        }
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            return resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_FRIDAY, 5);
        }
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_WEEKEND_HOLIDAY, 3);
        }
        return resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_WEEKDAY_134, 4);
    }

    private int resolveIntConfig(Long teamId, String key, int defaultValue) {
        MedirConfig scoped = configMapper.findByTeamIdAndConfigKey(teamId, key);
        if (scoped != null) {
            Integer parsed = parsePositiveInt(scoped.getConfigValue());
            if (parsed != null) {
                return parsed;
            }
        }
        MedirConfig global = configMapper.findByTeamIdAndConfigKey(0L, key);
        if (global != null) {
            Integer parsed = parsePositiveInt(global.getConfigValue());
            if (parsed != null) {
                return parsed;
            }
        }
        return defaultValue;
    }

    private Integer parsePositiveInt(String raw) {
        if (Strings.isNullOrEmpty(raw)) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private int countByFlag(List<MedirRosterCell> rows, Map<Long, MedirShiftType> shiftById, ShiftFlagExtractor flagExtractor) {
        int count = 0;
        for (MedirRosterCell row : rows) {
            MedirShiftType shiftType = shiftById.get(row.getShiftTypeId());
            if (shiftType == null) {
                continue;
            }
            Integer flag = flagExtractor.get(shiftType);
            if (flag != null && flag == 1) {
                count++;
            }
        }
        return count;
    }

    private MedirShiftType pickShiftType(
            int requiredHeadcount,
            int requiredZhong,
            int requiredLin,
            int daytimeCount,
            int zhongCount,
            int linCount,
            int staffFullDayCount,
            int staffManualFullDayCount,
            MedirShiftType zhong,
            MedirShiftType lin,
            MedirShiftType guisuiQuan,
            MedirShiftType xiu) {
        int dayNeed = Math.max(0, requiredHeadcount - daytimeCount);
        if (dayNeed <= 0) {
            return xiu;
        }
        // 单日「中」不足时：优先让本周已满 2 个全天的人上「中」，避免「先全员凑中」把尚未凑满周维度的人长期派成中（如多日人工锁定值班链后只剩少数可生成格）。
        if (zhongCount < requiredZhong && staffFullDayCount >= 2) {
            return zhong;
        }
        // 本人尚未满 2 个全天，且当日仍缺「结构临」：派临（兼顾日与周）。
        if (staffFullDayCount < 2 && linCount < requiredLin) {
            return lin;
        }
        // 单日仍缺「中」：由包括未满 2 个全天者在内的剩余上班人员补中（后续 promote / 互换再调周维度）。
        if (zhongCount < requiredZhong) {
            return zhong;
        }
        // 本人尚未满 2 个全天，当日「中」已达标：继续派临直至个人满 2 个全天。
        if (staffFullDayCount < 2) {
            return lin;
        }
        // 已有「手工确认」的 2 个全天（如骨髓全×2）时，禁止继续补「临」；避免出现「2 骨髓全 + 额外临」。
        // 其余场景允许临时超过 2，留给后处理回调，避免某些人员（如被值班链锁定）长期 <2 无法借调。
        if (staffManualFullDayCount >= 2) {
            return zhong;
        }
        if (linCount < requiredLin) {
            return lin;
        }
        return zhong;
    }

    private String cellKey(Long staffId, LocalDate workDate) {
        return staffId + "@" + workDate;
    }

    /**
     * 每天轮转人员顺序，避免固定排序导致同一人长期拿到同类班次。
     */
    private List<MedirStaff> rotateStaffOrder(List<MedirStaff> staffs, int dayOffset) {
        if (staffs.isEmpty()) {
            return List.of();
        }
        int size = staffs.size();
        int offset = Math.floorMod(dayOffset, size);
        if (offset == 0) {
            return staffs;
        }
        List<MedirStaff> rotated = new ArrayList<>(size);
        rotated.addAll(staffs.subList(offset, size));
        rotated.addAll(staffs.subList(0, offset));
        return rotated;
    }

    private MedirRosterWeek ensureWeek(Long rosterWeekId) {
        return I18nPreconditions.checkNotNull(
                rosterWeekMapper.findById(rosterWeekId), NOT_FOUND, "error.rosterWeek.notFound");
    }

    private RosterWeekResponse toWeekResponse(MedirRosterWeek w) {
        return new RosterWeekResponse(
                w.getId(), w.getTeamId(), w.getWeekStartDate(), w.getYearLabel(),
                w.getStatus(), w.getVersion(), w.getRemark(), w.getCreatedAt(), w.getUpdatedAt());
    }

    private RosterCellResponse toCellResponse(MedirRosterCell c) {
        return new RosterCellResponse(
                c.getId(), c.getRosterWeekId(), c.getStaffId(), c.getWorkDate(), c.getShiftTypeId(),
                c.getPostId(), c.getPostLabel(), c.getValidationExempt(), c.getExemptReason(), c.getRemark(),
                c.getCreatedAt(), c.getUpdatedAt());
    }

    private RosterWeekStaffPostResponse toStaffPostResponse(MedirRosterWeekStaffPost r) {
        return new RosterWeekStaffPostResponse(
                r.getId(), r.getRosterWeekId(), r.getStaffId(), r.getDisplayPostId(), r.getDisplayLabel(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    private record AutoWeekendStat(
            int weekendFullAuto,
            int lastWeekendAuto
    ) {
    }

    /**
     * 生成排班诊断：按周统计每人「结构临」天数（与 counts_as_lin_for_structure 一致，含临、骨髓全等）。
     * DEBUG 时每人一行；若周次数不等于 2 再打 INFO，便于未开 DEBUG 时 grep「structureLin anomaly」。
     */
    private void logStructureLinWeekAudit(
            String phase,
            Long rosterWeekId,
            LocalDate weekStart,
            List<MedirStaff> staffs,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById,
            Long teamId) {
        int minZhongCfg = resolveIntConfig(teamId, CONFIG_KEY_STRUCTURE_MIN_ZHONG, 2);
        for (MedirStaff staff : staffs) {
            int linDays = 0;
            for (MedirRosterCell row : finalRows) {
                if (!row.getStaffId().equals(staff.getId())) {
                    continue;
                }
                MedirShiftType st = shiftById.get(row.getShiftTypeId());
                if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                    linDays++;
                }
            }
            String codes = buildStaffWeekShiftCodes(staff.getId(), weekStart, finalRows, shiftById);
            if (log.isDebugEnabled()) {
                log.debug(
                        "Roster structureLin audit [{}] weekId={} staffId={} name={} structureLinDays={} shiftCodesWeek={} minZhongCfg={}",
                        phase,
                        rosterWeekId,
                        staff.getId(),
                        staff.getName(),
                        linDays,
                        codes,
                        minZhongCfg);
            }
            if (linDays != 2) {
                log.info(
                        "Roster structureLin anomaly [{}] weekId={} staffId={} name={} structureLinDays={} expect=2 shiftCodesWeek={} minZhongCfg={}",
                        phase,
                        rosterWeekId,
                        staff.getId(),
                        staff.getName(),
                        linDays,
                        codes,
                        minZhongCfg);
            }
        }
    }

    private String buildStaffWeekShiftCodes(
            Long staffId, LocalDate weekStart, List<MedirRosterCell> rows, Map<Long, MedirShiftType> shiftById) {
        Map<LocalDate, String> byDate = new HashMap<>();
        for (MedirRosterCell c : rows) {
            if (!c.getStaffId().equals(staffId)) {
                continue;
            }
            MedirShiftType st = shiftById.get(c.getShiftTypeId());
            byDate.put(c.getWorkDate(), st == null ? "?" : st.getTypeCode());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(byDate.getOrDefault(weekStart.plusDays(i), "-"));
        }
        return sb.toString();
    }

    /**
     * INFO：竖向「每日结构临」人数 vs {@link #resolveRequiredStructureLin}；横向每人「休」天数 vs {@link #buildWeeklyRestTargets(MedirRosterWeek, List, int[])}。
     * 用于对照需求（工作日 min_lin、周末 min_lin_weekend、休息预算分配）与落库结果；算法非全局最优解，冲突时见日志后再调参或显式优先级。
     */
    private void logInfoRosterConstraintDiagnostics(
            String phase,
            Long rosterWeekId,
            MedirRosterWeek week,
            List<MedirStaff> staffs,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById,
            Map<Long, Integer> targetRestByStaff,
            Map<Long, String> staffIdToName) {
        Long teamId = week.getTeamId();
        LocalDate weekStart = week.getWeekStartDate();
        for (int i = 0; i < 7; i++) {
            LocalDate workDate = weekStart.plusDays(i);
            int requiredMinLin = resolveRequiredStructureLin(teamId, workDate);
            List<String> contributors = new ArrayList<>();
            int actualLin = 0;
            for (MedirRosterCell c : finalRows) {
                if (!workDate.equals(c.getWorkDate())) {
                    continue;
                }
                MedirShiftType st = shiftById.get(c.getShiftTypeId());
                if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                    actualLin++;
                    Long sid = c.getStaffId();
                    String nm = staffIdToName == null ? "?" : staffIdToName.getOrDefault(sid, "?");
                    String code = st.getTypeCode() == null ? "?" : st.getTypeCode();
                    contributors.add(nm + ":" + code);
                }
            }
            String match;
            if (actualLin < requiredMinLin) {
                match = "DEFICIT";
            } else if (actualLin > requiredMinLin) {
                match = "SURPLUS";
            } else {
                match = "OK";
            }
            log.info(
                    "Roster gen dailyStructureLin phase={} weekId={} workDate={} dow={} requiredMinLin={} actualLinCount={} match={} contributors={}",
                    phase,
                    rosterWeekId,
                    workDate,
                    workDate.getDayOfWeek(),
                    requiredMinLin,
                    actualLin,
                    match,
                    String.join(",", contributors));
        }
        for (MedirStaff staff : staffs) {
            int restActual = 0;
            for (MedirRosterCell c : finalRows) {
                if (!staff.getId().equals(c.getStaffId())) {
                    continue;
                }
                if (isRestShift(c.getShiftTypeId(), shiftById)) {
                    restActual++;
                }
            }
            int restTarget = targetRestByStaff.getOrDefault(staff.getId(), 0);
            String restMatch = restActual >= restTarget ? "OK" : "BELOW_TARGET";
            log.info(
                    "Roster gen restBudget phase={} weekId={} staffId={} name={} restDaysActual={} restTargetFromHeadcountSlack={} match={}",
                    phase,
                    rosterWeekId,
                    staff.getId(),
                    staff.getName(),
                    restActual,
                    restTarget,
                    restMatch);
        }
    }

    /**
     * 后处理：尽量保证每人每周**恰好** 2 个全天（临/骨髓全，业务上「至少 2」与「不超过 2」在此对齐为 2）。
     * 不足 2 个的员工：将其 zhong 格子与同天 lin 且 fullDayCount &gt; 2 的员工互换；若无人可借调，则在单日「中」有富余时把纯「中」升为「临」。
     * 超过 2 个的员工：若同天还有 &gt; 2 个 lin，可将其多余的 lin 降为 zhong；若仍超标（跨日分散），在单日「结构临」多于 min_lin 时降为「中」。
     *
     * @param frozenCellKeys 本次生成前已存在的「人员×日期」键，不参与互换与升降（保护用户已保存的自定义班次）。
     */
    private void ensureEachStaffExactlyTwoFullDays(
            MedirRosterWeek week,
            List<MedirRosterCell> finalRows,
            Map<Long, WeekStaffStat> weeklyStats,
            Map<Long, MedirShiftType> shiftById,
            MedirShiftType zhongShift,
            MedirShiftType lin,
            Set<String> frozenCellKeys,
            Map<Long, String> staffIdToName) {
        Map<Long, Integer> count = buildWeeklyFullDayCountsPerStaff(weeklyStats, finalRows, shiftById);

        boolean changed = true;
        int safety = 0;
        while (changed && safety < 50) {
            changed = false;
            safety++;
            for (Map.Entry<Long, Integer> entry : new ArrayList<>(count.entrySet())) {
                Long staffId = entry.getKey();
                int fc = count.get(staffId);
                if (fc == 2) {
                    continue;
                }
                if (fc < 2) {
                    // 不足：找 zhong 换 lin（同天 lin 且 fullDayCount > 2 的人）
                    for (MedirRosterCell cell : finalRows) {
                        if (!cell.getStaffId().equals(staffId)) {
                            continue;
                        }
                        MedirShiftType st = shiftById.get(cell.getShiftTypeId());
                        if (st == null || st.getCountsAsZhongForStructure() == null || st.getCountsAsZhongForStructure() != 1) {
                            continue;
                        }
                        if (isPostProcessFrozen(frozenCellKeys, cell)) {
                            continue;
                        }
                        for (MedirRosterCell other : finalRows) {
                            if (other.getStaffId().equals(staffId) || !other.getWorkDate().equals(cell.getWorkDate())) {
                                continue;
                            }
                            MedirShiftType otherSt = shiftById.get(other.getShiftTypeId());
                            if (otherSt == null || otherSt.getCountsAsLinForStructure() == null || otherSt.getCountsAsLinForStructure() != 1) {
                                continue;
                            }
                            if (count.getOrDefault(other.getStaffId(), 0) <= 2) {
                                continue;
                            }
                            if (isPostProcessFrozen(frozenCellKeys, other)) {
                                continue;
                            }
                            swapShift(cell, other);
                            count.put(staffId, count.get(staffId) + 1);
                            count.put(other.getStaffId(), count.get(other.getStaffId()) - 1);
                            changed = true;
                            break;
                        }
                        if (count.get(staffId) >= 2) {
                            break;
                        }
                    }
                } else {
                    // 超过 2：找多余的 lin 换 zhong（需保证同天「结构临」仍不少于当日下限）
                    Long teamId = week.getTeamId();
                    List<MedirRosterCell> linCells = new ArrayList<>();
                    for (MedirRosterCell cell : finalRows) {
                        if (!cell.getStaffId().equals(staffId)) {
                            continue;
                        }
                        MedirShiftType st = shiftById.get(cell.getShiftTypeId());
                        if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                            linCells.add(cell);
                        }
                    }
                    for (MedirRosterCell extraLin : linCells) {
                        if (count.get(staffId) <= 2) {
                            break;
                        }
                        int dayLinCount = 0;
                        for (MedirRosterCell c : finalRows) {
                            if (c.getWorkDate().equals(extraLin.getWorkDate())) {
                                MedirShiftType st = shiftById.get(c.getShiftTypeId());
                                if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                                    dayLinCount++;
                                }
                            }
                        }
                        int minLinThatDay = resolveRequiredStructureLin(teamId, extraLin.getWorkDate());
                        if (dayLinCount > minLinThatDay) {
                            if (isPostProcessFrozen(frozenCellKeys, extraLin)) {
                                continue;
                            }
                            extraLin.setShiftTypeId(zhongShift.getId());
                            count.put(staffId, count.get(staffId) - 1);
                            changed = true;
                        }
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            Map<Long, Integer> afterSwap = buildWeeklyFullDayCountsPerStaff(weeklyStats, finalRows, shiftById);
            for (Map.Entry<Long, Integer> e : afterSwap.entrySet()) {
                if (e.getValue() < 2) {
                    log.debug(
                            "Roster ensureEach afterSwapLoop stillBelow2 weekId={} staffId={} name={} structureLinDays={} (before promoteZhong)",
                            week.getId(),
                            e.getKey(),
                            staffIdToName == null ? "?" : staffIdToName.getOrDefault(e.getKey(), "?"),
                            e.getValue());
                }
            }
        }

        promoteZhongToLinWhenBelowTwoFullDays(week, finalRows, shiftById, lin, frozenCellKeys, staffIdToName);
        // 单日「结构临」与配置下限/上限对齐（与仅按周度降临不同）：修周五多 1 个临、周日缺 1 个临等竖向问题。
        promoteZhongToMeetDailyMinStructureLin(
                week, finalRows, shiftById, lin, frozenCellKeys, staffIdToName);
        demoteExcessDailyStructureLin(week, finalRows, shiftById, zhongShift, frozenCellKeys, staffIdToName);
        // 单日降临「临」可能使某人周度结构全天不足 2，再尝试按周升临（与 promoteZhongToMeetDailyMinStructureLin 互补）。
        promoteZhongToLinWhenBelowTwoFullDays(week, finalRows, shiftById, lin, frozenCellKeys, staffIdToName);
        demoteExcessFullDaysPerWeek(week, finalRows, shiftById, zhongShift, frozenCellKeys);
    }

    /** 补全前已落库、本次不得被后处理改动的单元格。 */
    private boolean isPostProcessFrozen(Set<String> frozenCellKeys, MedirRosterCell cell) {
        return frozenCellKeys != null
                && !frozenCellKeys.isEmpty()
                && frozenCellKeys.contains(cellKey(cell.getStaffId(), cell.getWorkDate()));
    }

    /**
     * 以单元格为准统计每人每周「结构临」天数；并对周表内出现的 staffId 与班组人员表对齐，避免遗漏 0 全天人员。
     */
    private Map<Long, Integer> buildWeeklyFullDayCountsPerStaff(
            Map<Long, WeekStaffStat> weeklyStats,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById) {
        Map<Long, Integer> count = new HashMap<>();
        for (Long staffId : weeklyStats.keySet()) {
            count.put(staffId, 0);
        }
        for (MedirRosterCell row : finalRows) {
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                count.merge(row.getStaffId(), 1, Integer::sum);
            }
        }
        return count;
    }

    /**
     * 在无人可「借调」全天的情况下：若某人仍不足 2 个全天，且某日「中」多于结构下限，将该日一个纯「中」升为「临」。
     */
    private void promoteZhongToLinWhenBelowTwoFullDays(
            MedirRosterWeek week,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById,
            MedirShiftType linShift,
            Set<String> frozenCellKeys,
            Map<Long, String> staffIdToName) {
        Long teamId = week.getTeamId();
        int requiredZhong = resolveIntConfig(teamId, CONFIG_KEY_STRUCTURE_MIN_ZHONG, 2);

        Map<Long, Integer> fullDayByStaff = new HashMap<>();
        for (MedirRosterCell row : finalRows) {
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                fullDayByStaff.merge(row.getStaffId(), 1, Integer::sum);
            }
        }

        Set<Long> staffIds = new HashSet<>();
        for (MedirRosterCell row : finalRows) {
            staffIds.add(row.getStaffId());
        }

        for (Long staffId : staffIds) {
            int need = 2 - fullDayByStaff.getOrDefault(staffId, 0);
            if (need <= 0) {
                continue;
            }
            List<MedirRosterCell> zhongCells = new ArrayList<>();
            for (MedirRosterCell row : finalRows) {
                if (!row.getStaffId().equals(staffId)) {
                    continue;
                }
                MedirShiftType st = shiftById.get(row.getShiftTypeId());
                if (st != null && "ZHONG".equals(st.getTypeCode())) {
                    zhongCells.add(row);
                }
            }
            zhongCells.sort(Comparator.comparing(MedirRosterCell::getWorkDate));
            for (MedirRosterCell cell : zhongCells) {
                if (need <= 0) {
                    break;
                }
                if (isPostProcessFrozen(frozenCellKeys, cell)) {
                    continue;
                }
                LocalDate d = cell.getWorkDate();
                int zhongOnDay = countStructureZhongOnDate(finalRows, shiftById, d);
                if (zhongOnDay > requiredZhong) {
                    cell.setShiftTypeId(linShift.getId());
                    fullDayByStaff.merge(staffId, 1, Integer::sum);
                    need--;
                }
            }
            if (need > 0 && log.isDebugEnabled()) {
                String nm = staffIdToName == null ? "?" : staffIdToName.getOrDefault(staffId, "?");
                StringBuilder dayDetail = new StringBuilder();
                for (MedirRosterCell cell : zhongCells) {
                    LocalDate d = cell.getWorkDate();
                    int zhongOnDay = countStructureZhongOnDate(finalRows, shiftById, d);
                    boolean frozen = isPostProcessFrozen(frozenCellKeys, cell);
                    if (dayDetail.length() > 0) {
                        dayDetail.append(" | ");
                    }
                    dayDetail.append(d)
                            .append(":teamZhong=")
                            .append(zhongOnDay)
                            .append(",minZhong=")
                            .append(requiredZhong)
                            .append(",frozen=")
                            .append(frozen);
                }
                log.debug(
                        "Roster promoteZhong unresolved weekId={} staffId={} name={} remainNeed={} "
                                + "typeCodeZhongCellCount={} perDayTeamZhongVsMin={}",
                        week.getId(),
                        staffId,
                        nm,
                        need,
                        zhongCells.size(),
                        dayDetail);
            }
        }
    }

    private int countStructureZhongOnDate(
            List<MedirRosterCell> finalRows, Map<Long, MedirShiftType> shiftById, LocalDate workDate) {
        int n = 0;
        for (MedirRosterCell row : finalRows) {
            if (!workDate.equals(row.getWorkDate())) {
                continue;
            }
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsZhongForStructure() != null && st.getCountsAsZhongForStructure() == 1) {
                n++;
            }
        }
        return n;
    }

    private int countStructureLinOnDate(
            List<MedirRosterCell> finalRows, Map<Long, MedirShiftType> shiftById, LocalDate workDate) {
        int n = 0;
        for (MedirRosterCell row : finalRows) {
            if (!workDate.equals(row.getWorkDate())) {
                continue;
            }
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                n++;
            }
        }
        return n;
    }

    private MedirRosterCell findCellForStaffOnDate(
            List<MedirRosterCell> finalRows, Long staffId, LocalDate workDate) {
        for (MedirRosterCell row : finalRows) {
            if (staffId.equals(row.getStaffId()) && workDate.equals(row.getWorkDate())) {
                return row;
            }
        }
        return null;
    }

    /**
     * 周末双休均上班且两天均为纯「中」时，为满足 REQ-GEN-05 / REQ-WE-05，不得把其中任一天的「中」升为「临」。
     */
    private boolean weekendSatSunBothPureZhongLocked(
            Long staffId,
            LocalDate weekStart,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById) {
        LocalDate sat = weekStart.plusDays(5);
        LocalDate sun = weekStart.plusDays(6);
        MedirRosterCell cSat = findCellForStaffOnDate(finalRows, staffId, sat);
        MedirRosterCell cSun = findCellForStaffOnDate(finalRows, staffId, sun);
        if (cSat == null || cSun == null) {
            return false;
        }
        MedirShiftType stSat = shiftById.get(cSat.getShiftTypeId());
        MedirShiftType stSun = shiftById.get(cSun.getShiftTypeId());
        if (stSat == null || stSun == null) {
            return false;
        }
        if (isRestShift(stSat) || isRestShift(stSun)) {
            return false;
        }
        return "ZHONG".equals(stSat.getTypeCode()) && "ZHONG".equals(stSun.getTypeCode());
    }

    /**
     * 单日「结构临」不足配置下限时，将非冻结的纯「中」升为「临」（在仍满足当日 structure.min_zhong 的前提下）。
     * 解决：周维度已凑满 2 个全天后，周末某日仍缺 1 个结构临（日志 DEFICIT）无人被 {@link #promoteZhongToLinWhenBelowTwoFullDays} 处理。
     */
    private void promoteZhongToMeetDailyMinStructureLin(
            MedirRosterWeek week,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById,
            MedirShiftType linShift,
            Set<String> frozenCellKeys,
            Map<Long, String> staffIdToName) {
        Long teamId = week.getTeamId();
        LocalDate weekStart = week.getWeekStartDate();
        int requiredZhong = resolveIntConfig(teamId, CONFIG_KEY_STRUCTURE_MIN_ZHONG, 2);
        for (int i = 0; i < 7; i++) {
            LocalDate workDate = weekStart.plusDays(i);
            int minLin = resolveRequiredStructureLin(teamId, workDate);
            int safety = 0;
            while (countStructureLinOnDate(finalRows, shiftById, workDate) < minLin && safety < 16) {
                safety++;
                int zhongBefore = countStructureZhongOnDate(finalRows, shiftById, workDate);
                if (zhongBefore <= requiredZhong) {
                    log.info(
                            "Roster gen dailyStructureLin promoteToMin skip weekId={} workDate={} minLin={} actualLin={} "
                                    + "teamZhong={} minZhong={} reason=wouldBreakMinZhong",
                            week.getId(),
                            workDate,
                            minLin,
                            countStructureLinOnDate(finalRows, shiftById, workDate),
                            zhongBefore,
                            requiredZhong);
                    break;
                }
                MedirRosterCell chosen = null;
                for (MedirRosterCell row : finalRows) {
                    if (!workDate.equals(row.getWorkDate())) {
                        continue;
                    }
                    MedirShiftType st = shiftById.get(row.getShiftTypeId());
                    if (st == null || !"ZHONG".equals(st.getTypeCode())) {
                        continue;
                    }
                    if (isPostProcessFrozen(frozenCellKeys, row)) {
                        continue;
                    }
                    if (weekendSatSunBothPureZhongLocked(row.getStaffId(), weekStart, finalRows, shiftById)) {
                        continue;
                    }
                    chosen = row;
                    break;
                }
                if (chosen == null) {
                    log.info(
                            "Roster gen dailyStructureLin promoteToMin skip weekId={} workDate={} minLin={} actualLin={} "
                                    + "teamZhong={} minZhong={} reason=noEligiblePureZhongOrWeekendBothZhongLock",
                            week.getId(),
                            workDate,
                            minLin,
                            countStructureLinOnDate(finalRows, shiftById, workDate),
                            zhongBefore,
                            requiredZhong);
                    break;
                }
                chosen.setShiftTypeId(linShift.getId());
                log.info(
                        "Roster gen dailyStructureLin promoteToMin weekId={} workDate={} staffId={} name={} minLin={}",
                        week.getId(),
                        workDate,
                        chosen.getStaffId(),
                        staffIdToName == null ? "?" : staffIdToName.getOrDefault(chosen.getStaffId(), "?"),
                        minLin);
            }
        }
    }

    /**
     * 单日「结构临」超过配置下限时，将多余格子降为「中」：优先降 {@code LIN}，尽量保留「骨髓全」等业务强约束班次。
     * 解决：骨髓全 + 两格「临」同日计数为 3，而每人周度仍 ≤2 时 {@link #demoteExcessFullDaysPerWeek} 不会触发。
     */
    private void demoteExcessDailyStructureLin(
            MedirRosterWeek week,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById,
            MedirShiftType zhongShift,
            Set<String> frozenCellKeys,
            Map<Long, String> staffIdToName) {
        Long teamId = week.getTeamId();
        LocalDate weekStart = week.getWeekStartDate();
        for (int i = 0; i < 7; i++) {
            LocalDate workDate = weekStart.plusDays(i);
            int minLin = resolveRequiredStructureLin(teamId, workDate);
            int safety = 0;
            while (countStructureLinOnDate(finalRows, shiftById, workDate) > minLin && safety < 16) {
                safety++;
                List<MedirRosterCell> linCells = new ArrayList<>();
                for (MedirRosterCell row : finalRows) {
                    if (!workDate.equals(row.getWorkDate())) {
                        continue;
                    }
                    MedirShiftType st = shiftById.get(row.getShiftTypeId());
                    if (st == null || st.getCountsAsLinForStructure() == null || st.getCountsAsLinForStructure() != 1) {
                        continue;
                    }
                    if (isPostProcessFrozen(frozenCellKeys, row)) {
                        continue;
                    }
                    linCells.add(row);
                }
                linCells.sort(
                        (a, b) -> {
                            MedirShiftType sta = shiftById.get(a.getShiftTypeId());
                            MedirShiftType stb = shiftById.get(b.getShiftTypeId());
                            String ca = sta != null ? sta.getTypeCode() : "";
                            String cb = stb != null ? stb.getTypeCode() : "";
                            boolean aLin = "LIN".equals(ca);
                            boolean bLin = "LIN".equals(cb);
                            if (aLin != bLin) {
                                return Boolean.compare(!aLin, !bLin);
                            }
                            return Long.compare(a.getStaffId(), b.getStaffId());
                        });
                if (linCells.isEmpty()) {
                    break;
                }
                MedirRosterCell victim = linCells.get(0);
                String prevCode =
                        shiftById.get(victim.getShiftTypeId()) != null
                                ? shiftById.get(victim.getShiftTypeId()).getTypeCode()
                                : "?";
                victim.setShiftTypeId(zhongShift.getId());
                log.info(
                        "Roster gen dailyStructureLin demoteSurplus weekId={} workDate={} staffId={} name={} minLin={} "
                                + "demotedTypeWas={}",
                        week.getId(),
                        workDate,
                        victim.getStaffId(),
                        staffIdToName == null ? "?" : staffIdToName.getOrDefault(victim.getStaffId(), "?"),
                        minLin,
                        prevCode);
            }
        }
    }

    /**
     * 每人每周至多 2 个「结构临」：若仍多于 2 个，优先在「结构临」多于 min_lin 的日期将多余格子降为「中」。
     */
    private void demoteExcessFullDaysPerWeek(
            MedirRosterWeek week,
            List<MedirRosterCell> finalRows,
            Map<Long, MedirShiftType> shiftById,
            MedirShiftType zhongShift,
            Set<String> frozenCellKeys) {
        Long teamId = week.getTeamId();

        Map<Long, Integer> fullDayByStaff = new HashMap<>();
        for (MedirRosterCell row : finalRows) {
            MedirShiftType st = shiftById.get(row.getShiftTypeId());
            if (st != null && st.getCountsAsLinForStructure() != null && st.getCountsAsLinForStructure() == 1) {
                fullDayByStaff.merge(row.getStaffId(), 1, Integer::sum);
            }
        }

        Set<Long> staffIds = new HashSet<>();
        for (MedirRosterCell row : finalRows) {
            staffIds.add(row.getStaffId());
        }

        for (Long staffId : staffIds) {
            int fc = fullDayByStaff.getOrDefault(staffId, 0);
            if (fc <= 2) {
                continue;
            }
            int excess = fc - 2;
            while (excess > 0) {
                MedirRosterCell best = null;
                int bestDayLin = -1;
                for (MedirRosterCell row : finalRows) {
                    if (!row.getStaffId().equals(staffId)) {
                        continue;
                    }
                    MedirShiftType st = shiftById.get(row.getShiftTypeId());
                    if (st == null || st.getCountsAsLinForStructure() == null || st.getCountsAsLinForStructure() != 1) {
                        continue;
                    }
                    if (isPostProcessFrozen(frozenCellKeys, row)) {
                        continue;
                    }
                    int dayLin = countStructureLinOnDate(finalRows, shiftById, row.getWorkDate());
                    int minLinThatDay = resolveRequiredStructureLin(teamId, row.getWorkDate());
                    if (dayLin > minLinThatDay && dayLin > bestDayLin) {
                        bestDayLin = dayLin;
                        best = row;
                    }
                }
                if (best == null) {
                    break;
                }
                best.setShiftTypeId(zhongShift.getId());
                excess--;
                fullDayByStaff.merge(staffId, -1, Integer::sum);
            }
        }
    }

    private void swapShift(MedirRosterCell a, MedirRosterCell b) {
        long tmp = a.getShiftTypeId();
        a.setShiftTypeId(b.getShiftTypeId());
        b.setShiftTypeId(tmp);
    }

    private record GenerateOptions(
            String strategy,
            int respectManualConfirmed,
            int dryRun,
            String reason
    ) {
    }

    private interface ShiftFlagExtractor {

        Integer get(MedirShiftType shiftType);
    }

    private static final class WeekStaffStat {

        private int assignedCount;
        private int restCount;
        private int fullDayCount;
    }
}
