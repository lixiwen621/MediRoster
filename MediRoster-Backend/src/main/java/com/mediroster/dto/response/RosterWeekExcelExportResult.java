package com.mediroster.dto.response;

/**
 * 排班周 Excel 导出结果（字节 + 建议下载文件名）。
 *
 * @author tongguo.li
 */
public record RosterWeekExcelExportResult(byte[] content, String downloadFilename) {
}
