package com.mediroster.excel;

import com.google.common.base.Strings;
import com.mediroster.dto.response.RosterCellResponse;
import com.mediroster.dto.response.RosterWeekStaffPostResponse;
import com.mediroster.dto.response.RosterWeekWeekendStatResponse;
import com.mediroster.entity.MedirRosterWeek;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 排班周 Excel 导出（A4 纵向、标题「临检组排班表」）。
 *
 * @author tongguo.li
 */
public final class RosterWeekExcelExporter {

    private static final String[] WEEKDAY_ZH = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private RosterWeekExcelExporter() {
    }

    /**
     * 生成 .xlsx 字节流。
     *
     * @param week          排班周
     * @param sheetTitle    表头标题（默认临检组排班表）
     * @param footerText    页脚说明（可空）
     * @param shiftNameById 班次 id → 中文名
     * @param cells         当周单元格
     * @param staffPosts    周岗位摘要
     * @param weekendStats  周末统计（含最终值）
     * @param staffOrder    行顺序：人员 id 列表（已排序）
     * @param staffNameById 人员 id → 姓名
     */
    public static byte[] export(
            MedirRosterWeek week,
            String sheetTitle,
            String footerText,
            Map<Long, String> shiftNameById,
            List<RosterCellResponse> cells,
            List<RosterWeekStaffPostResponse> staffPosts,
            List<RosterWeekWeekendStatResponse> weekendStats,
            List<Long> staffOrder,
            Map<Long, String> staffNameById) throws IOException {

        Map<Long, Map<LocalDate, RosterCellResponse>> cellMap = new HashMap<>();
        for (RosterCellResponse c : cells) {
            cellMap.computeIfAbsent(c.staffId(), ignored -> new HashMap<>()).put(c.workDate(), c);
        }
        Map<Long, RosterWeekStaffPostResponse> postByStaff = new HashMap<>();
        for (RosterWeekStaffPostResponse p : staffPosts) {
            postByStaff.put(p.staffId(), p);
        }
        Map<Long, RosterWeekWeekendStatResponse> statByStaff = new HashMap<>();
        for (RosterWeekWeekendStatResponse s : weekendStats) {
            statByStaff.put(s.staffId(), s);
        }

        int colCount = 11;
        LocalDate start = week.getWeekStartDate();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("排班");
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.LEFT);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setWrapText(true);

            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setWrapText(true);

            XSSFCellStyle footerStyle = workbook.createCellStyle();
            footerStyle.setWrapText(true);
            footerStyle.setVerticalAlignment(VerticalAlignment.TOP);

            int rowIdx = 0;
            XSSFRow titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(22);
            XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheetTitle);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            XSSFCell yearCell = titleRow.createCell(10);
            yearCell.setCellValue(String.valueOf(week.getYearLabel()));
            yearCell.setCellStyle(titleStyle);

            XSSFRow headRow = sheet.createRow(rowIdx++);
            headRow.setHeightInPoints(36);
            String[] headers = {
                    "姓名", "", "", "", "", "", "", "", "岗位", "周末全天", "上周末"
            };
            for (int c = 0; c < colCount; c++) {
                XSSFCell hc = headRow.createCell(c);
                if (c == 0) {
                    hc.setCellValue(headers[0]);
                } else if (c >= 1 && c <= 7) {
                    LocalDate d = start.plusDays(c - 1);
                    hc.setCellValue(WEEKDAY_ZH[c - 1] + "\n" + formatMd(d));
                } else {
                    hc.setCellValue(headers[c]);
                }
                hc.setCellStyle(headerStyle);
            }

            for (Long staffId : staffOrder) {
                XSSFRow dataRow = sheet.createRow(rowIdx++);
                String name = staffNameById.getOrDefault(staffId, "");
                setCell(dataRow, 0, name, dataStyle);
                Map<LocalDate, RosterCellResponse> byDate = cellMap.getOrDefault(staffId, Map.of());
                for (int d = 0; d < 7; d++) {
                    LocalDate workDate = start.plusDays(d);
                    RosterCellResponse cell = byDate.get(workDate);
                    String text = cell == null ? "" : formatShiftCell(cell, shiftNameById);
                    setCell(dataRow, 1 + d, text, dataStyle);
                }
                RosterWeekStaffPostResponse post = postByStaff.get(staffId);
                String postText = post == null ? "" : Strings.nullToEmpty(post.displayLabel());
                setCell(dataRow, 8, postText, dataStyle);

                RosterWeekWeekendStatResponse stat = statByStaff.get(staffId);
                String wf = stat == null || stat.weekendFullFinal() == null ? "" : String.valueOf(stat.weekendFullFinal());
                String lw = stat == null || stat.lastWeekendFinal() == null ? "" : String.valueOf(stat.lastWeekendFinal());
                setCell(dataRow, 9, wf, dataStyle);
                setCell(dataRow, 10, lw, dataStyle);
            }

            if (!Strings.isNullOrEmpty(footerText)) {
                rowIdx++;
                XSSFRow footRow = sheet.createRow(rowIdx);
                footRow.setHeightInPoints(120);
                XSSFCell fc = footRow.createCell(0);
                fc.setCellValue(footerText);
                fc.setCellStyle(footerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 10));
            }

            sheet.setColumnWidth(0, 10 * 256);
            for (int c = 1; c <= 7; c++) {
                sheet.setColumnWidth(c, 9 * 256);
            }
            sheet.setColumnWidth(8, 12 * 256);
            sheet.setColumnWidth(9, 10 * 256);
            sheet.setColumnWidth(10, 10 * 256);

            XSSFPrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setPaperSize(XSSFPrintSetup.A4_PAPERSIZE);
            printSetup.setLandscape(false);
            sheet.setHorizontallyCenter(true);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static void setCell(XSSFRow row, int col, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private static String formatMd(LocalDate d) {
        return d.getMonthValue() + "." + d.getDayOfMonth();
    }

    private static String formatShiftCell(RosterCellResponse c, Map<Long, String> shiftNameById) {
        String name = shiftNameById.getOrDefault(c.shiftTypeId(), "");
        if (Strings.isNullOrEmpty(c.postLabel())) {
            return name;
        }
        return name + c.postLabel();
    }
}
