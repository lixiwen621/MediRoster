package com.mediroster.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
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

    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String CONFLICT = "CONFLICT";
    private static final String CONFIG_KEY_WEEKEND_FULL_SHIFT_TYPES = "stats.weekend_full_shift_types";
    private static final String CONFIG_KEY_HEADCOUNT_WEEKDAY_134 = "headcount.weekday_134";
    private static final String CONFIG_KEY_HEADCOUNT_WEEKDAY_25 = "headcount.weekday_25";
    private static final String CONFIG_KEY_HEADCOUNT_WEEKEND_HOLIDAY = "headcount.weekend_holiday";
    private static final String CONFIG_KEY_STRUCTURE_MIN_ZHONG = "structure.min_zhong";
    private static final String CONFIG_KEY_STRUCTURE_MIN_LIN = "structure.min_lin";
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
        I18nPreconditions.checkArgument(n != 0, "OPTIMISTIC_LOCK", "error.optimisticLock");
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

    @Transactional
    public void replaceCells(Long rosterWeekId, RosterCellReplaceRequest req) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        Long teamId = week.getTeamId();
        List<MedirRosterCell> rows = new ArrayList<>();
        List<RosterCellReplaceRequest.CellItem> items = req.cells() == null ? List.of() : req.cells();
        for (RosterCellReplaceRequest.CellItem item : items) {
            MedirStaff staff = staffMapper.findById(item.staffId());
            I18nPreconditions.checkArgument(
                    staff != null && staff.getDeletedAt() == null && teamId.equals(staff.getTeamId()),
                    "INVALID_STAFF", "error.roster.invalidStaff", item.staffId());
            I18nPreconditions.checkNotNull(
                    shiftTypeMapper.findById(item.shiftTypeId()),
                    "INVALID_SHIFT", "error.roster.invalidShift", item.shiftTypeId());
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
    }

    @Transactional
    public RosterWeekGenerateResponse generateWeek(Long rosterWeekId, RosterWeekGenerateRequest req) {
        MedirRosterWeek week = ensureWeek(rosterWeekId);
        GenerateOptions options = normalizeGenerateOptions(req);
        List<MedirStaff> staffs = new ArrayList<>(staffMapper.findByTeamId(week.getTeamId(), false));
        staffs.sort(Comparator.comparing(MedirStaff::getSortOrder).thenComparing(MedirStaff::getId));
        I18nPreconditions.checkArgument(!staffs.isEmpty(), "CONFLICT", "error.roster.generate.noStaff");

        Map<Long, MedirShiftType> shiftById = new HashMap<>();
        Map<String, MedirShiftType> shiftByCode = new HashMap<>();
        for (MedirShiftType shiftType : shiftTypeMapper.findAll()) {
            shiftById.put(shiftType.getId(), shiftType);
            shiftByCode.put(shiftType.getTypeCode(), shiftType);
        }
        MedirShiftType xiu = I18nPreconditions.checkNotNull(
                shiftByCode.get("XIU"), "CONFLICT", "error.roster.generate.missingShiftType", "XIU");
        MedirShiftType zhong = I18nPreconditions.checkNotNull(
                shiftByCode.get("ZHONG"), "CONFLICT", "error.roster.generate.missingShiftType", "ZHONG");
        MedirShiftType lin = I18nPreconditions.checkNotNull(
                shiftByCode.get("LIN"), "CONFLICT", "error.roster.generate.missingShiftType", "LIN");

        List<MedirRosterCell> existingRows = rosterCellMapper.findByRosterWeekId(rosterWeekId);
        Map<String, MedirRosterCell> cellByStaffAndDate = new HashMap<>();
        for (MedirRosterCell row : existingRows) {
            cellByStaffAndDate.put(cellKey(row.getStaffId(), row.getWorkDate()), row);
        }

        boolean overwriteAll = STRATEGY_OVERWRITE_ALL.equals(options.strategy()) && options.respectManualConfirmed() == 0;
        List<MedirRosterCell> finalRows = new ArrayList<>();
        if (!overwriteAll) {
            finalRows.addAll(existingRows);
        } else {
            cellByStaffAndDate.clear();
        }
        int overwrittenCellCount = overwriteAll ? existingRows.size() : 0;
        int skippedConfirmedCount = !overwriteAll && options.respectManualConfirmed() == 1 ? existingRows.size() : 0;
        Map<Long, Integer> targetRestByStaff = buildWeeklyRestTargets(week, staffs);
        Map<Long, WeekStaffStat> weeklyStats = initWeeklyStats(staffs, finalRows, shiftById);

        int generatedCellCount = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate workDate = week.getWeekStartDate().plusDays(i);
            int requiredHeadcount = resolveRequiredHeadcount(week.getTeamId(), workDate);
            int requiredZhong = resolveIntConfig(week.getTeamId(), CONFIG_KEY_STRUCTURE_MIN_ZHONG, 2);
            int requiredLin = resolveIntConfig(week.getTeamId(), CONFIG_KEY_STRUCTURE_MIN_LIN, 2);

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
            int workNeeded = Math.max(0, requiredHeadcount - daytimeCount);
            if (workNeeded > candidates.size()) {
                workNeeded = candidates.size();
            }
            Set<Long> workAssignees = pickWorkAssignees(candidates, workNeeded, weeklyStats, targetRestByStaff);

            for (MedirStaff staff : candidates) {
                MedirShiftType picked = workAssignees.contains(staff.getId())
                        ? pickShiftType(
                                requiredHeadcount, requiredZhong, requiredLin, daytimeCount, zhongCount, linCount, zhong, lin, xiu)
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
                }
            }
        }

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
                        "INVALID_STAFF", "error.roster.invalidStaff", item.staffId());
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
                    staffMap.get(item.staffId()), "INVALID_STAFF", "error.roster.invalidStaff", item.staffId());
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
            if (satCode != null && weekendFullShiftTypeCodes.contains(satCode)) {
                weekendFullAuto++;
            }
            if (sunCode != null && weekendFullShiftTypeCodes.contains(sunCode)) {
                weekendFullAuto++;
            }
            int lastWeekendAuto = "ZHONG".equals(satCode) && "ZHONG".equals(sunCode) ? 1 : 0;
            autoMap.put(staff.getId(), new AutoWeekendStat(weekendFullAuto, lastWeekendAuto));
        }
        return autoMap;
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
                "CONFLICT",
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

    private Map<Long, Integer> buildWeeklyRestTargets(MedirRosterWeek week, List<MedirStaff> staffs) {
        Map<Long, Integer> targetMap = new HashMap<>();
        if (staffs.isEmpty()) {
            return targetMap;
        }
        int totalHeadcount = 0;
        for (int i = 0; i < 7; i++) {
            totalHeadcount += resolveRequiredHeadcount(week.getTeamId(), week.getWeekStartDate().plusDays(i));
        }
        int totalSlots = staffs.size() * 7;
        int totalRests = Math.max(0, totalSlots - totalHeadcount);
        int base = totalRests / staffs.size();
        int extra = totalRests % staffs.size();
        int offset = Math.floorMod((int) week.getWeekStartDate().toEpochDay(), staffs.size());
        for (int i = 0; i < staffs.size(); i++) {
            int rotated = Math.floorMod(i - offset, staffs.size());
            int target = base + (rotated < extra ? 1 : 0);
            targetMap.put(staffs.get(i).getId(), target);
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
            int ta = targetRestByStaff.getOrDefault(a.getId(), 2);
            int tb = targetRestByStaff.getOrDefault(b.getId(), 2);
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

    private int resolveRequiredHeadcount(Long teamId, LocalDate workDate) {
        DayOfWeek dayOfWeek = workDate.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.FRIDAY) {
            return resolveIntConfig(teamId, CONFIG_KEY_HEADCOUNT_WEEKDAY_25, 5);
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
            MedirShiftType zhong,
            MedirShiftType lin,
            MedirShiftType xiu) {
        int dayNeed = Math.max(0, requiredHeadcount - daytimeCount);
        if (dayNeed <= 0) {
            return xiu;
        }
        if (zhongCount < requiredZhong) {
            return zhong;
        }
        if (linCount < requiredLin) {
            return lin;
        }
        return lin;
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
    }
}
