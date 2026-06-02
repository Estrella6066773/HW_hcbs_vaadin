# HCBS Contribution Matrix（贡献矩阵）

**Module:** IN3338 Object-Oriented Development  
**Project:** Horizon Cinemas Booking System (HCBS)  
**Group ID:** Group ___（请填写 Blackboard 组号）  
**Academic year:** 2025–2026  
**Submission date:** ___ / ___ / 2026  

## 文档分工说明

| 文档 | 内容 |
| --- | --- |
| [README.md](../README.md) / [README_CN.md](README_CN.md) | 系统功能、运行、测试、业务规则 |
| [四人分工.md](四人分工.md) | **主分工**：模块划分 + 源码路径（可点击） |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 技术分层与依赖规则 |
| **本文件** | 姓名、贡献比例、交付清单、答辩顺序、签字 |
| [TEST_CASES.md](TEST_CASES.md) | 手工测试用例表 |

**分工原则：** 每人 **垂直负责一个完整模块**（Service + View + Model/Repository/DTO + 测试 + 相关配置），**不按前端/业务/测试横向切分**。

**各成员详细说明：** [roles/README.md](roles/README.md)

| 成员 | 模块 | 快速上手 |
| --- | --- | --- |
| **A** | 影片浏览（Home） | [MEMBER_A_影片浏览.md](roles/MEMBER_A_影片浏览.md) |
| **B** | 订票（Booking） | [MEMBER_B_订票.md](roles/MEMBER_B_订票.md) |
| **C** | 账户 · 取消 · 导航 | [MEMBER_C_账户与导航.md](roles/MEMBER_C_账户与导航.md) |
| **D** | 数据管理 · 平台基础 | [MEMBER_D_管理与平台.md](roles/MEMBER_D_管理与平台.md) |

---

## 1. Group members（小组成员）

| 成员 | 姓名 Name | 学号 Student ID | UWE Email | 负责模块 |
| --- | --- | --- | --- | --- |
| **A** | 【待填】 | 【待填】 | 【待填】 | 影片浏览 |
| **B** | 【待填】 | 【待填】 | 【待填】 | 订票 |
| **C** | 【待填】 | 【待填】 | 【待填】 | 账户 · 取消 · 导航 |
| **D** | 【待填】 | 【待填】 | 【待填】 | 数据管理 · 平台基础 |

> 四人组每人总体贡献目标 **25%**。合计须为 **100%**。

---

## 2. Module ownership（模块主责）

| 成员 | 用户可见功能 | 主要 View | 主要 Service | 自动化测试 |
| --- | --- | --- | --- | --- |
| **A** | 主页、筛选、影片详情 | `FilmRecommendView`, `FilmDetailView`, 筛选/海报组件 | `HcbsSearchService`, `FilmListingService`, `FilmCatalogService`, `PosterResourceService` | `HcbsSearchServiceTest`, `FilmListingServiceTest`, `FilmCatalogServiceTest` |
| **B** | 订票、座位图、收据 | `BookingView`, `SeatMapPicker` | `BookingService` | `BookingServiceTest`（6 用例） |
| **C** | 登录/注册/账户、员工取消、客户我的订单、全局侧栏 | `LoginView`, `RegisterView`, `CancellationView`, `MyBookingsView`, `MainLayout`, `AppShell` | `RegistrationService`, `CancellationService` + `security/*` | `RegistrationServiceTest`, `CancellationServiceTest` |
| **D** | 数据管理、种子数据、端口/启动 | `AdminDataView` | `AdminCatalogService` | `AdminCatalogServiceTest`, `DataLoaderTest`, `HcbsPortAllocatorTest`, `UiThemeTest` |

**手工用例：** 见 [TEST_CASES.md](TEST_CASES.md)。**D** 汇总 Actual Result 列；各模块主责人填写并讲解自己负责的 TC。

**跨模块只读依赖：**

- A → B：详情页 Book 跳转 `/booking?showingId=`
- B → C：订单写入后由 C 取消
- C → 全员：`MainLayout` 菜单可见性
- D → 全员：种子数据

---

## 3. Technical scope（各模块代码范围）

完整文件列表见 [四人分工.md](四人分工.md)。摘要：

| 成员 | 主责目录 / 文件 |
| --- | --- |
| **A** | `service/search|listing|catalog`, `web/home`, `FilmPosterCatalog`, `HcbsMediaCatalog`, 海报/横幅资源；列表相关 `model`/`repository`/`dto` |
| **B** | `service/booking`, `web/booking`, 订票相关 `model`/`repository`/`dto`, `SeatGridFormat` |
| **C** | `service/auth`, `service/cancellation`, `web/auth`, `web/cancellation`, `web/shell`, `web/component/PageHero`, `security/*`, `SecurityConfig`, `DemoAccountCatalog`, `PhoneNumbers`, User 相关 `model`/`repository`/`dto` |
| **D** | `service/admin`, `web/admin`, `AdminCatalogService` DTO, `DataLoader`, `HcbsTestDataSeeder`, 端口与启动配置, `HOW_TO_RUN.txt`, 本矩阵汇总 |

**依赖规则（全组遵守）：**

1. `web` 只依赖 `service.*`、`security.*`（只读）与 `dto`。  
2. `service` 依赖 `repository` 与 `model`，向界面返回 `dto`。  
3. 修改他人模块须 PR + 模块主责人 Review。

---

## 4. Overall contribution（总体贡献比例）

| 成员 | Exercise 1<br>ERD | Exercise 2<br>实现 | Exercise 3<br>测试 | Presentation | **组内总体** |
| --- | ---: | ---: | ---: | ---: | ---: |
| **A** | **50%** | 25% | 25% | 25% | **25%** |
| **B** | 10% | **35%** | 25% | 25% | **25%** |
| **C** | 10% | 30% | 25% | 25% | **25%** |
| **D** | 30% | 10% | 25% | 25% | **25%** |
| **合计** | 100% | 100% | 100% | 100% | **100%** |

- **A** 牵头 Exercise 1（ERD）。  
- **B** 订票实现占比最高。  
- **C** 账户+取消+导航全栈。  
- **D** 平台/Admin/种子/提交协调；ERD 中 User 与全局数据协作。

---

## 5. Deliverables checklist（交付清单）

### A — 影片浏览

| # | 交付物 | 状态 |
| --- | --- | --- |
| A1 | ERD（影片/场馆/场次/票价） | ☑ |
| A2 | 搜索与列表 Service + Home View | ☑ |
| A3 | 海报/媒体配置与静态资源 | ☑ |
| A4 | 模块单元测试 ×3 | ☑ |

### B — 订票

| # | 交付物 | 状态 |
| --- | --- | --- |
| B1 | `BookingService` + 订票 View/座位图 | ☑ |
| B2 | `BookingServiceTest`（6 用例） | ☑ |

### C — 账户 · 取消 · 导航

| # | 交付物 | 状态 |
| --- | --- | --- |
| C1 | 登录/注册/Security | ☑ |
| C2 | 取消 Service + Cancellation/MyBookings View | ☑ |
| C3 | `MainLayout` / `AppShell` | ☑ |
| C4 | 模块单元测试 ×2 | ☑ |

### D — 数据管理 · 平台

| # | 交付物 | 状态 |
| --- | --- | --- |
| D1 | `AdminDataView` + `AdminCatalogService` | ☑ |
| D2 | 种子数据 + 端口/启动配置 | ☑ |
| D3 | 平台相关测试 ×4 | ☑ |
| D4 | `Group_No.zip`、`HOW_TO_RUN.txt`、本矩阵 | ☐ |

---

## 6. Presentation（分项答辩，≤20 分钟）

### 6.1 演示顺序（与 [四人分工.md](四人分工.md) 一致）

| 顺序 | 成员 | 内容 | 时长 |
| ---: | --- | --- | --- |
| 1 | **D** | 架构一句 + 启动/种子 | ~2 min |
| 2 | **A** | Home 浏览 → 详情 | ~4 min |
| 3 | **B** | 订票 → 收据 | ~4 min |
| 4 | **C** | 登录/角色 → 取消 → MainLayout | ~5 min |
| 5 | **D** | Admin 排片 | ~3 min |
| 6 | 全员 | 测试 + ERD + Q&A | ~2 min |

### 6.2 每人必会（1 页小抄）

1. Vaadin + Spring Boot 各一句。  
2. **自己模块**的 `View → Service → Repository → Entity` 一条链。  
3. 一个 Vaadin 组件例子（自己页面上的）。  
4. **自己模块**的单元测试方法名 ×1–2。

### 6.3 个人答辩要点

| 成员 | 必须讲清 | 必须演示 | 必须能讲的测试 |
| --- | --- | --- | --- |
| **A** | ERD 片段；筛选与 `ShowingRow` | `/` 筛选 → 详情 | `FilmListingServiceTest` 等 |
| **B** | 7 日窗口；防重复占座；计价 | 订票出收据 | `BookingServiceTest` |
| **C** | 三类角色；`MainLayout`；取消 50% | 登录 + 取消或 My bookings | `RegistrationServiceTest`, `CancellationServiceTest` |
| **D** | 种子数据；Admin 排片 | `/admin` | `AdminCatalogServiceTest`, `DataLoaderTest` |

---

## 7. Out of scope（已知未实现）

- 支付、邮件验证、密码找回  
- 独立 Manager 视图（新增影院）  
- Admin 报表（订票数、月收入等）

---

## 8. Signatures（成员确认）

| 成员 | 签名 | 日期 |
| --- | --- | --- |
| A | | |
| B | | |
| C | | |
| D | | |
