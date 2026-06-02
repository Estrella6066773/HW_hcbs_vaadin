# HCBS 项目文档索引

除仓库根目录 [README.md](../README.md)（英文功能说明）外，**小组文档均在本目录**。

> **分工以 [四人分工.md](四人分工.md) 为准**（按模块垂直负责，每人端到端负责 Service + View + Test + 相关配置）。

---

## 快速入口

| 文档 | 说明 |
| --- | --- |
| **[四人分工.md](四人分工.md)** | **主文档**：A–D 模块划分、可点击源码路径、答辩脚本 |
| [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) | 贡献矩阵（姓名、比例、交付清单、签字） |
| [roles/README.md](roles/README.md) | 各成员模块说明（单人单文档） |

---

## 功能与运行

| 文档 | 说明 |
| --- | --- |
| [README_CN.md](README_CN.md) | 功能说明（中文） |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 技术分层、`web` 分包与依赖规则 |
| [TEST_CASES.md](TEST_CASES.md) | 手工测试用例表（TC_001–TC_011） |
| [TEST_DATABASE.md](TEST_DATABASE.md) | 测试数据库设计与种子数据场景 |
| [DEV_TROUBLESHOOTING.md](DEV_TROUBLESHOOTING.md) | 开发环境：H2 锁库 / 删本地库 |

---

## 源码内文档（答辩用）

| 路径 | 说明 |
| --- | --- |
| [web/README.md](../src/main/java/com/hcbs/web/README.md) | Web 层总览 |
| [web/答辩指南.md](../src/main/java/com/hcbs/web/答辩指南.md) | 答辩 Q&A 速查 |
| [service/booking/B-阅读指引.md](../src/main/java/com/hcbs/service/booking/B-阅读指引.md) | 订票模块阅读顺序（成员 B） |
| `web/{home,booking,cancellation,auth,admin,shell}/README.md` | 各菜单板块说明 |

---

## 课程要求原文

| 路径 | 说明 |
| --- | --- |
| [req/HCBS_Case_Study.txt](req/HCBS_Case_Study.txt) | HCBS 案例说明（文本） |
| [req/Object Oriented Development [Coursework] (firstsit2025-2026).txt](req/Object%20Oriented%20Development%20%5BCoursework%5D%20(firstsit2025-2026).txt) | 作业说明（80%） |
| [req/](req/) | 上述 `.docx` 原件 |

---

## 当前四人模块（摘要）

| 成员 | 模块 | 路由 / 能力 |
|:----:|------|-------------|
| **A** | 影片浏览 | `/` · `/film/:id` |
| **B** | 订票 | `/booking` |
| **C** | 账户 · 取消 · 导航 | `/login` · `/register` · `/cancellation` · `/my-bookings` · 侧栏 |
| **D** | 数据管理 · 平台基础 | `/admin` · 种子数据 · 端口 |

详细文件清单见 [四人分工.md](四人分工.md)。
