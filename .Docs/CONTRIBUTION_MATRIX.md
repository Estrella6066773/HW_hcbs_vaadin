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
| [TEST_CASES.md](TEST_CASES.md) | 测试用例表（D 汇总维护，各功能主责人填写/维护对应 TC） |

本矩阵采用 **「功能纵向主责 + 技术分层协作」**：开发合并时可按包 Review，答辩与个人评分按功能模块讲解。

**各成员详细说明（单人单文档）：** [roles/README.md](roles/README.md)

| 成员 | 答辩主责功能 | 快速上手文档 |
| --- | --- | --- |
| **A** | Film Listing + 场馆/价格数据 | [MEMBER_A_持久化与数据.md](roles/MEMBER_A_持久化与数据.md) |
| **B** | Booking / 订票 | [MEMBER_B_应用服务.md](roles/MEMBER_B_应用服务.md) |
| **C** | Cancellation + My Bookings | [MEMBER_C_Web界面.md](roles/MEMBER_C_Web界面.md) |
| **D** | Login / Register / Role / Navigation + Admin | [MEMBER_D_测试与交付.md](roles/MEMBER_D_测试与交付.md) |

---

## 1. Group members（小组成员）

| 成员 | 姓名 Name | 学号 Student ID | UWE Email | 答辩主责 |
| --- | --- | --- | --- | --- |
| **A** | 【待填】 | 【待填】 | 【待填】 | 影片列表、场馆与票价数据 |
| **B** | 【待填】 | 【待填】 | 【待填】 | 订票业务与页面 |
| **C** | 【待填】 | 【待填】 | 【待填】 | 取消订票、我的订单 |
| **D** | 【待填】 | 【待填】 | 【待填】 | 登录/注册/角色/导航、数据管理、提交协调 |

> 四人组每人总体贡献目标为 **25%**。若小组为 2–3 人，请删除多余行并重新分配比例，确保合计为 **100%**。

---

## 2. Feature ownership（功能主责 — 答辩与测试归属）

| 成员 | 用户可见功能 | 主要 View | 主要 Service | 相关 model / repository | 负责自动化测试 | 对应用例 |
| --- | --- | --- | --- | --- | --- | --- |
| **A** | 主页列表、筛选、影片详情；四城影院与票价 | `FilmRecommendView`, `FilmDetailView`, `AdditiveShowingFilterPanel`, `FilmPoster` | `FilmListingService`, `HcbsSearchService`, `FilmCatalogService`, `PosterResourceService` | `City`, `Cinema`, `Screen`, `Film`, `Actor`, `Showing`, `PriceRule` 及对应 Repository；**`ShowingRepository.searchShowings`** | `FilmListingServiceTest`, `HcbsSearchServiceTest`, `FilmCatalogServiceTest` | TC_001, TC_002 |
| **B** | 订票、座位图、收据 | `BookingView`, `SeatMapPicker` | `BookingService` + 订票 DTO | `Booking`, `BookingSeat`；占座查询与 A 协作 | `BookingServiceTest` | TC_003–TC_007 |
| **C** | 员工取消、客户我的订单 | `CancellationView`, `MyBookingsView` | `CancellationService` + 取消 DTO | 取消后释放 `BookingSeat`（与 A/B 协作） | `CancellationServiceTest` | TC_008–TC_011 |
| **D** | 登录、注册、侧栏导航、账户中心、管理页 | `LoginView`, `RegisterView`, `MainLayout`, `AccountCenterView`, `AdminDataView`, `SecurityConfig` | `RegistrationService`, `AdminCatalogService`, `CurrentUserService`, `HcbsUserDetailsService`, `AuthUiService` | `User`, `UserRole`, `UserStatus` | `RegistrationServiceTest`, `AdminCatalogServiceTest`；**汇总** `TEST_CASES.md`、`DataLoaderTest`, `UiThemeTest` | 注册/角色/Admin 手工；全表由 D 汇总 |

**测试原则：** 功能主责人必须能讲解**自己模块**的单元测试（正常流程 + 至少一条异常/边界）。D 负责用例表汇总、全量 `mvn test` 与提交包，**不代替** A/B/C 讲解各自功能的测试逻辑。

---

## 3. Technical packages（技术包协作 — 合并 Review 用）

| 成员 | 主责包 / 目录 | 合并前须 Review |
| --- | --- | --- |
| **A** | `model`（列表/场馆/票价相关）、`repository`（列表/场次/价目）、`web` 列表页、`service.listing` / `search` / `catalog` | B（订票占座）、C（取消 UI 无交叉）、D（注册字段） |
| **B** | `service.booking`、`dto`（订票相关）、`web/BookingView` | A（占座查询）、C（取消释放座位）、D（角色代订 UI） |
| **C** | `service.cancellation`、`dto`（取消相关）、`web/CancellationView`, `MyBookingsView` | B（订票数据）、D（客户/员工权限） |
| **D** | `security/*`、`service.auth`、`service.admin`、`web` 登录/布局/管理、`config/SecurityConfig` | A（User 实体）、B/C（侧栏入口） |

**依赖规则（全组遵守）：**

1. `web` 只依赖 `service.*` 与 `dto`（`SeatArea` 枚举除外）。  
2. `service` 依赖 `repository` 与 `model`，向界面只返回 `dto`。  
3. `repository` 只依赖 `model`。  
4. 跨功能修改须走 Pull Request；功能主责人 Review 自己模块相关改动。

**共享维护（组内协商，默认）：** `HcbsTestDataSeeder`、`DataLoader`、`frontend/themes/hcbs/styles.css`（样式改动知会各 View 主责人）。

---

## 4. Overall contribution（总体贡献比例）

Coursework 占模块成绩 **80%**；演示 Presentation 占 **20%**（下表最后一列）。  
Exercise 1 / 2 / 3 列为该练习**组内**相对投入，用于说明分工，不等于模块总分权重。

| 成员 | Exercise 1<br>ERD & 数据设计 | Exercise 2<br>实现 | Exercise 3<br>测试 | Presentation<br>演示 | **组内总体** |
| --- | ---: | ---: | ---: | ---: | ---: |
| **A** | **50%** | 25% | 20% | 25% | **25%** |
| **B** | 15% | **35%** | 25% | 25% | **25%** |
| **C** | 15% | 25% | **25%** | 25% | **25%** |
| **D** | 20% | 15% | 20% | 25% | **25%** |
| **合计** | 100% | 100% | 100% | 100% | **100%** |

**分配说明：**

- **A** 主导 Exercise 1（列表/场馆/票价 ERD 与 JPA），并实现列表全栈。  
- **B** 订票实现与业务规则占比最高（Exercise 2）。  
- **C** 取消模块实现 + 本模块测试（Exercise 3 与 C 并列）。  
- **D** 安全/管理实现、User 相关 ERD 协作、用例表与提交协调；答辩主责登录与 Admin，**不以「只讲测试」为主**。  
- **Presentation** 四人各 **25%**：按 §5 功能顺序分项答辩。

---

## 5. Detailed deliverables（分项交付明细）

### 成员 A — Film Listing + 场馆/价格

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| A1 | ERD（列表/场馆/票价部分） | City–Cinema–Screen–Showing–Film–PriceRule | ☑ |
| A2 | 场次搜索 | `ShowingRepository.searchShowings` | ☑ |
| A3 | 列表 Service | `FilmListingService`, `HcbsSearchService`, `FilmCatalogService` | ☑ |
| A4 | 列表 UI | `FilmRecommendView`, `FilmDetailView`, 筛选与海报组件 | ☑ |
| A5 | 模块测试 | `FilmListingServiceTest`, `HcbsSearchServiceTest`, `FilmCatalogServiceTest` | ☑ |
| A6 | 协作 | 与 B 确认占座统计用于余座列；与 D 确认 User 实体边界 | ☑ |

### 成员 B — Booking / 订票

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| B1 | 订票 Service | `BookingService`（7 日、计价、参考号、防重复占座） | ☑ |
| B2 | 订票 DTO | `ShowingOption`, `SeatMapSeat`, `BookingReceipt` 等 | ☑ |
| B3 | 订票 UI | `BookingView`, `SeatMapPicker` | ☑ |
| B4 | 模块测试 | `BookingServiceTest` | ☑ |
| B5 | 协作 | 与 A 对齐占座查询；与 C 对齐取消后座位释放 | ☑ |

### 成员 C — Cancellation + My Bookings

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| C1 | 取消 Service | `CancellationService`（提前一天、50%、释放座位） | ☑ |
| C2 | 取消 DTO | `BookingSummary`, `CustomerBookingRow` 等 | ☑ |
| C3 | 取消 UI | `CancellationView`, `MyBookingsView` | ☑ |
| C4 | 模块测试 | `CancellationServiceTest` | ☑ |
| C5 | 协作 | 与 B 对齐订单/占座状态；种子参考号 `HCBS-SEED001` 演示 | ☑ |

### 成员 D — Login / Register / Role / Nav + Admin + 交付

| 序号 | 工作项 | 交付物 | 状态 |
| --- | --- | --- | --- |
| D1 | 认证与角色 | `SecurityConfig`, `UserRole`, `LoginView`, `RegistrationService` | ☑ |
| D2 | 导航 | `MainLayout` 按角色显示菜单；`AuthUiService` | ☑ |
| D3 | 管理页 | `AdminDataView`, `AdminCatalogService` | ☑ |
| D4 | 模块测试 | `RegistrationServiceTest`, `AdminCatalogServiceTest` | ☑ |
| D5 | 用例与全量测试 | 汇总 `TEST_CASES.md`；维护 `DataLoaderTest`, `UiThemeTest` | ☑ |
| D6 | 提交与答辩协调 | `Group_No.zip`、彩排计时、本矩阵签字 | ☑ |

---

## 6. Individual presentation（分项答辩）

模块 Presentation 占成绩 **20%**。本组约定：**每位成员按功能模块单独讲解并演示**，导师按个人表现评分；**不设全组统一主讲人**。

### 6.1 全组答辩底线（每人必会，1 页小抄）

1. **Vaadin + Spring Boot 各一句**：Vaadin 在服务端构建 UI 组件；Spring Boot 提供依赖注入、安全与 JPA。  
2. **一条完整调用链**（用自己负责的功能）：`View → Service → Repository → Entity`。  
3. **JPA 三注解**：`@Entity`、`@Table`、`@Column` 的作用。  
4. **Repository 一句**：Spring Data 接口，封装数据库访问。  
5. **一个 Vaadin 组件例子**：如自己页面中的 `Grid`、`ComboBox` 或 `Button`。

深讲只需**自己的功能模块**；被问到其他模块时可简要说明后交由对应成员补充。

### 6.2 演示顺序（用户故事，建议）

| 顺序 | 成员 | 演示内容 | 时长 |
| ---: | --- | --- | --- |
| 1 | **D** | `/login` 三类演示账号 → 侧栏随角色变化 → 可选 `/register` | ~1 min |
| 2 | **A** | `/` 筛选 Search → 场次表 → 可选 `/film/{id}` | ~3 min |
| 3 | **B** | `/booking` 选场次与座位 → Confirm → 收据参考号 | ~3 min |
| 4 | **C** | `/cancellation` 查 `HCBS-SEED001` 取消；或客户 `/my-bookings` | ~3 min |
| 5 | **D** | `/admin`（admin 账号）排期或账户维护 | ~1 min |

全组到场；浏览器可由 **D** 操作（导航熟悉），或各人在自己环节操作。

### 6.3 个人答辩要点

| 成员 | 必须讲清 | 必须演示 | 必须能讲的测试 |
| --- | --- | --- | --- |
| **A** | ERD 片段；`searchShowings`；`PriceRule` 存储；余座如何算 | 主页筛选与表格 | `FilmListingServiceTest` 对应 TC_001/002 |
| **B** | 7 日窗口；防重复占座；计价与参考号；员工代订 | 订票并出收据 | `BookingServiceTest` 对应 TC_003–007 |
| **C** | 提前 1 天取消；50% 手续费；当日拒绝；客户只看自己的单 | 取消或 My bookings | `CancellationServiceTest` 对应 TC_008–010 |
| **D** | 三类角色；`@RolesAllowed`；侧栏逻辑；注册默认 CUSTOMER | 登录 + Admin | `RegistrationServiceTest`；用例表如何汇总 |

### 6.4 个人答辩材料

| 成员 | 携带材料 |
| --- | --- |
| A | 幻灯片或提纲 + ERD 图 + 测试方法名列表 |
| B | 幻灯片或提纲 + 业务规则对照表 + `BookingServiceTest` 截图 |
| C | 幻灯片或提纲 + 取消流程图 + `CancellationServiceTest` 截图 |
| D | 幻灯片或提纲 + 角色权限表 + `TEST_CASES.md` 结构说明 |

**D** 负责协调顺序与计时，**不代替** A/B/C 讲解其功能实现与测试细节。

### 6.5 与功能说明文档的关系

- 演示步骤对照 [README_CN.md](README_CN.md)。  
- 答辩内容以本节与 §2、§5 为准，**不必在 README 中重复**。

### 6.6 建议提交包中的文档

```text
Group_No.zip
├── src/
├── pom.xml
├── README.md
├── .Docs/
│   ├── README_CN.md
│   ├── CONTRIBUTION_MATRIX.md
│   ├── ARCHITECTURE.md
│   ├── TEST_CASES.md
│   ├── TEST_DATABASE.md
│   └── req/                  # （可选）ERD.pdf 等
```

---

## 7. Sprint / collaboration log（协作记录）

| 活动 | 频率 | 参与人 | 说明 |
| --- | --- | --- | --- |
| 站会 / 进度同步 | 每周 1 次 | A–D | 按功能模块同步进度 |
| 代码评审 | 每 Sprint 结束 | 功能主责 Review | A↔B（占座/余座）、C↔B（订单状态）、D↔全组（权限） |
| 结对联调 | 按需 | A+B+C+D | 列表 → 订 → 取消完整链路 |
| 答辩彩排 | 答辩前 1 次 | A–D | 按 §6.2 顺序每人试讲 3–5 分钟 |
| 版本管理 | 持续 | A–D | 功能分支合并前至少 1 人 Review |

---

## 8. Out of scope（已知未实现功能）

按课程与案例范围，**全组一致**不实现以下内容（非个人缺席）：

- 支付接口  
- 邮件验证、密码找回  
- 案例中的独立 Manager 界面（新增影院等由 `/admin` 承担，未单独拆分）  
- 管理报表（案例 Admin 报表）  
- 跨影院代订（仅管理员）的权限区分  

**已实现：** 登录与三类角色（客户 / 订票员 / 管理员）、`/admin` 数据管理页。

---

## 9. Fairness statement（公平性声明）

本矩阵由全组根据实际分工与 Git 提交记录共同确认。各人最终成绩将按导师政策并结合本表、**个人答辩表现**与**个人问答**评定（非仅按小组统一演示打分）。若某成员组内占比低于 **15%**，须在答辩前准备补充说明。

---

## 10. Signatures（成员确认）

本人确认上表真实反映本人在 HCBS 项目中的贡献。

| 成员 | 签名 Signature | 日期 Date |
| --- | --- | --- |
| A | | |
| B | | |
| C | | |
| D | | |
