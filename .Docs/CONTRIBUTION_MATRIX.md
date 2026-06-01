# HCBS Contribution Matrix（贡献矩阵）

**Module:** IN3338 Object-Oriented Development  
**Project:** Horizon Cinemas Booking System (HCBS)  
**Group ID:** Group ___（请填写 Blackboard 组号）  
**Academic year:** 2025–2026  
**Repository:** `hcbs-vaadin`  
**Submission date:** ___ / ___ / 2026  

## 文档分工说明

| 文档 | 内容 |
| --- | --- |
| [README.md](../README.md) / [README_CN.md](README_CN.md) | **仅**系统功能、运行、测试、业务规则（给用户与评分人看「软件做什么」） |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 技术分层与依赖规则（不含成员姓名） |
| **本文件** | 成员 A–D 分工、贡献比例、交付清单、分项答辩、签字 |
| [TEST_CASES.md](TEST_CASES.md) | 测试用例表 |

本矩阵与代码包结构一致；技术架构见 [ARCHITECTURE.md](ARCHITECTURE.md)。

**各角色详细上手说明（单人单文档）：** [roles/README.md](roles/README.md)

| 成员 | 快速上手文档 |
| --- | --- |
| A | [MEMBER_A_持久化与数据.md](roles/MEMBER_A_持久化与数据.md) |
| B | [MEMBER_B_应用服务.md](roles/MEMBER_B_应用服务.md) |
| C | [MEMBER_C_Web界面.md](roles/MEMBER_C_Web界面.md) |
| D | [MEMBER_D_测试与交付.md](roles/MEMBER_D_测试与交付.md) |

---

## 1. Group members（小组成员）

| 成员 | 姓名 Name | 学号 Student ID | UWE Email | 角色 Role |
| --- | --- | --- | --- | --- |
| **A** | 【待填】 | 【待填】 | 【待填】 | 持久化与数据设计 |
| **B** | 【待填】 | 【待填】 | 【待填】 | 应用服务与业务规则 |
| **C** | 【待填】 | 【待填】 | 【待填】 | Web 界面与前端主题 |
| **D** | 【待填】 | 【待填】 | 【待填】 | 测试、文档与提交交付 |

> 四人组每人总体贡献目标为 **25%**。若小组为 2–3 人，请删除多余行并重新分配比例，确保合计为 **100%**。

---

## 2. Modular ownership（按代码包分工）

| 成员 | 负责目录 / 包 | 核心类与交付物 | 合并前须 Review 的人 |
| --- | --- | --- | --- |
| **A** | `com.hcbs.model/*`<br>`com.hcbs.repository/*` | 实体、枚举、ERD、逻辑模式、`searchShowings`、活跃占座查询 | B（服务接口）、D（测试数据） |
| **B** | `com.hcbs.dto/*`<br>`com.hcbs.service.listing/*`<br>`com.hcbs.service.booking/*`<br>`com.hcbs.service.cancellation/*`<br>`com.hcbs.config/DataLoader.java` | 三项业务服务、DTO 契约、演示种子数据 | A（表结构）、C（界面字段）、D（可测性） |
| **C** | `com.hcbs.web/*`<br>`frontend/themes/hcbs/*` | 三个 View、`MainLayout`、`AppShell`、主题 CSS | B（Service API）、D（UI 测试反馈） |
| **D** | `src/test/**`<br>`TEST_CASES.md`<br>本矩阵、提交 zip、演示材料 | 单元测试、用例表、打包检查、答辩脚本 | A/B/C（缺陷修复） |

**依赖规则（全组遵守）：**

1. `web` 只依赖 `service.*` 与 `dto`（`SeatArea` 枚举除外）。  
2. `service` 依赖 `repository` 与 `model`，向界面只返回 `dto`。  
3. `repository` 只依赖 `model`。  
4. 禁止跨层擅自修改对方包内代码，须通过 Pull Request / 结对评审。

---

## 3. Overall contribution（总体贡献比例）

Coursework 占模块成绩 **80%**；演示 Presentation 占 **20%**（下表最后一列）。  
Exercise 1 / 2 / 3 列为该练习**组内**相对投入，用于说明分工，不等于模块总分权重。

| 成员 | Exercise 1<br>ERD & 数据设计 | Exercise 2<br>实现 | Exercise 3<br>测试 | Presentation<br>演示 | **组内总体** |
| --- | ---: | ---: | ---: | ---: | ---: |
| **A** | **40%** | 18% | 8% | 25% | **25%** |
| **B** | 12% | **42%** | 18% | 25% | **25%** |
| **C** | 8% | 28% | 12% | 25% | **25%** |
| **D** | 8% | 12% | **62%** | 25% | **25%** |
| **合计** | 100% | 100% | 100% | 100% | **100%** |

**分配说明（合理性）：**

- **A** 主导 Exercise 1（ERD、表结构、JPA），实现阶段提供持久层，测试阶段配合数据约束说明。  
- **B** 实现量最大（三个 Service + 全部 DTO + 业务规则），与 A/C 对接接口。  
- **C** 负责全部 Vaadin 界面与主题，答辩时讲解本人负责的页面与交互。  
- **D** 主导测试策略、用例表、自动化测试与提交物核对，Exercise 3 组内占比最高。  
- **Presentation** 四人各占组内 **25%**：采用**分项答辩**（见第 5 节），非一人统一答辩。

---

## 4. Detailed deliverables（分项交付明细）

### 成员 A — 持久化与数据设计

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| A1 | 需求分析与 ERD | 实体、关系、主外键、基数（PDF/图） | ☑ |
| A2 | 逻辑数据库模式 | 表结构、类型、约束说明 | ☑ |
| A3 | 设计说明 | 1–2 段（规范化、票价规则存储等） | ☑ |
| A4 | JPA 实体 | `City`, `Cinema`, `Screen`, `Seat`, `Film`, `Actor`, `Showing`, `Booking`, `PriceRule`, … | ☑ |
| A5 | Repository | `ShowingRepository.searchShowings`、`BookingSeatRepository` 活跃预订查询 | ☑ |
| A6 | 协作 | 评审 B 的 DTO 字段；确认取消后占座释放策略 | ☑ |

---

### 成员 B — 应用服务与 DTO

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| B1 | 影片列表服务 | `FilmListingService` → `ShowingRow`, `CityOption`, `CinemaOption` | ☑ |
| B2 | 订票服务 | `BookingService`：7 日限制、计价、唯一参考号、防重复占座 | ☑ |
| B3 | 取消服务 | `CancellationService`：提前一天、50% 费用、释放座位 | ☑ |
| B4 | DTO 层 | `dto/*` 全部记录类 | ☑ |
| B5 | 演示数据 | `DataLoader` 四城影院与票价种子 | ☑ |
| B6 | 协作 | 为 C 提供稳定 Service API；配合 D 补充边界测试 | ☑ |

---

### 成员 C — Web 界面与主题

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| C1 | 影片列表页 | `FilmListingView`（含 description 列、筛选） | ☑ |
| C2 | 订票页 | `BookingView`（仅依赖 `BookingService`） | ☑ |
| C3 | 取消页 | `CancellationView`（仅依赖 `CancellationService`） | ☑ |
| C4 | 布局与导航 | `MainLayout`, `AppShell` | ☑ |
| C5 | 主题样式 | `frontend/themes/hcbs/styles.css` | ☑ |
| C6 | 功能说明文档 | 与 `README` / `README_CN` 中的界面流程描述一致（不负责撰写贡献矩阵） | ☑ |
| C7 | 协作 | 联调订票/取消流程；按 D 反馈改提示文案 | ☑ |

---

### 成员 D — 测试与交付

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| D1 | 测试策略 | 单元测试 + 关键路径手工测试 | ☑ |
| D2 | 自动化测试 | `BookingServiceTest`, `CancellationServiceTest`, `FilmListingServiceTest`, `DataLoaderTest`, `UiThemeTest` | ☑ |
| D3 | 测试用例表 | `TEST_CASES.md`（TC_001–TC_011） | ☑ |
| D4 | 缺陷跟踪 | 重复订座、超期预订、当日取消等问题的验证与关闭 | ☑ |
| D5 | 提交打包 | `Group_No.zip` 结构核对（源码、README、测试证据） | ☑ |
| D6 | 答辩与提交协调 | 组织分项答辩顺序；核对每人幻灯片/提纲；`Group_No.zip` 清单 | ☑ |
| D7 | 本贡献矩阵 | 起草并组织 A–D 确认签字 | ☑ |

---

## 5. Individual presentation（分项答辩）

模块 Presentation 占成绩 **20%**。本组约定：**每位成员在答辩时单独说明自己负责的部分**，导师按个人表现评分；**不设全组统一主讲人**。

### 5.1 到场与顺序（示例）

1. 全组到场，应用可由任一人启动（建议 **C** 操作浏览器）。  
2. 按 **A → B → C → D**（或导师指定顺序）依次发言，每人约 **3–5 分钟**。  
3. 问答阶段：每人回答与自己模块相关的问题。

### 5.2 个人答辩要点（每人自备幻灯片或提纲）

| 成员 | 必须讲清的内容 | 建议演示 |
| --- | --- | --- |
| **A** | 为何这样建 ERD；主要实体与关系；`searchShowings`、活跃占座查询的设计 | ERD 图、`model` / `repository` 关键类 |
| **B** | 三个 Service 的分工；7 日订票、计价、取消 50%、释放座位等规则落在哪段代码；DTO 如何避免界面依赖实体 | `BookingService`、`CancellationService`、任一 DTO |
| **C** | 三个 View 的用户流程；为何只注入 Service；主题与导航结构 | 现场打开列表 / 订票 / 取消页 |
| **D** | 测试策略；TC_001–TC_011 与自动化测试的对应；故意非法输入测过什么；已知局限 | `TEST_CASES.md`、`mvn test` 结果摘要 |

### 5.3 个人答辩材料（每人提交/携带）

| 成员 | 个人材料 |
| --- | --- |
| A | 答辩幻灯片或一页提纲 + ERD PDF |
| B | 答辩幻灯片或一页提纲（含业务规则对照表） |
| C | 答辩幻灯片或一页提纲 + 界面截图（可选） |
| D | 答辩幻灯片或一页提纲 + 测试覆盖说明 |

**D** 负责协调答辩顺序与计时，**不代替** A/B/C 讲解其模块。

### 5.4 与功能说明文档的关系

- 答辩时演示操作可对照 [README_CN.md](README_CN.md) 中的使用流程。  
- 个人答辩内容（ERD、Service、界面、测试）以本节与第 4 节为准，**不必在 README 中重复**。  

### 5.5 建议提交包中的文档

```text
Group_No.zip
├── src/
├── pom.xml
├── README.md                 # 功能说明（英文，仓库根目录）
├── .Docs/
│   ├── README_CN.md          # 功能说明（中文）
│   ├── CONTRIBUTION_MATRIX.md
│   ├── ARCHITECTURE.md
│   ├── TEST_CASES.md
│   ├── TEST_DATABASE.md
│   └── req/                  # （可选）ERD.pdf 等
```

---

## 6. Sprint / collaboration log（协作记录）

| 活动 | 频率 | 参与人 | 说明 |
| --- | --- | --- | --- |
| 站会 / 进度同步 | 每周 1 次 | A–D | 同步 ERD、Service API、界面与测试进度 |
| 代码评审 | 每 Sprint 结束 | 交叉 | A↔B（模型/服务）、C↔D（界面/测试） |
| 结对联调 | 按需 | B + C | 订票收据、取消规则、错误提示 |
| 答辩彩排 | 答辩前 1 次 | A–D | 每人试讲自己的 3–5 分钟，不统一彩排整稿 |
| 版本管理 | 持续 | A–D | 功能分支合并前至少 1 人 Review |

---

## 7. Out of scope（已知未实现功能）

按课程与案例范围，**全组一致**不实现以下内容（非个人缺席）：

- 支付接口  
- 邮件验证、密码找回  
- 案例中的独立 Manager 界面（新增影院等由 `/admin` 承担，未单独拆分）  
- 管理报表（案例 Admin 报表）  
- 跨影院代订（仅管理员）的权限区分  

**已实现（与早期矩阵表述不同）：** 登录与三类角色（客户 / 订票员 / 管理员）、`/admin` 数据管理页。

---

## 8. Fairness statement（公平性声明）

本矩阵由全组根据实际分工与 Git 提交记录共同确认。各人最终成绩将按导师政策并结合本表、**个人答辩表现**与**个人问答**评定（非仅按小组统一演示打分）。若某成员组内占比低于 **15%**，须在答辩前准备补充说明。

---

## 9. Signatures（成员确认）

本人确认上表真实反映本人在 HCBS 项目中的贡献。

| 成员 | 签名 Signature | 日期 Date |
| --- | --- | --- |
| A | | |
| B | | |
| C | | |
| D | | |
