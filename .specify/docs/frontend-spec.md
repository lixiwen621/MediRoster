# MediRoster 前端技术规范

**版本**: 1.0 | **创建**: 2026-04-17

---

## 1. 技术栈

- **核心框架**: Vue 3.4+ (Composition API)
- **脚本语言**: TypeScript 5.0+ (严格模式，严禁 `any`)
- **构建工具**: Vite
- **状态管理**: Pinia (Setup Store 模式)
- **路由管理**: Vue Router 4
- **CSS 方案**: Tailwind CSS + daisyUI
- **HTTP 请求**: Axios (拦截器封装)
- **工具库**: VueUse, Lodash-es, Dayjs

---

## 2. 本地开发

1. 启动后端（默认端口 **8084**）
2. `npm install`
3. `npm run dev`

开发时请求走 Vite 代理：`/api` → `http://localhost:8084`（见 `vite.config.ts`）。

生产构建可设置 `VITE_API_BASE_URL` 指向后端根地址。

---

## 3. 目录结构

```
src/
├── api/
│   ├── medir/
│   │   ├── index.ts        # 所有 API 调用函数
│   │   └── types.ts        # TypeScript 类型定义
│   └── client.ts            # Axios 实例 + 拦截器
├── components/
│   └── AppLayout.vue        # 全局布局
├── router/
│   └── index.ts             # 路由配置
├── utils/
│   ├── weekDates.ts         # 周日期计算
│   └── rosterStats.ts       # 排班统计
├── views/
│   ├── HomeView.vue
│   └── medir/
│       ├── TeamsView.vue
│       ├── PostsView.vue
│       ├── ShiftTypesView.vue
│       ├── StaffView.vue
│       ├── ConfigView.vue
│       ├── RuleMetaView.vue
│       ├── CalendarDaysView.vue
│       └── RosterWeeksView.vue
├── App.vue
├── main.ts
└── style.css
```

---

## 4. 功能页面

- 班组管理、岗位管理、班次类型管理
- 人员管理（含能力子表）
- 规则键值配置、规则元数据
- 日历日管理
- 排班周矩阵编辑（含周末统计列）
- 首页：`GET /api/v1/health`

---

## 5. TypeScript 类型定义

所有类型在 `src/api/medir/types.ts` 中，与后端 REST API v1 字段名严格一致（camelCase）。

### 统一响应

```typescript
interface ApiResponse<T> {
  success: boolean; code: string; message: string; data: T;
}
```

### 实体类型

| 前端接口 | 后端对应 | 说明 |
|---------|---------|------|
| `MedirTeam` | TeamResponse | 班组 |
| `MedirPost` | PostResponse | 岗位 |
| `MedirShiftType` | ShiftTypeResponse | 班次类型 |
| `MedirStaff` | StaffResponse | 人员 |
| `MedirStaffCapability` | StaffCapabilityResponse | 人员能力 |
| `MedirConfig` | ConfigResponse | 规则键值 |
| `MedirRuleMeta` | RuleMetaResponse | 规则元数据 |
| `MedirRosterWeek` | RosterWeekResponse | 排班周 |
| `MedirRosterCell` | RosterCellResponse | 排班单元格 |
| `MedirStaffPostRow` | RosterWeekStaffPostResponse | 人员岗位 |
| `MedirRosterWeekendStat` | RosterWeekWeekendStatResponse | 周末统计 |
| `MedirCalendarDay` | CalendarDayResponse | 日历日 |

### 请求类型

| 前端接口 | 后端对应 | 用途 |
|---------|---------|------|
| `TeamUpsertRequest` | TeamUpsertRequest | 班组增删改 |
| `PostUpsertRequest` | PostUpsertRequest | 岗位增删改 |
| `ShiftTypeUpdateRequest` | ShiftTypeUpdateRequest | 班次类型更新 |
| `StaffUpsertRequest` | StaffUpsertRequest | 人员增删改 |
| `RosterWeekCreateRequest` | RosterWeekCreateRequest | 创建排班周 |
| `RosterWeekUpdateRequest` | RosterWeekUpdateRequest | 更新排班周 |
| `RosterCellReplaceRequest` | RosterCellReplaceRequest | 覆盖单元格 |
| `RosterWeekGenerateRequest` | RosterWeekGenerateRequest | 自动生成 |
| `CalendarDayUpsertRequest` | CalendarDayUpsertRequest | 日历日增删改 |

### 枚举

```typescript
type StaffStatus = 1 | 2  // 1: 在职, 2: 停用
type RosterWeekStatus = 1 | 2  // 1: 草稿, 2: 已发布
```

---

## 6. 文档同步约定

代码/需求变更须同步更新：
1. `src/api/medir/types.ts` — TypeScript 类型
2. 相关 Spec 文档

---

## 7. 变更记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-04-17 | 初始版本，合并自 MediRoster-frontend 项目 |
