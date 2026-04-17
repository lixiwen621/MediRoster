# MediRoster-frontend

Vue 3 + Vite + TypeScript + Tailwind CSS v4 + daisyUI，对接 MediRoster 后端 v1 REST（`/api/v1/medir/*`）。

## 本地开发

1. 启动后端（默认端口 **8084**）。
2. `npm install`
3. `npm run dev`

开发时请求走 Vite 代理：`/api` → `http://localhost:8084`（见 `vite.config.ts`）。若后端端口不同，请改代理 `target`。

生产构建可设置 `VITE_API_BASE_URL` 指向后端根地址；留空则使用相对路径 `/api`。

## 功能页面

- 班组、岗位、班次类型、人员（含能力子表）、规则键值、规则元数据、排班周（矩阵编辑 + §15 周末统计列）、日历日
- 首页：`GET /api/v1/health`

类型与路径以你方 OpenAPI 为准；若字段名与 Spring 实体不一致，请改 `src/api/medir/types.ts` 与各视图表单。

## 详细文档（项目内）

见 **`.specify/docs/`**：

- `mediroster-frontend.md` — 技术栈、目录结构、需求映射、**文档同步约定**（代码/需求变更须同步更新说明与接口文档）
- `rest-api-frontend-alignment.md` — REST 与前端封装对齐、联调待确认项
