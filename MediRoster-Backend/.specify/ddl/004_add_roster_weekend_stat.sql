-- 周末统计两列人工覆盖（周末全天、上周末）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS medir_roster_weekend_stat (
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    roster_week_id         BIGINT UNSIGNED NOT NULL COMMENT '排班周 id（medir_roster_week.id）',
    staff_id               BIGINT UNSIGNED NOT NULL COMMENT '人员 id（medir_staff.id）',
    weekend_full_override  INT             NULL COMMENT '周末全天人工覆盖值，NULL=未覆盖',
    last_weekend_override  INT             NULL COMMENT '上周末人工覆盖值，NULL=未覆盖',
    override_reason        VARCHAR(512)    NULL COMMENT '人工覆盖原因',
    created_at             DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at             DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_rw_weekend_stat (roster_week_id, staff_id),
    KEY idx_medir_rw_weekend_stat_staff (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='排班周周末统计人工覆盖';
