-- 规则元数据：供管理端动态渲染配置表单（在 001/002 之后执行，可重复执行前请先清空 medir_rule_meta 或改用 INSERT IGNORE）
SET NAMES utf8mb4;

INSERT INTO medir_rule_meta (rule_code, category, label_zh, value_type, default_value, options_json, help_text, sort_order, enabled) VALUES
('headcount.weekday_134', 'HEADCOUNT', '周一三四白天人数', 'INT', '4', NULL, '对应 REQ-DAILY-02', 10, 1),
('headcount.weekday_25', 'HEADCOUNT', '周二五白天人数', 'INT', '5', NULL, '对应 REQ-DAILY-01', 11, 1),
('headcount.weekend_holiday', 'HEADCOUNT', '周末及节假日白天人数', 'INT', '3', NULL, '对应 REQ-DAILY-03', 12, 1),
('structure.min_zhong', 'HEADCOUNT', '每天至少「中」数（小夜算中）', 'INT', '2', NULL, 'REQ-DAILY-04', 20, 1),
('structure.min_lin', 'HEADCOUNT', '每天至少「临」侧（骨髓全算临）', 'INT', '2', NULL, 'REQ-DAILY-05', 21, 1),
('duty.chain', 'DUTY', '值班三连（JSON 班次编码序列）', 'JSON', '["ZHI_ZHONG|ZHI_DA","QIBAN","XIU"]', NULL, 'REQ-DUTY-01', 30, 1),
('bone_marrow.weekdays', 'BONE', '骨髓全固定星期', 'JSON', '["TUE","FRI"]', NULL, 'REQ-BM-01', 40, 1),
('post_rotation.weeks', 'ROTATION', '岗位轮换周期（周）', 'INT', '2', NULL, 'REQ-POST-07', 50, 1),
('export.title', 'EXPORT', 'Excel 表头标题', 'STRING', '临检组排班表', NULL, 'REQ-XLS-02', 60, 1),
('export.footer.small_night', 'EXPORT', '脚注：小夜说明', 'STRING', '小夜固定上中班，上至中午1点。', NULL, '附图脚注', 61, 1),
('stats.weekend_full_shift_types', 'EXPORT', '周末全天统计计入的 type_code（JSON 数组）', 'JSON', '["LIN","GUISUI_QUAN"]', NULL, 'REQ-STAT-02', 70, 1)
ON DUPLICATE KEY UPDATE
    label_zh = VALUES(label_zh),
    help_text = VALUES(help_text),
    sort_order = VALUES(sort_order);
