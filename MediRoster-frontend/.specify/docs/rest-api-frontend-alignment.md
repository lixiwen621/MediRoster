# MediRoster REST API — 前端对齐说明

本文档与 **MediRoster REST API（v1）** 主数据规范一致；若与运行中的 OpenAPI 有出入，以 `GET /api/v1/openapi` 为准。

**维护约定**：凡涉及路径、参数、DTO、`unwrap`、联调差异的代码或需求变更，须同步修订本文档；产品/页面级需求说明见 **`mediroster-frontend.md`** 第六节「文档同步约定」。

## 1. 统一响应

- 成功：`success=true`，`data` 一般为非 `null`（删除等无体成功时 `data` 可为 `null`）。
- 前端：`unwrap()` 要求 `data` 非空；删除/无返回体接口使用 `unwrapOk()`。
- 失败：`success=false`；HTTP `400`、`404`、`409`（含 `OPTIMISTIC_LOCK` / `DUPLICATE_KEY` 等）。

## 2. 路径与查询参数（camelCase）

| 模块 | 要点 |
|------|------|
| 班组 | `TeamResponse`：`teamCode`、`teamName`、`description`、`enabled`（0/1） |
| 岗位 | **全局字典**，`GET /posts` 无 `teamId`；`postCode`、`postName`、`description`、`sortOrder`、`enabled` |
| 班次 | `ShiftTypeResponse`：`typeCode`、`nameZh`；标志位均为 **整数 0/1** |
| 人员 | `GET /staff` **必填** `teamId`；可选 `includeDeleted`；`StaffUpsertRequest` 含 **`memberType`**、`sortOrder` 等 |
| 能力 | `POST` 体：`capabilityCode`、`enabled`（0/1） |
| 规则配置 | `ConfigUpsertRequest`：`configKey`、`configValue`、**`valueType`**、**`sortOrder`**、**`enabled`**、`category`、`description` |
| 规则元数据 | `RuleMetaResponse`：`labelZh`、`helpText`、`category`、`optionsJson` 等 |
| 排班周 | 列表：`teamId`、`year`；`RosterWeekResponse`：`weekStartDate`（周一）、**`yearLabel`**（无 `weekNumber`） |
| 创建周 | `RosterWeekCreateRequest`：`teamId`、`weekStartDate`、`yearLabel`、`status`、`remark` |
| 更新周 | `RosterWeekUpdateRequest`：`yearLabel`、`status`、`remark`、`version` |
| 单元格 | `PUT .../cells` 请求体仅 **`cells`**（无 `version`）；元素 **`workDate`**、`shiftTypeId`、**`validationExempt`**（0/1）等。**成功时 `data` 为 `RosterWeekWeekendStatResponse[]`**（与 `GET .../weekend-stats` 同结构，含 `weekendFullAuto`/`weekendFullFinal`、`lastWeekendAuto`/`lastWeekendFinal`、`isOverridden` 等）；前端用 `unwrap` 取数组，保存单元格/清空后可直接刷新周末列，无需再 GET 统计（与仅改单元格无关的流程仍可按需 GET） |
| 半自动生成 | `POST .../roster-weeks/{weekId}/generate`：`RosterWeekGenerateRequest`（`strategy`：`FILL_UNCONFIRMED` / `OVERWRITE_ALL`，`respectManualConfirmed` 0/1，`dryRun` 0/1，`reason`）；响应 `RosterWeekGenerateResponse`（`generatedCellCount`、`skippedConfirmedCount` 等） |
| Excel 导出 | `GET .../roster-weeks/{weekId}/export`：返回 `.xlsx` 二进制流（非 `ApiResponse`）；前端需 `responseType='blob'`，从 `Content-Disposition` 解析文件名 |
| 周末统计 | `GET/PUT .../weekend-stats`：读取 `weekendFullAuto/final`、`lastWeekendAuto/final`、`isOverridden`、`overrideReason`；保存体 `items[]` 使用 `weekendFullOverride`、`lastWeekendOverride`、`overrideReason`（`null` 表示恢复自动） |
| 周岗位行 | `PUT .../staff-posts` 体：**`items`**，`Item`：`staffId`、`displayPostId`、`displayLabel` |
| 日历日 | `CalendarDayResponse`：`calDate`、`dayType`、`holidayName`；区间查询 **`from`** / **`to`** |

## 3. 前端实现位置

| 文件 | 说明 |
|------|------|
| `src/api/medir/types.ts` | DTO 与请求体类型 |
| `src/api/medir/index.ts` | 方法签名与路径 |
| `src/api/client.ts` | `unwrap` / `unwrapOk` |
| `src/utils/rosterStats.ts` | 统计用 `typeCode`（如 `LIN`、`ZHONG`） |

## 4. 相关引用

- 业务规则：`.specify/requirements-lab-roster-scheduling.md`（若存在于仓库）
- 表结构：`.specify/ddl/001_lab_roster_schema.sql`（若存在）
