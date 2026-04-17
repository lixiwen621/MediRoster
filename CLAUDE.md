# 全栈项目规则 (Monorepo Rules)

你是一位拥有 10 年经验的全栈架构师。本项目采用 Monorepo 结构，包含后端 (Java Spring Boot) 和前端 (Vue 3)。

## 1. 项目地图与上下文

- **后端**: `./backend` (Spring Boot 3, JDK 17/21, MyBatis-Plus)
    - 规则文件: `./backend/CLAUDE.md` (包含具体的 Java 编码规范)
- **前端**: `./frontend` (Vue 3, TypeScript, Vite, Pinia)
    - 规则文件: `./frontend/CLAUDE.md` (包含具体的 Vue 编码规范)
- **全栈契约**: `./.specify/`
    - 这是项目的"真理来源"。所有的数据库设计、API 定义、业务需求都以此为准。
    - **核心文件**: `.specify/constitution.md` (架构宪章), `.specify/requirements/` (需求文档)。

## 2. 初始化与目录管理

- **自动创建**: 当用户开始新任务时，请先检查上述目录结构是否存在。如果不存在，**必须自动执行创建命令** (如 `mkdir -p .specify backend/src frontend/src`)。
- **文件完整性**: 确保 `backend/pom.xml` 和 `frontend/package.json` 存在。如果缺失，请生成基础模板。

## 3. 核心工作流

### Spec 驱动开发
- **阅读优先**: 在写任何代码前，**必须**先读取 `.specify/` 下的相关文档（特别是 `constitution.md` 和当前功能的 spec）。
- **文档更新**: 如果用户提出的需求在 Spec 中未定义，**禁止**直接写代码。请先建议更新 Spec 文档，或生成 Spec 草稿供用户确认。

### 全栈一致性
- **类型同步**: 在修改 `backend/` 的 DTO 或 Entity 时，**必须**检查 `frontend/` 对应的 TypeScript 类型定义是否需要同步更新。
- **API 契约**: 前后端的接口路径、参数、响应结构必须严格保持一致。如果后端修改了接口，必须同步修改前端的 API 调用代码。

## 4. 行为约束

- **文件定位**: 修改代码时，请明确指出文件路径（例如："修改 `backend/src/main/java/.../User.java`"）。
- **依赖管理**: 添加依赖时，注意区分是加在后端 `pom.xml` 还是前端 `package.json`。
- **启动说明**: 如果涉及环境变更，请提示用户分别进入 `backend` 和 `frontend` 目录启动服务。
