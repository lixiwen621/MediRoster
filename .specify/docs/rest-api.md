# MediRoster REST API（v1 / 排班主数据）

**基地址**：`http://<host>:<port>`，默认端口 **8084**（见 `application.yml` 的 `SERVER_PORT`）。

**内容类型**：请求/响应均为 `application/json`（日期类字段见下文「约定」）。

---

## 统一响应 `ApiResponse<T>`

所有接口返回外层结构如下（`data` 为具体业务数据，成功时非 `null`，失败时多为 `null`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 是否成功 |
| `code` | string | 成功时一般为 `"0"`；失败时为业务/错误码 |
| `message` | string | 提示文案 |
| `data` | T | 业务载荷；无体成功（如删除）时可为 `null` |

失败时：`success=false`；HTTP：`400` 参数/校验，`404` 资源不存在（`code=NOT_FOUND`），`409` 冲突（`code=CONFLICT` / `OPTIMISTIC_LOCK` / `DUPLICATE_KEY` 等）。

**文件导出（例外）**：`GET .../export` 成功时响应体为 **Excel 二进制**（`.xlsx`），**不使用**外层 `ApiResponse` JSON；失败时仍按全局异常返回 JSON。

---

## OpenAPI

| 说明 | URL |
|------|-----|
| OpenAPI JSON | `GET /api/v1/openapi` |
| Swagger UI | `GET /swagger-ui.html` |

---

## 约定

- **国际化（i18n）**：失败时 `message` 为已按语言解析的文案。请求头可带 **`Accept-Language`**（如 `zh-CN`、`en`）；未指定时默认简体中文。
- **路径参数、查询参数名**与下表一致（**camelCase**）。
- **日期**：`LocalDate` 使用 **`yyyy-MM-dd`**；`LocalDateTime` 为 **ISO-8601**。
- **整型布尔/开关**：未单独说明时，一般为 **`0`/`1`**（`0`=否，`1`=是）。
- **人员状态 `status`**：`1` 在职，`2` 停用。
- **排班周状态 `status`**：`1` 草稿，`2` 已发布。
- **排班周更新**：`PUT` 须带当前 `version`，否则可能 `409` + `OPTIMISTIC_LOCK`。
- **覆盖单元格**：`PUT .../cells` 为先删后插；`cells` 为 `null` 或 `[]` 表示清空当周单元格。
- **周末统计两列**（`周末全天`、`上周末`）：默认自动计算，支持人工覆盖。
- **半自动排班生成**：默认仅补全无单元格的格子；覆盖整周需二次确认。
- **Excel 导出**：A4 纵向，标题默认「临检组排班表」。

---

## 健康检查

### `GET /api/v1/health`

无参数。**`data`**：`{ status: "UP" }`

---

## 班组 `/api/v1/medir/teams`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/teams` | 列表（无参） |
| GET | `/api/v1/medir/teams/{id}` | 详情 |
| POST | `/api/v1/medir/teams` | 创建（TeamUpsertRequest） |
| PUT | `/api/v1/medir/teams/{id}` | 更新（同 TeamUpsertRequest） |
| DELETE | `/api/v1/medir/teams/{id}` | 删除 |

**TeamUpsertRequest**: `teamCode`(string, 必填), `teamName`(string, 必填), `description`(string, 可选), `enabled`(integer, 可选, 默认1)
**TeamResponse**: `id`, `teamCode`, `teamName`, `description`, `enabled`, `createdAt`, `updatedAt`

---

## 岗位 `/api/v1/medir/posts`

> 全局岗位字典（无 `teamId` 筛选）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/posts` | 列表 |
| GET | `/api/v1/medir/posts/{id}` | 详情 |
| POST | `/api/v1/medir/posts` | 创建 |
| PUT | `/api/v1/medir/posts/{id}` | 更新 |
| DELETE | `/api/v1/medir/posts/{id}` | 删除 |

**PostUpsertRequest**: `postCode`(string, 必填), `postName`(string, 必填), `description`(string, 可选), `sortOrder`(integer, 必填), `enabled`(integer, 必填)
**PostResponse**: `id`, `postCode`, `postName`, `description`, `sortOrder`, `enabled`, `createdAt`, `updatedAt`

---

## 班次类型 `/api/v1/medir/shift-types`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/shift-types` | 列表 |
| GET | `/api/v1/medir/shift-types/{id}` | 详情 |
| PUT | `/api/v1/medir/shift-types/{id}` | 全量更新（ShiftTypeUpdateRequest） |

> 注：班次类型无 POST/DELETE，字典由种子数据初始化。

**ShiftTypeUpdateRequest**: `typeCode`, `nameZh`, `sortOrder`, `isRest`, `isDutyZhong`, `isDutyDa`, `isQiban`, `isSmallNight`, `countsDaytimeHeadcount`, `countsWeekendFullDayStat`, `countsAsZhongForStructure`, `countsAsLinForStructure`, `nextDayMustRest`, `enabled`（全为 integer 必填）
**ShiftTypeResponse**: 同上 + `id`, `createdAt`, `updatedAt`

---

## 人员 `/api/v1/medir/staff`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/staff?teamId={teamId}` | 列表（必填 teamId，可选 includeDeleted） |
| GET | `/api/v1/medir/staff/{id}` | 详情 |
| POST | `/api/v1/medir/staff` | 创建 |
| PUT | `/api/v1/medir/staff/{id}` | 更新 |
| DELETE | `/api/v1/medir/staff/{id}` | 软删除 |

**StaffUpsertRequest**: `teamId`(long), `name`(string), `employeeNo`(string, 可选), `phone`(string, 可选), `email`(string, 可选), `memberType`(string), `sortOrder`(integer), `status`(integer: 1在职/2停用), `fixedPostId`(long, 可选), `remark`(string, 可选)
**StaffResponse**: 同上 + `id`, `deletedAt`, `createdAt`, `updatedAt`

---

## 人员能力 `/api/v1/medir/staff/{staffId}/capabilities`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../capabilities` | 列表 |
| POST | `.../capabilities` | 创建 |
| DELETE | `.../capabilities/{capabilityId}` | 删除 |

**StaffCapabilityUpsertRequest**: `capabilityCode`(string), `enabled`(integer)
**StaffCapabilityResponse**: `id`, `staffId`, `capabilityCode`, `enabled`, `createdAt`, `updatedAt`

---

## 规则键值 `/api/v1/medir/config`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/config?teamId={teamId}` | 列表（可选 teamId） |
| GET | `/api/v1/medir/config/{id}` | 详情 |
| POST | `/api/v1/medir/config` | 创建 |
| PUT | `/api/v1/medir/config/{id}` | 更新 |
| DELETE | `/api/v1/medir/config/{id}` | 删除 |

**ConfigUpsertRequest**: `teamId`(long, 0=全局), `configKey`(string), `configValue`(string), `valueType`(string), `category`(string, 可选), `description`(string, 可选), `sortOrder`(integer), `enabled`(integer)
**ConfigResponse**: 同上 + `id`, `createdAt`, `updatedAt`

---

## 规则元数据 `/api/v1/medir/rule-meta`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/rule-meta` | 列表（配置页表单定义） |

**RuleMetaResponse**: `id`, `ruleCode`, `category`, `labelZh`, `valueType`, `defaultValue`, `optionsJson`(string, 可空), `helpText`, `sortOrder`, `enabled`, `createdAt`, `updatedAt`

---

## 排班周 `/api/v1/medir/roster-weeks`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/roster-weeks?teamId={teamId}&year={year}` | 列表（必填 teamId, year） |
| GET | `/api/v1/medir/roster-weeks/{id}` | 详情 |
| POST | `/api/v1/medir/roster-weeks` | 创建 |
| PUT | `/api/v1/medir/roster-weeks/{id}` | 更新（需 version） |
| DELETE | `/api/v1/medir/roster-weeks/{id}` | 删除 |

**RosterWeekCreateRequest**: `teamId`(long), `weekStartDate`(string, date), `yearLabel`(integer), `status`(integer: 1草稿/2已发布), `remark`(string, 可选)
**RosterWeekUpdateRequest**: `yearLabel`(integer), `status`(integer), `remark`(string, 可选), `version`(integer, 必填)
**RosterWeekResponse**: `id`, `teamId`, `weekStartDate`, `yearLabel`, `status`, `version`, `remark`, `createdAt`, `updatedAt`

### 排班单元格 `/api/v1/medir/roster-weeks/{weekId}/cells`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../cells` | 读取当周全部单元格 |
| PUT | `.../cells` | 覆盖写入（先删后插） |

**RosterCellReplaceRequest**: `cells`(CellItem[] 或 null)
**CellItem**: `staffId`(long), `workDate`(string, date), `shiftTypeId`(long), `postId`(long, 可选), `postLabel`(string, 可选), `validationExempt`(integer), `exemptReason`(string, 可选), `remark`(string, 可选)
**RosterCellResponse**: 同上 + `id`, `rosterWeekId`, `createdAt`, `updatedAt`

### 排班生成 `/api/v1/medir/roster-weeks/{weekId}/generate`

**POST** — 按策略自动生成排班单元格。

**RosterWeekGenerateRequest**: `strategy`(string: FILL_UNCONFIRMED/OVERWRITE_ALL), `respectManualConfirmed`(integer, 默认1), `dryRun`(integer, 默认0), `reason`(string, 可选)
**RosterWeekGenerateResponse**: `weekId`, `strategy`, `generatedCellCount`, `overwrittenCellCount`, `skippedConfirmedCount`, `dryRun`, `message`

### Excel 导出 `/api/v1/medir/roster-weeks/{weekId}/export`

**GET** — 下载 `.xlsx` 文件。成功时返回二进制流（非 JSON）。

可选查询参数：`filename`(string, 建议文件名)

### 人员岗位 `/api/v1/medir/roster-weeks/{weekId}/staff-posts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../staff-posts` | 列表 |
| PUT | `.../staff-posts` | 覆盖写入 |

**RosterStaffPostReplaceRequest**: `items`[{ `staffId`(long), `displayPostId`(long, 可选), `displayLabel`(string, 可选) }]
**RosterWeekStaffPostResponse**: `id`, `rosterWeekId`, `staffId`, `displayPostId`, `displayLabel`, `createdAt`, `updatedAt`

### 周末统计 `/api/v1/medir/roster-weeks/{weekId}/weekend-stats`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../weekend-stats` | 读取（含自动值与最终值） |
| PUT | `.../weekend-stats` | 覆盖人工值 |

**RosterWeekWeekendStatReplaceRequest**: `items`[{ `staffId`(long), `weekendFullOverride`(integer, null=取消), `lastWeekendOverride`(integer, null=取消), `overrideReason`(string, 可选) }]
**RosterWeekWeekendStatResponse**: `id`, `rosterWeekId`, `staffId`, `weekendFullAuto`, `weekendFullFinal`, `lastWeekendAuto`, `lastWeekendFinal`, `isOverridden`, `overrideReason`, `updatedAt`

---

## 日历日 `/api/v1/medir/calendar-days`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/medir/calendar-days?from={date}&to={date}` | 列表（必填 from, to） |
| GET | `/api/v1/medir/calendar-days/{id}` | 详情 |
| POST | `/api/v1/medir/calendar-days` | 创建 |
| PUT | `/api/v1/medir/calendar-days/{id}` | 更新 |
| DELETE | `/api/v1/medir/calendar-days/{id}` | 删除 |

**CalendarDayUpsertRequest**: `calDate`(string, date), `dayType`(string), `holidayName`(string, 可选)
**CalendarDayResponse**: `id`, `calDate`, `dayType`, `holidayName`, `createdAt`, `updatedAt`

---

## 常用规则键（medir_config）

`team_id=0` 表示全局。常用键：
- `headcount.weekday_134` / `headcount.weekday_25` / `headcount.weekend_holiday`
- `structure.min_zhong` / `structure.min_lin`
- `duty.chain`（JSON）
- `bone_marrow.weekdays`（JSON）
- `post_rotation.weeks`
- `export.title`、`export.footer.small_night`
- `stats.weekend_full_shift_types`（JSON）
