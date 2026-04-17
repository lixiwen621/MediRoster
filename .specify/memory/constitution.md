# MediRoster 架构宪章 (Architecture Constitution)

**版本**: 1.0 | **创建**: 2026-04-17 | **最后更新**: 2026-04-17

## 核心原则

### 1. Spec 驱动开发 (Spec-Driven Development)
所有需求、接口、架构约束以此目录下的文档为"真理来源"。代码变更必须以 Spec 为依据，需求变更先更新 Spec 文档。

### 2. 前后端契约一致
前端 TypeScript 类型定义必须与后端 DTO/Response 严格对齐。API 路径、参数、响应结构必须保持一致。字段命名统一使用 camelCase。

### 3. 国际化 (i18n)
后端所有错误提示、用户文案禁止硬编码中文字符串，必须通过 i18n Key 获取。资源文件位于 `messages_zh_CN.properties` / `messages_en.properties`。

### 4. 统一响应体
所有 REST 接口返回统一 `ApiResponse<T>` 结构：`{ success: boolean, code: string, message: string, data: T }`。
- 成功时 `success=true, code="0"`
- 失败时按 HTTP 状态码 + 业务码区分（400/404/409）
- 文件导出（Excel）为例外，成功时返回二进制流

### 5. 乐观锁约束
排班周（roster_weeks）的更新必须携带当前 `version` 字段，版本不匹配时返回 409 + `OPTIMISTIC_LOCK`。

## 技术栈约束

### 后端
- **语言**: Java 17/21
- **框架**: Spring Boot 3.x, MyBatis-Plus
- **注入**: 构造器注入（`@RequiredArgsConstructor`），严禁 `@Autowired` 字段注入
- **映射**: MapStruct（严禁 `BeanUtils.copyProperties`）
- **校验**: Google Guava `Preconditions` + i18n
- **数据库**: MySQL 8.0+
- **文档**: OpenAPI 3 (`/api/v1/openapi`)

### 前端
- **框架**: Vue 3.4+ (Composition API, `<script setup lang="ts">`)
- **语言**: TypeScript 5.0+ (严格模式，严禁 `any`)
- **构建**: Vite
- **状态**: Pinia (Setup Store 模式)
- **HTTP**: Axios (拦截器封装)
- **CSS**: Tailwind CSS + daisyUI

## 开发工作流

1. 阅读 `.specify/requirements/` 下的需求文档
2. 阅读 `.specify/docs/rest-api.md` 接口文档
3. 确认需求在 Spec 中已定义
4. 按 CLAUDE.md 编码规范实现
5. 代码/需求变更须同步更新 Spec 文档
