-- MediRoster 检验科排班 — MySQL 8 物理模型（DDL）
-- 约定（与项目 .cursorrules 一致）：
--   * 字符集 utf8mb4；引擎 InnoDB
--   * 不声明 FOREIGN KEY，表间关系由应用层维护；需要处加普通索引
--   * 业务表含 created_at / updated_at；人员支持软删除 deleted_at
--
-- 说明：班次行为标志、人数阈值等既可由 seed 初始化，也可由管理端页面维护；
--       键值类规则存 medir_config；需表单元数据的规则存 medir_rule_meta（可选）。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 班组（如：临检组）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_team (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    team_code     VARCHAR(64)  NOT NULL COMMENT '班组编码，唯一',
    team_name     VARCHAR(128) NOT NULL COMMENT '班组名称，如临检组',
    description   VARCHAR(512) NULL COMMENT '备注',
    enabled       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_team_code (team_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='排班班组';

-- ---------------------------------------------------------------------------
-- 岗位字典（1～5 岗等）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_post (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_code     VARCHAR(32)  NOT NULL COMMENT '岗位编码，如 P1、P2',
    post_name     VARCHAR(64)  NOT NULL COMMENT '简称，如 1岗',
    description   VARCHAR(512) NULL COMMENT '职责说明（血常规、尿常规等）',
    sort_order    INT          NOT NULL DEFAULT 0 COMMENT '展示排序',
    enabled       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_post_code (post_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='岗位字典';

-- ---------------------------------------------------------------------------
-- 班次类型（10 种名称 + 校验/统计用行为标志）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_shift_type (
    id                              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    type_code                       VARCHAR(32)  NOT NULL COMMENT '编码，如 LIN、ZHONG',
    name_zh                         VARCHAR(32)  NOT NULL COMMENT '中文名称，与需求一致（含√）',
    sort_order                      INT          NOT NULL DEFAULT 0 COMMENT '展示排序',
    is_rest                         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否休息班次（休）；布尔语义 0=否(false) 1=是(true)',
    is_duty_zhong                   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否值中；布尔语义 0=否(false) 1=是(true)',
    is_duty_da                      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否值大；布尔语义 0=否(false) 1=是(true)',
    is_qiban                        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否起班√；布尔语义 0=否(false) 1=是(true)',
    is_small_night                  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否小夜；布尔语义 0=否(false) 1=是(true)',
    counts_daytime_headcount        TINYINT      NOT NULL DEFAULT 1 COMMENT '是否计入白天上班人数（值中/值大配置为0）；布尔语义 0=否(false) 1=是(true)',
    counts_weekend_full_day_stat    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否计入「周末全天」统计（临、骨髓全等）；布尔语义 0=否(false) 1=是(true)',
    counts_as_zhong_for_structure   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否计入「至少2个中」结构（含小夜）；布尔语义 0=否(false) 1=是(true)',
    counts_as_lin_for_structure     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否计入「至少2个临」结构（骨髓全算临）；布尔语义 0=否(false) 1=是(true)',
    next_day_must_rest              TINYINT      NOT NULL DEFAULT 0 COMMENT '是否要求次日必须休（小夜）；布尔语义 0=否(false) 1=是(true)',
    enabled                         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用该班次类型；0=停用 1=启用（等价布尔：0=false 1=true）',
    created_at                      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at                      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_shift_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='班次类型及行为标志（供校验引擎与统计读取）';

-- ---------------------------------------------------------------------------
-- 人员（可页面增删改；扩展人员用 member_type 区分）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_staff (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    team_id         BIGINT UNSIGNED NOT NULL COMMENT '所属班组 medir_team.id',
    name            VARCHAR(64)     NOT NULL COMMENT '姓名',
    employee_no     VARCHAR(64)     NULL COMMENT '工号/院内编号',
    phone           VARCHAR(32)     NULL COMMENT '手机',
    email           VARCHAR(128)    NULL COMMENT '邮箱',
    member_type     VARCHAR(32)     NOT NULL DEFAULT 'core' COMMENT '成员类型：core核心排班 extended扩展（规则可弱化）',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '列表/排班表行序',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '人员状态：1=在职 2=停用',
    fixed_post_id   BIGINT UNSIGNED NULL COMMENT '固定主岗 medir_post.id（如固定1岗）；非固定为空',
    remark          VARCHAR(512)    NULL COMMENT '备注',
    deleted_at      DATETIME(3)     NULL COMMENT '软删除时间',
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_medir_staff_team (team_id),
    KEY idx_medir_staff_status (status),
    KEY idx_medir_staff_team_status (team_id, status),
    KEY idx_medir_staff_fixed_post (fixed_post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='排班人员';

-- ---------------------------------------------------------------------------
-- 人员能力/标签（如：骨髓全可排人员、是否参与某轮序）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_staff_capability (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    staff_id        BIGINT UNSIGNED NOT NULL COMMENT 'medir_staff.id',
    capability_code VARCHAR(64)     NOT NULL COMMENT '能力编码，如 BONE_MARROW_FULL',
    enabled         TINYINT         NOT NULL DEFAULT 1 COMMENT '1启用',
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_staff_cap (staff_id, capability_code),
    KEY idx_medir_staff_cap_code (capability_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='人员能力标签';

-- ---------------------------------------------------------------------------
-- 键值配置（人数、值班链、骨髓周几、轮换周期等；前端可做成配置页）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_config (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    team_id       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=全局默认；非0=medir_team.id 覆盖',
    config_key    VARCHAR(128)    NOT NULL COMMENT '配置键，如 headcount.wd_25',
    config_value  TEXT            NOT NULL COMMENT '配置值，建议 JSON 或纯文本',
    value_type    VARCHAR(32)     NOT NULL DEFAULT 'STRING' COMMENT 'STRING INT BOOLEAN JSON DECIMAL',
    category      VARCHAR(64)     NULL COMMENT '分组：HEADCOUNT DUTY ROTATION BONE EXPORT HOLIDAY',
    description   VARCHAR(512)    NULL COMMENT '说明',
    sort_order    INT             NOT NULL DEFAULT 0 COMMENT '界面排序',
    enabled       TINYINT         NOT NULL DEFAULT 1 COMMENT '1启用',
    created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_config_scope_key (team_id, config_key),
    KEY idx_medir_config_team (team_id),
    KEY idx_medir_config_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='业务规则键值配置';

-- ---------------------------------------------------------------------------
-- 规则元数据（可选：驱动管理端表单渲染、校验提示）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_rule_meta (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_code      VARCHAR(128)    NOT NULL COMMENT '与 medir_config.config_key 对齐或子规则码',
    category       VARCHAR(64)     NOT NULL COMMENT '分组',
    label_zh       VARCHAR(256)    NOT NULL COMMENT '中文标题',
    value_type     VARCHAR(32)     NOT NULL COMMENT 'STRING INT BOOLEAN JSON',
    default_value  VARCHAR(1024)   NULL COMMENT '默认值',
    options_json   TEXT            NULL COMMENT '枚举选项 JSON，如 [{"v":"4","l":"4人"}]',
    help_text      VARCHAR(1024)   NULL COMMENT '帮助说明',
    sort_order     INT             NOT NULL DEFAULT 0 COMMENT '排序',
    enabled        TINYINT         NOT NULL DEFAULT 1 COMMENT '1展示',
    created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_rule_meta_code (rule_code),
    KEY idx_medir_rule_meta_cat (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='规则配置页元数据';

-- ---------------------------------------------------------------------------
-- 排班周（自然周：建议 week_start_date 存周一日期）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_roster_week (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    team_id         BIGINT UNSIGNED NOT NULL COMMENT 'medir_team.id',
    week_start_date DATE            NOT NULL COMMENT '本周周一日期',
    year_label      INT             NOT NULL COMMENT '展示用年份，如2026',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '周排班状态：1=草稿 2=已发布',
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    remark          VARCHAR(512)    NULL COMMENT '备注',
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_roster_week (team_id, week_start_date),
    KEY idx_medir_roster_week_team_year (team_id, year_label)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='按周排班头';

-- ---------------------------------------------------------------------------
-- 排班单元格：人 × 日 → 班次 + 可选岗位标注
-- ---------------------------------------------------------------------------
CREATE TABLE medir_roster_cell (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    roster_week_id   BIGINT UNSIGNED NOT NULL COMMENT 'medir_roster_week.id',
    staff_id         BIGINT UNSIGNED NOT NULL COMMENT 'medir_staff.id',
    work_date        DATE            NOT NULL COMMENT '排班日期',
    shift_type_id    BIGINT UNSIGNED NOT NULL COMMENT 'medir_shift_type.id',
    post_id          BIGINT UNSIGNED NULL COMMENT '主岗 medir_post.id，可选',
    post_label       VARCHAR(64)     NULL COMMENT '附加标注：3.5、4+、圈号等',
    validation_exempt TINYINT        NOT NULL DEFAULT 0 COMMENT '1跳过部分校验',
    exempt_reason    VARCHAR(512)    NULL COMMENT '豁免原因',
    remark           VARCHAR(512)    NULL COMMENT '备注',
    created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_roster_cell (roster_week_id, staff_id, work_date),
    KEY idx_medir_roster_cell_week (roster_week_id),
    KEY idx_medir_roster_cell_staff_date (staff_id, work_date),
    KEY idx_medir_roster_cell_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='排班明细单元格';

-- ---------------------------------------------------------------------------
-- 可选：周级每人岗位摘要（纸质表「岗位」列；也可由应用按规则计算不落库）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_roster_week_staff_post (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    roster_week_id  BIGINT UNSIGNED NOT NULL COMMENT 'medir_roster_week.id',
    staff_id        BIGINT UNSIGNED NOT NULL COMMENT 'medir_staff.id',
    display_post_id BIGINT UNSIGNED NULL COMMENT '本周展示主岗 medir_post.id',
    display_label   VARCHAR(64)     NULL COMMENT '如 ① ⑤ 等展示',
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_rw_staff_post (roster_week_id, staff_id),
    KEY idx_medir_rwsp_week (roster_week_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='周视图每人岗位摘要（可选）';

-- ---------------------------------------------------------------------------
-- 可选：法定节假日（用于「周末及节假日3人」等；也可外部同步）
-- ---------------------------------------------------------------------------
CREATE TABLE medir_calendar_day (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    cal_date     DATE            NOT NULL COMMENT '日期',
    day_type     VARCHAR(32)     NOT NULL COMMENT 'workday weekend holiday workday_adjust',
    holiday_name VARCHAR(128)    NULL COMMENT '节假日名称',
    created_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_medir_cal_date (cal_date),
    KEY idx_medir_cal_type (day_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='日历日类型（可选）';
