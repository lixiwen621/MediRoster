-- 补充：周末「结构临」下限单独配置（已有库执行一次即可；全新初始化若已含 002 最新种子可跳过）
SET NAMES utf8mb4;

INSERT IGNORE INTO medir_config (team_id, config_key, config_value, value_type, category, description, sort_order, enabled) VALUES
(0, 'structure.min_lin_weekend', '1', 'INT', 'HEADCOUNT', '周六、周日每天至少「结构临」（骨髓全算临）', 22, 1);
