-- v0.15：休息目标、周四五弹性人头、周末全天统计排除名单、周末临轮序（在 001/002 之后执行）
SET NAMES utf8mb4;

INSERT INTO medir_config (team_id, config_key, config_value, value_type, category, description, sort_order) VALUES
(0, 'rest.target_min_days', '2', 'INT', 'HEADCOUNT', '每人每周目标休息天数（与 §20 REQ-GEN-01 一致）', 15),
(0, 'headcount.flex_floor_thursday', '3', 'INT', 'HEADCOUNT', '弹性减人头时周四最低白天人数', 16),
(0, 'headcount.flex_floor_friday', '3', 'INT', 'HEADCOUNT', '弹性减人头时周五最低白天人数', 17),
(0, 'stats.weekend_full_exclude_names', '["程海荣"]', 'JSON', 'EXPORT', '周末全天自动统计排除（按姓名，JSON 数组）', 71),
(0, 'roster.weekend_lin_rotate_by_sort_order', '1', 'INT', 'ROTATION', '周末排「临」时按人员 sort_order 自上而下轮序', 18)
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    description = VALUES(description),
    sort_order = VALUES(sort_order);

INSERT INTO medir_rule_meta (rule_code, category, label_zh, value_type, default_value, options_json, help_text, sort_order, enabled) VALUES
('rest.target_min_days', 'HEADCOUNT', '每人每周目标休息天数', 'INT', '2', NULL, '§20 REQ-GEN-01', 15, 1),
('headcount.flex_floor_thursday', 'HEADCOUNT', '弹性周四最低人头', 'INT', '3', NULL, '§20', 16, 1),
('headcount.flex_floor_friday', 'HEADCOUNT', '弹性周五最低人头', 'INT', '3', NULL, '§20', 17, 1),
('stats.weekend_full_exclude_names', 'EXPORT', '周末全天统计排除姓名', 'JSON', '["程海荣"]', NULL, '§20 REQ-GEN-07', 71, 1),
('roster.weekend_lin_rotate_by_sort_order', 'ROTATION', '周末临按行序轮', 'INT', '1', NULL, '§20 REQ-GEN-06', 18, 1)
ON DUPLICATE KEY UPDATE
    label_zh = VALUES(label_zh),
    help_text = VALUES(help_text),
    sort_order = VALUES(sort_order);
