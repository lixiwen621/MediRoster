# MediRoster 前端（Vue）— 实现说明

**位置**：仓库根目录，与 `.specify` 并列。  
**更新**：随代码迭代时请同步修订本文档。

## 1. 技术栈

| 项 | 说明 |
|----|------|
| 框架 | Vue 3（`<script setup>` + TypeScript） |
| 构建 | Vite 7 |
| 路由 | Vue Router 5 |
| 状态 | Pinia（已挂载，可按需使用） |
| 样式 | Tailwind CSS v4 + daisyUI 5 |
| HTTP | axios，`src/api/client.ts` 统一解包 `ApiResponse` |

## 2. 本地运行

```bash
npm install
npm run dev
```

- 开发时代理：`vite.config.ts` 将 `/api` 转发至 `http://localhost:8084`（与后端默认端口一致）。
- 生产环境：可通过 `VITE_API_BASE_URL` 指向后端根地址；留空则使用相对路径 `/api`。示例见仓库根目录 `.env.example`。

## 3. 源码结构（摘要）

| 路径 | 作用 |
|------|------|
| `src/api/client.ts` | axios 实例、`unwrap`、错误工具 |
| `src/api/medir/types.ts` | MediRoster DTO 类型（与 OpenAPI 对齐时需改此文件） |
| `src/api/medir/index.ts` | 各 REST 端点封装 |
| `src/router/index.ts` | 路由表 |
| `src/components/AppLayout.vue` | 侧栏/导航布局 |
| `src/views/` | 页面：首页 + `medir/*` |
| `src/utils/weekDates.ts` | ISO 周周一至周日日期串 |
| `src/utils/rosterStats.ts` | 需求 §15「周末全天」「上周末」前端统计与提示 |

## 4. 与 REST API 的对应关系

详见 **`.specify/docs/rest-api-frontend-alignment.md`**（含 `teamCode`/`teamName`、`typeCode`/`nameZh`、`workDate`、`yearLabel` 等与后端一致的命名）。

前端请求前缀为 **`/api/v1`**。字段名以 OpenAPI 为准；整型开关在后端一般为 **0/1**。

## 5. 业务需求文档映射（摘录）

- **班次枚举、岗位、6 人组、规则键**：主数据与 `medir_config` 由后端/管理端维护；前端排班周矩阵按 `shiftTypeId` 选择班次。
- **§15 周末统计**：`rosterStats.ts` 按周六（列索引 5）、周日（列索引 6）与 `shift_types.code`（如 `LIN`、`GUISUI_QUAN`、`ZHONG`、`XIAOYE`）计算展示列；与 Excel 导出一致性以后端/统一服务为准。
- **未在前端闭环**：整表规则校验、Excel 导出、认证 — 见需求文档 §19.3 与接口说明中的后续迭代说明。

## 6. 文档同步约定（必做）

**代码变更**或**需求变更**合并前，须同步更新下列文档，避免实现与说明脱节：

| 变更类型 | 更新文档 |
|----------|----------|
| 产品/流程/页面范围、业务规则、与需求章节对应关系 | **本文档**（`mediroster-frontend.md`），必要时补充或引用仓库内完整需求稿 |
| 请求路径、查询参数、DTO 字段、错误码、`unwrap` 约定、联调注意 | **`rest-api-frontend-alignment.md`**；若与 OpenAPI 不一致，注明以 `GET /api/v1/openapi` 为准 |
| 用户可见的运行方式、环境变量、脚本 | 根目录 **`README.md`**（仅当上述有变时） |

同一 PR/提交中尽量**代码与文档同批修改**；若仅文档修正，在变更记录表中简要说明。

## 7. 构建与检查

```bash
npm run build   # vue-tsc -b && vite build
```

## 8. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-04-11 | 初版：脚手架、API 封装、各管理页与排班周矩阵 |
| 2026-04-17 | 增加「文档同步约定」：代码/需求变更须同步更新需求说明与接口对齐文档 |
