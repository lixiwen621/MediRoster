# MediRoster REST API（v1 / 排班主数据）

**基地址**：`http://<host>:<port>`，默认端口见 `application.yml` 的 `SERVER_PORT`（默认 `8084`）。

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

**文件导出（例外）**：`GET .../export` 成功时响应体为 **Excel 二进制**（`.xlsx`），**不使用**外层 `ApiResponse` JSON；失败时仍按全局异常返回 JSON（与上表一致）。

---

## OpenAPI（可配合本页给前端 Cursor 对字段）

| 说明 | URL |
|------|-----|
| OpenAPI JSON | `GET /api/v1/openapi` |
| Swagger UI | `GET /swagger-ui.html` |

---

## 约定

- **国际化（i18n）**：失败时 `message` 为**已按语言解析**的文案。请求头可带 **`Accept-Language`**（如 `zh-CN`、`en`）；未指定时默认 **简体中文**。文案文件目录：`src/main/resources/i18n/`（`messages.properties`、`messages_zh_CN.properties`、`messages_en.properties`；Jakarta Bean Validation 默认键亦在同一套 basename 中维护）。
- **路径参数、查询参数名**与下表一致（**camelCase**）。
- **日期**：`LocalDate` 使用 ISO 日期 **`yyyy-MM-dd`**（如 `2026-04-11`）；`LocalDateTime` 为 **ISO-8601**（如 `2026-04-11T12:30:00`）。
- **整型布尔/开关**：未单独说明时，一般为 **`0`/`1`**（`0`=否，`1`=是），与表结构 COMMENT 一致。
- **人员状态 `status`**：`1` 在职，`2` 停用。
- **排班周状态 `status`**：`1` 草稿，`2` 已发布。
- **排班周更新**：`PUT` 须带当前 `version`，否则可能 `409` + `OPTIMISTIC_LOCK`。
- **覆盖单元格**：`PUT .../cells` 为**先删后插**；`cells` 为 `null` 或 `[]` 表示清空当周单元格。
- **周末统计两列**（`周末全天`、`上周末`）：默认可由系统自动计算，同时支持人工覆盖；导出与界面统一展示最终值（覆盖后）。
- **半自动排班生成**：`POST /api/v1/medir/roster-weeks/{weekId}/generate` 已实现。在已通过 `PUT .../cells` 保存部分单元格后，默认仅补全**尚无单元格**的格子；覆盖整周模式需前端二次确认。
- **周排班 Excel 导出**：`GET /api/v1/medir/roster-weeks/{weekId}/export` 下载当周 `.xlsx`（成功时为文件流；**A4 纵向**；表头标题默认「临检组排班表」，可被 `export.title` 覆盖）。

---

## 健康检查

### `GET /api/v1/health`

| 类型 | 参数 |
|------|------|
| 请求 | 无 |

**`data` 类型**：`object`

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | 固定 `"UP"` |

---

## 班组 `/api/v1/medir/teams`

### `GET /api/v1/medir/teams`

| 类型 | 参数 |
|------|------|
| 查询 | 无 |

**`data`**：`TeamResponse[]`（见下表「TeamResponse」）。

### `GET /api/v1/medir/teams/{id}`

| 类型 | 参数 |
|------|------|
| 路径 | `id`：long，班组主键 |

**`data`**：`TeamResponse`。

### `POST /api/v1/medir/teams`

**请求体**：`TeamUpsertRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `teamCode` | string | 是 | 班组编码；**别名**：`code` |
| `teamName` | string | 是 | 班组名称；**别名**：`name` |
| `description` | string | 否 | 备注；**别名**：`remark` |
| `enabled` | integer | 否 | `0`/`1`；**不传时新建默认为 `1`** |

**`data`**：`TeamResponse`。

### `PUT /api/v1/medir/teams/{id}`

| 类型 | 参数 |
|------|------|
| 路径 | `id`：long |

**请求体**：同 `TeamUpsertRequest`。`enabled` 不传则**不修改**原启用状态。

**`data`**：`TeamResponse`。

### `DELETE /api/v1/medir/teams/{id}`

**`data`**：`null`（成功即可）。

---

### `TeamResponse`（班组）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | |
| `teamCode` | string | |
| `teamName` | string | |
| `description` | string | |
| `enabled` | integer | |
| `createdAt` | string (datetime) | |
| `updatedAt` | string (datetime) | |

---

## 岗位 `/api/v1/medir/posts`

> 当前实现为**全局岗位字典**（无 `teamId` 筛选）；列表返回全部岗位。

### `GET /api/v1/medir/posts`

**`data`**：`PostResponse[]`。

### `GET /api/v1/medir/posts/{id}`

| 类型 | 参数 |
|------|------|
| 路径 | `id`：long |

**`data`**：`PostResponse`。

### `POST /api/v1/medir/posts`

**请求体**：`PostUpsertRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `postCode` | string | 是 | 岗位编码 |
| `postName` | string | 是 | 岗位名称 |
| `description` | string | 否 | 描述 |
| `sortOrder` | integer | 是 | 排序 |
| `enabled` | integer | 是 | `0`/`1` |

**`data`**：`PostResponse`。

### `PUT /api/v1/medir/posts/{id}`

**请求体**：`PostUpsertRequest`（同上）。

**`data`**：`PostResponse`。

### `DELETE /api/v1/medir/posts/{id}`

**`data`**：`null`。

---

### `PostResponse`（岗位）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | |
| `postCode` | string | |
| `postName` | string | |
| `description` | string | |
| `sortOrder` | integer | |
| `enabled` | integer | |
| `createdAt` | string (datetime) | |
| `updatedAt` | string (datetime) | |

---

## 班次类型 `/api/v1/medir/shift-types`

### `GET /api/v1/medir/shift-types`

**`data`**：`ShiftTypeResponse[]`。

### `GET /api/v1/medir/shift-types/{id}`

**`data`**：`ShiftTypeResponse`。

### `PUT /api/v1/medir/shift-types/{id}`

**请求体**：`ShiftTypeUpdateRequest`（全量更新）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `typeCode` | string | 是 | 类型编码 |
| `nameZh` | string | 是 | 中文名称（与业务表一致） |
| `sortOrder` | integer | 是 | 排序 |
| `isRest` | integer | 是 | 是否休息类 |
| `isDutyZhong` | integer | 是 | 是否值中 |
| `isDutyDa` | integer | 是 | 是否值大 |
| `isQiban` | integer | 是 | 是否起班 |
| `isSmallNight` | integer | 是 | 是否小夜 |
| `countsDaytimeHeadcount` | integer | 是 | 是否计入白天人数等（见实现） |
| `countsWeekendFullDayStat` | integer | 是 | 周末全天统计等 |
| `countsAsZhongForStructure` | integer | 是 | 结构计数：中 |
| `countsAsLinForStructure` | integer | 是 | 结构计数：临 |
| `nextDayMustRest` | integer | 是 | 次日是否必须休 |
| `enabled` | integer | 是 | 是否启用 |

**`data`**：`ShiftTypeResponse`。

---

### `ShiftTypeResponse`（班次类型）

| 字段 | 类型 |
|------|------|
| `id` | long |
| `typeCode` | string |
| `nameZh` | string |
| `sortOrder` | integer |
| `isRest` | integer |
| `isDutyZhong` | integer |
| `isDutyDa` | integer |
| `isQiban` | integer |
| `isSmallNight` | integer |
| `countsDaytimeHeadcount` | integer |
| `countsWeekendFullDayStat` | integer |
| `countsAsZhongForStructure` | integer |
| `countsAsLinForStructure` | integer |
| `nextDayMustRest` | integer |
| `enabled` | integer |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

---

## 人员 `/api/v1/medir/staff`

### `GET /api/v1/medir/staff`

| 类型 | 参数 |
|------|------|
| 查询 | `teamId`：long，**必填**，所属班组 |
| 查询 | `includeDeleted`：boolean，可选，默认 `false`，是否包含已软删人员 |

**`data`**：`StaffResponse[]`。

### `GET /api/v1/medir/staff/{id}`

**`data`**：`StaffResponse`。

### `POST /api/v1/medir/staff`

**请求体**：`StaffUpsertRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `teamId` | long | 是 | 班组 id |
| `name` | string | 是 | 姓名 |
| `employeeNo` | string | 否 | 工号 |
| `phone` | string | 否 | 手机 |
| `email` | string | 否 | 邮箱 |
| `memberType` | string | 是 | 成员类型（业务约定字符串，如固定/机动等） |
| `sortOrder` | integer | 是 | 排序 |
| `status` | integer | 是 | `1` 在职 `2` 停用 |
| `fixedPostId` | long | 否 | 固定岗位 id，可空 |
| `remark` | string | 否 | 备注 |

**`data`**：`StaffResponse`。

### `PUT /api/v1/medir/staff/{id}`

**请求体**：`StaffUpsertRequest`（同上）。

**`data`**：`StaffResponse`。

### `DELETE /api/v1/medir/staff/{id}`

软删除。**`data`**：`null`。

---

### `StaffResponse`（人员）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | |
| `teamId` | long | |
| `name` | string | |
| `employeeNo` | string | |
| `phone` | string | |
| `email` | string | |
| `memberType` | string | |
| `sortOrder` | integer | |
| `status` | integer | |
| `fixedPostId` | long | 可空 |
| `remark` | string | |
| `deletedAt` | string (datetime) | 软删时间，未删为 `null` |
| `createdAt` | string (datetime) | |
| `updatedAt` | string (datetime) | |

---

## 人员能力 `/api/v1/medir/staff/{staffId}/capabilities`

### `GET ...`

**`data`**：`StaffCapabilityResponse[]`。

### `POST ...`

**请求体**：`StaffCapabilityUpsertRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `capabilityCode` | string | 是 | 能力编码 |
| `enabled` | integer | 是 | `0`/`1` |

**`data`**：`StaffCapabilityResponse`。

### `DELETE .../{capabilityId}`

**`data`**：`null`。

---

### `StaffCapabilityResponse`

| 字段 | 类型 |
|------|------|
| `id` | long |
| `staffId` | long |
| `capabilityCode` | string |
| `enabled` | integer |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

---

## 规则键值 `/api/v1/medir/config`

### `GET /api/v1/medir/config`

| 类型 | 参数 |
|------|------|
| 查询 | `teamId`：long，**可选**；不传=全部；传值=仅该班组（`0` 表示仅全局键） |

**`data`**：`ConfigResponse[]`。

### `GET /api/v1/medir/config/{id}`

**`data`**：`ConfigResponse`。

### `POST /api/v1/medir/config`

**请求体**：`ConfigUpsertRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `teamId` | long | 是 | `0`=全局 |
| `configKey` | string | 是 | 键 |
| `configValue` | string | 是 | 值 |
| `valueType` | string | 是 | 值类型（如 string/json/number） |
| `category` | string | 否 | 分类 |
| `description` | string | 否 | 说明 |
| `sortOrder` | integer | 是 | 排序 |
| `enabled` | integer | 是 | |

**`data`**：`ConfigResponse`。

### `PUT /api/v1/medir/config/{id}`

**请求体**：`ConfigUpsertRequest`（同上）。

**`data`**：`ConfigResponse`。

### `DELETE /api/v1/medir/config/{id}`

**`data`**：`null`。

---

### `ConfigResponse`

| 字段 | 类型 |
|------|------|
| `id` | long |
| `teamId` | long |
| `configKey` | string |
| `configValue` | string |
| `valueType` | string |
| `category` | string |
| `description` | string |
| `sortOrder` | integer |
| `enabled` | integer |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

---

## 规则元数据 `/api/v1/medir/rule-meta`

### `GET /api/v1/medir/rule-meta`

配置页表单定义列表。**`data`**：`RuleMetaResponse[]`。

---

### `RuleMetaResponse`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | |
| `ruleCode` | string | |
| `category` | string | |
| `labelZh` | string | 中文标签 |
| `valueType` | string | |
| `defaultValue` | string | |
| `optionsJson` | string | 可空，JSON 字符串 |
| `helpText` | string | |
| `sortOrder` | integer | |
| `enabled` | integer | |
| `createdAt` | string (datetime) | |
| `updatedAt` | string (datetime) | |

---

## 排班周 `/api/v1/medir/roster-weeks`

### `GET /api/v1/medir/roster-weeks`

| 类型 | 参数 |
|------|------|
| 查询 | `teamId`：long，**必填** |
| 查询 | `year`：integer，**必填**，年份 |

**`data`**：`RosterWeekResponse[]`。

### `GET /api/v1/medir/roster-weeks/{id}`

**`data`**：`RosterWeekResponse`。

### `POST /api/v1/medir/roster-weeks`

**请求体**：`RosterWeekCreateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `teamId` | long | 是 | |
| `weekStartDate` | string (date) | 是 | 周起始日（周一） |
| `yearLabel` | integer | 是 | 年份标签 |
| `status` | integer | 是 | 草稿/已发布 |
| `remark` | string | 否 | |

**`data`**：`RosterWeekResponse`。

### `PUT /api/v1/medir/roster-weeks/{id}`

**请求体**：`RosterWeekUpdateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `yearLabel` | integer | 是 | |
| `status` | integer | 是 | |
| `remark` | string | 否 | |
| `version` | integer | 是 | 乐观锁版本，须与当前一致 |

**`data`**：`RosterWeekResponse`。

### `DELETE /api/v1/medir/roster-weeks/{id}`

**`data`**：`null`。

### `GET /api/v1/medir/roster-weeks/{weekId}/cells`

**`data`**：`RosterCellResponse[]`。

### `PUT /api/v1/medir/roster-weeks/{weekId}/cells`

**请求体**：`RosterCellReplaceRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `cells` | array | 否 | 元素为 `CellItem`；`null` 或 `[]` 清空当周 |

**`CellItem`**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `staffId` | long | 是 | |
| `workDate` | string (date) | 是 | |
| `shiftTypeId` | long | 是 | |
| `postId` | long | 否 | |
| `postLabel` | string | 否 | |
| `validationExempt` | integer | 是 | 是否豁免校验 |
| `exemptReason` | string | 否 | |
| `remark` | string | 否 | |

**`data`**：`null`。

**清空整周排班（前端常用）**

- 用于“重新排班前先清空单元格”场景。
- 请求体传空数组即可：

```json
{
  "cells": []
}
```

- 效果：删除该 `weekId` 下全部单元格（后续可再调用 `POST .../generate` 重新补全）。
- **强约束**：本接口仅做“清空/覆盖单元格”，**不会**隐式触发自动补全；是否补全必须由前端在用户显式点击后单独调用 `POST /api/v1/medir/roster-weeks/{weekId}/generate`。
- 说明：该操作仅清空**单元格**；若还需重置周末两列人工覆盖，请再调用 `PUT .../weekend-stats` 并对目标人员提交 `null` 覆盖值。

### `POST /api/v1/medir/roster-weeks/{weekId}/generate`

按策略自动生成排班单元格。**后端已实现**；请求/响应字段以 **OpenAPI**（`GET /api/v1/openapi`）或 **Swagger UI**（`GET /swagger-ui.html`）为准，本文仅作说明。

- **默认策略 `FILL_UNCONFIRMED`**：只对当周**尚未存在单元格**的「人员 × 工作日」组合生成班次；若某 `staffId` 在某 `workDate` 已有单元格，则视为已人工确认，在 `respectManualConfirmed=1`（默认）时**跳过**该格。
- **`OVERWRITE_ALL` + `respectManualConfirmed=0`**：可整周重算并覆盖已有单元格（高风险，**前端须二次确认**）。
- **试算**：`dryRun=1` 时仅返回统计、**不落库**；`dryRun=0` 时写入数据库。
- **留痕**：`reason` 会参与服务端日志记录，便于追溯。

**请求体**：`RosterWeekGenerateRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `strategy` | string | 否 | 生成策略：`FILL_UNCONFIRMED`（默认） / `OVERWRITE_ALL` |
| `respectManualConfirmed` | integer | 否 | 是否保护已人工确认单元格；默认 `1`。`strategy=OVERWRITE_ALL` 时可置 `0` |
| `dryRun` | integer | 否 | `1`=仅试算不落库，`0`=执行并落库；默认 `0` |
| `reason` | string | 否 | 生成原因/备注（用于留痕） |

**`data`**：`RosterWeekGenerateResponse`。

### `GET /api/v1/medir/roster-weeks/{weekId}/export`

下载指定排班周的 **Excel 排班表**（`.xlsx`），版式与业务需求 **§14**（`.specify/requirements-lab-roster-scheduling.md`）一致：含姓名、周一至周日每日班次（中文名）、岗位摘要、**周末全天** / **上周末**（展示**最终值**，含人工覆盖）；**不导出**「覆盖原因」列（该信息仍在接口 `weekend-stats` 中可查）。表头标题与脚注文案优先来自 `medir_config`（如 `export.title`、`export.footer.small_night`）。

| 类型 | 参数 / 说明 |
|------|-------------|
| 路径 | `weekId`：long，排班周 id |
| 查询 | 无必填。可选 `filename`：string，建议文件名（不含路径）；服务端可忽略或做安全过滤后写入 `Content-Disposition` |

**成功响应**

- **HTTP**：`200 OK`
- **`Content-Type`**：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **`Content-Disposition`**：`attachment`（默认）；建议文件名包含班组、周一起始日、`version`，例如：`临检组_排班_20260413_v0.xlsx`（具体以实现为准）
- **Body**：`.xlsx` 二进制流（**非** `ApiResponse` 包装）

**失败响应**

- 与全局一致：`404` 排班周不存在（`code=NOT_FOUND`）等，**JSON `ApiResponse`**

**前端注意**

- 使用 `fetch`/axios 时需 `responseType: 'arraybuffer'` 或 `blob`，勿按 JSON 解析。
- 导出前若用户在界面有未保存修改，应以产品约定为准（提示先保存或自动保存）；**服务端以当前库内数据为准**。

**状态**：**后端已实现**（Apache POI 生成 `.xlsx`）；版式细调（与纸质表完全一致）可继续迭代。

### `GET /api/v1/medir/roster-weeks/{weekId}/staff-posts`

**`data`**：`RosterWeekStaffPostResponse[]`。

### `PUT /api/v1/medir/roster-weeks/{weekId}/staff-posts`

**请求体**：`RosterStaffPostReplaceRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `items` | array | 是 | 元素为 `Item` |

**`Item`**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `staffId` | long | 是 | |
| `displayPostId` | long | 否 | |
| `displayLabel` | string | 否 | |

**`data`**：`null`。

### `GET /api/v1/medir/roster-weeks/{weekId}/weekend-stats`

读取本周「周末全天」「上周末」两列（含自动值与人工覆盖后的最终值）。

**`data`**：`RosterWeekWeekendStatResponse[]`。

### `PUT /api/v1/medir/roster-weeks/{weekId}/weekend-stats`

覆盖本周「周末全天」「上周末」两列的人工值（未提交某人则保持原值；提交 `null` 可清除该列覆盖并回退自动值）。

**请求体**：`RosterWeekWeekendStatReplaceRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `items` | array | 是 | 元素为 `Item` |

**`Item`**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `staffId` | long | 是 | 人员 id（必须属于该周对应班组） |
| `weekendFullOverride` | integer | 否 | 「周末全天」覆盖值；`null` 表示取消覆盖 |
| `lastWeekendOverride` | integer | 否 | 「上周末」覆盖值；`null` 表示取消覆盖 |
| `overrideReason` | string | 否 | 覆盖原因（建议填写） |

**`data`**：`null`。

---

### `RosterWeekResponse`

| 字段 | 类型 |
|------|------|
| `id` | long |
| `teamId` | long |
| `weekStartDate` | string (date) |
| `yearLabel` | integer |
| `status` | integer |
| `version` | integer |
| `remark` | string |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

### `RosterCellResponse`

| 字段 | 类型 |
|------|------|
| `id` | long |
| `rosterWeekId` | long |
| `staffId` | long |
| `workDate` | string (date) |
| `shiftTypeId` | long |
| `postId` | long |
| `postLabel` | string |
| `validationExempt` | integer |
| `exemptReason` | string |
| `remark` | string |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

### `RosterWeekStaffPostResponse`

| 字段 | 类型 |
|------|------|
| `id` | long |
| `rosterWeekId` | long |
| `staffId` | long |
| `displayPostId` | long |
| `displayLabel` | string |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

### `RosterWeekWeekendStatResponse`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 记录 id |
| `rosterWeekId` | long | 排班周 id |
| `staffId` | long | 人员 id |
| `weekendFullAuto` | integer | 「周末全天」自动统计值 |
| `weekendFullFinal` | integer | 「周末全天」最终展示值（有覆盖时取覆盖值） |
| `lastWeekendAuto` | integer | 「上周末」自动统计值 |
| `lastWeekendFinal` | integer | 「上周末」最终展示值（有覆盖时取覆盖值） |
| `isOverridden` | integer | 是否有人工覆盖（`0/1`） |
| `overrideReason` | string | 最近一次覆盖原因 |
| `updatedAt` | string (datetime) | 最近更新时间 |

### `RosterWeekGenerateRequest`

| 字段 | 类型 | 说明 |
|------|------|------|
| `strategy` | string | `FILL_UNCONFIRMED` / `OVERWRITE_ALL` |
| `respectManualConfirmed` | integer | 是否保护人工确认单元格（`0/1`） |
| `dryRun` | integer | 是否仅试算（`0/1`） |
| `reason` | string | 生成原因 |

### `RosterWeekGenerateResponse`

| 字段 | 类型 | 说明 |
|------|------|------|
| `weekId` | long | 排班周 id |
| `strategy` | string | 实际执行策略 |
| `generatedCellCount` | integer | 新生成单元格数量 |
| `overwrittenCellCount` | integer | 被覆盖单元格数量 |
| `skippedConfirmedCount` | integer | 因保护人工确认而跳过数量 |
| `dryRun` | integer | 是否仅试算（`0/1`） |
| `message` | string | 执行摘要 |

---

## 日历日 `/api/v1/medir/calendar-days`

### `GET /api/v1/medir/calendar-days`

| 类型 | 参数 |
|------|------|
| 查询 | `from`：string (date)，**必填**，区间起 |
| 查询 | `to`：string (date)，**必填**，区间止 |

**`data`**：`CalendarDayResponse[]`。

### `GET /api/v1/medir/calendar-days/{id}`

**`data`**：`CalendarDayResponse`。

### `POST /api/v1/medir/calendar-days`

**请求体**：`CalendarDayUpsertRequest`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `calDate` | string (date) | 是 | |
| `dayType` | string | 是 | 日类型（业务编码字符串） |
| `holidayName` | string | 否 | 节假日名称 |

**`data`**：`CalendarDayResponse`。

### `PUT /api/v1/medir/calendar-days/{id}`

**请求体**：`CalendarDayUpsertRequest`（同上）。

**`data`**：`CalendarDayResponse`。

### `DELETE /api/v1/medir/calendar-days/{id}`

**`data`**：`null`。

---

### `CalendarDayResponse`

| 字段 | 类型 |
|------|------|
| `id` | long |
| `calDate` | string (date) |
| `dayType` | string |
| `holidayName` | string |
| `createdAt` | string (datetime) |
| `updatedAt` | string (datetime) |

---

## 规则配置业务键（`medir_config` 常用键）

业务规则以键值存储；`team_id=0` 表示全局。常用键见种子 `002_seed_reference_data.sql`、`003_seed_rule_meta.sql`，例如：

- `headcount.weekday_134` / `headcount.weekday_25` / `headcount.weekend_holiday`
- `structure.min_zhong` / `structure.min_lin`
- `duty.chain`（JSON）
- `bone_marrow.weekdays`（JSON）
- `post_rotation.weeks`
- `export.title`、`export.footer.small_night`
- `stats.weekend_full_shift_types`（JSON）

---

## 与需求/表结构对应

- 业务规则：`.specify/requirements-lab-roster-scheduling.md`
- 表结构：`.specify/ddl/001_lab_roster_schema.sql`
