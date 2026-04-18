-- 参考数据：班组、岗位、班次类型（在 001 执行后运行）
-- 可按环境调整；班次行为标志与需求文档 §2、§5 对齐。

SET NAMES utf8mb4;

INSERT INTO medir_team (id, team_code, team_name, description, enabled) VALUES
(1, 'LJ', '临检组', '示例班组', 1);

INSERT INTO medir_post (id, post_code, post_name, description, sort_order, enabled) VALUES
(1, 'P1', '1岗', '血常规', 1, 1),
(2, 'P2', '2岗', '骨髓岗：复片、外周血涂片、骨髓', 2, 1),
(3, 'P3', '3岗', '凝血、血流变上机', 3, 1),
(4, 'P4', '4岗', '尿常规', 4, 1),
(5, 'P5', '5岗', '体液流水线', 5, 1);

-- 班次：行为标志按需求初值，后续可在管理端改 medir_shift_type 表
INSERT INTO medir_shift_type (
    id, type_code, name_zh, sort_order,
    is_rest, is_duty_zhong, is_duty_da, is_qiban, is_small_night,
    counts_daytime_headcount, counts_weekend_full_day_stat,
    counts_as_zhong_for_structure, counts_as_lin_for_structure,
    next_day_must_rest, enabled
) VALUES
(1, 'LIN',       '临',     10, 0,0,0,0,0, 1, 1, 0, 1, 0, 1),
(2, 'ZHONG',     '中',     20, 0,0,0,0,0, 1, 0, 1, 0, 0, 1),
(3, 'GUISUI_ZHONG', '骨髓中', 25, 0,0,0,0,0, 1, 0, 0, 0, 0, 1),
(4, 'GUISUI_QUAN',  '骨髓全', 30, 0,0,0,0,0, 1, 1, 0, 1, 0, 1),
(5, 'XIU',       '休',     5,  1,0,0,0,0, 0, 0, 0, 0, 0, 1),
(6, 'QIBAN',     '√',      40, 0,0,0,1,0, 0, 0, 0, 0, 0, 1),
(7, 'ZHI_ZHONG', '值中',   50, 0,1,0,0,0, 0, 0, 0, 0, 0, 1),
(8, 'ZHI_DA',    '值大',   60, 0,0,1,0,0, 0, 0, 0, 0, 0, 1),
(9, 'XIAOYE',    '小夜',   70, 0,0,0,0,1, 1, 0, 1, 0, 1, 1),
(10,'XUEXI',     '学习',   80, 0,0,0,0,0, 1, 0, 0, 0, 0, 1);

-- 键值规则示例（team_id=0 全局；班组可覆盖同名 key）
INSERT INTO medir_config (team_id, config_key, config_value, value_type, category, description, sort_order) VALUES
(0, 'headcount.weekday_134', '4', 'INT', 'HEADCOUNT', '周一、三、四白天上班人数', 10),
(0, 'headcount.weekday_2',  '6', 'INT', 'HEADCOUNT', '周二白天上班人数（可额外加1岗）', 11),
(0, 'headcount.weekday_5',  '5', 'INT', 'HEADCOUNT', '周五白天上班人数', 12),
(0, 'headcount.weekend_holiday', '3', 'INT', 'HEADCOUNT', '周末及法定节假日白天人数', 12),
(0, 'structure.min_zhong', '2', 'INT', 'HEADCOUNT', '每天至少中班数（小夜算中）', 20),
(0, 'structure.min_lin',   '2', 'INT', 'HEADCOUNT', '周一至周五每天至少「结构临」（骨髓全算临）', 21),
(0, 'structure.min_lin_weekend', '1', 'INT', 'HEADCOUNT', '周六、周日每天至少「结构临」', 22),
(0, 'duty.chain', '["ZHI_ZHONG|ZHI_DA","QIBAN","XIU"]', 'JSON', 'DUTY', '值班三连：值中|值大 → √ → 休', 30),
(0, 'bone_marrow.weekdays', '["TUE","FRI"]', 'JSON', 'BONE', '骨髓全固定周几', 40),
(0, 'post_rotation.weeks', '2', 'INT', 'ROTATION', '1-3-4-5 岗轮换周期（周）', 50),
(0, 'export.title', '临检组排班表', 'STRING', 'EXPORT', 'Excel 标题', 60),
(0, 'export.footer.small_night', '小夜固定上中班，上至中午1点。', 'STRING', 'EXPORT', 'Excel 脚注', 61),
(0, 'stats.weekend_full_shift_types', '["LIN","GUISUI_QUAN"]', 'JSON', 'EXPORT', '周末全天统计计入的班次编码', 70);
