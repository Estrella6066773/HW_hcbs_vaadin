# 成员 D — Login / Register / Role / Navigation + Admin + 交付协调

**答辩主责：** 登录、注册、三类角色、侧栏导航、数据管理；用例表汇总与提交  
**组内目标：** 约 25%  
**合并前须请谁 Review：** A（`User` 实体）、B/C（侧栏入口与 `@RolesAllowed`）

---

## 1. 你在项目中的位置

```text
登录/注册 → LoginView / RegisterView (你)
角色导航 → MainLayout.refreshDrawer (你)
管理后台 → AdminDataView (你)
安全     → SecurityConfig, UserRole, HcbsUserDetailsService (你)
Service  → RegistrationService, AdminCatalogService (你)

全组     → TEST_CASES.md 汇总, Group_No.zip, 答辩计时 (你协调)
```

答辩时**先讲登录与角色**（开场），**收尾演示 Admin**；中间 A/B/C 讲业务功能。你的答辩**不以「只讲 mvn test」为主**，但要说明用例表如何覆盖全系统。

---

## 2. 核心交付清单

| 编号 | 工作项 | 主要文件 |
| --- | --- | --- |
| D1 | 认证 | `SecurityConfig`, `LoginView`, `HcbsUserDetailsService` |
| D2 | 注册 | `RegisterView`, `RegistrationService` |
| D3 | 角色枚举 | `UserRole`：`CUSTOMER` / `BOOKING_STAFF` / `ADMIN` |
| D4 | 导航 | `MainLayout`, `AuthUiService`, `AccountCenterView` |
| D5 | 管理页 | `AdminDataView`, `AdminCatalogService` |
| D6 | **模块测试** | `RegistrationServiceTest`, `AdminCatalogServiceTest` |
| D7 | **全组测试汇总** | `TEST_CASES.md`；维护 `DataLoaderTest`, `UiThemeTest` |
| D8 | 提交与彩排 | `Group_No.zip`、答辩顺序与计时 |

---

## 3. 三类角色（答辩必讲）

| 角色 | 枚举 | 典型账号 | 侧栏能力 |
| --- | --- | --- | --- |
| 客户 | `CUSTOMER` | alice / bob / carol | 我的订单；无柜台取消 |
| 订票员 | `BOOKING_STAFF` | staff / desk01 | 订票、取消柜台 |
| 管理员 | `ADMIN` | admin / admin01 | 上述 + **数据管理** `/admin` |

- 登录：**手机号** + 密码 `demo`（`LoginView` 列出演示号）。  
- 注册：默认 `UserRole.CUSTOMER`。  
- 路由保护：`@RolesAllowed`（如 `AdminDataView` 仅 `ADMIN`）。

---

## 4. 关键代码

| 类 | 要点 |
| --- | --- |
| `MainLayout.refreshDrawer` | `isCustomer()` → My bookings；员工 → Cancellation；`canAccessAdminTools()` → Data admin |
| `RegistrationService.register` | 用户名/邮箱唯一；BCrypt 密码 |
| `AdminCatalogService` | 影片、排期、用户状态维护 |
| `SecurityConfig` | 表单登录、路由权限 |

---

## 5. 负责的测试

### 5.1 你主责的模块测试（答辩必讲）

| 测试类 | 说明 |
| --- | --- |
| `RegistrationServiceTest` | 注册为 `CUSTOMER`、字段校验 |
| `AdminCatalogServiceTest` | 管理端逻辑（如用户搜索） |

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=RegistrationServiceTest,AdminCatalogServiceTest
```

### 5.2 全组汇总（你维护文档，各主责人讲解）

| 负责人 | 测试类 | 用例 |
| --- | --- | --- |
| A | `FilmListingServiceTest` 等 | TC_001–002 |
| B | `BookingServiceTest` | TC_003–007 |
| C | `CancellationServiceTest` | TC_008–010 |
| D | 上表 + 手工 | 注册/角色/Admin；TC_011 |

**你额外维护：** `DataLoaderTest`（种子完整性）、`UiThemeTest`（主题名 `hcbs`）。

```powershell
mvn "-Dmaven.repo.local=.m2/repository" clean test
```

---

## 6. 答辩 3–5 分钟提纲

**开场（约 1 分钟）**

1. 演示 `/login`：客户 / 订票员 / 管理员各登录一次，指侧栏差异。  
2. 指 `UserRole` 与 `MainLayout` 中分支。

**收尾（约 1 分钟，若时间允许）**

3. `admin` 登录 → `/admin` 改排期或账户。

**贯穿说明**

4. `RegistrationServiceTest` 证明注册逻辑。  
5. `TEST_CASES.md` 如何映射到 A/B/C/D 四个模块（**不代替**他们讲断言细节）。

---

## 7. 演示顺序（你协调）

见 [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) §6.2：**D → A → B → C → D(Admin)**。  
建议由你操作浏览器（熟悉导航）；计时每人 3–5 分钟。

---

## 8. 提交包（D8）

```text
Group_No.zip
├── src/
├── pom.xml
├── README.md
├── .Docs/（含 CONTRIBUTION_MATRIX、TEST_CASES、roles/）
```

打包前：`mvn clean test` 全绿；不含 `data/`、`target/`。

---

## 9. 协作边界

| 成员 | 协作 |
| --- | --- |
| **A** | `User` 表结构；列表页不处理登录 |
| **B/C** | 侧栏链到订票/取消；权限异常文案 |
| **全组** | 各写各模块测试；你汇总用例表与 Actual Result |

---

## 10. 自检清单

- [ ] 三类角色登录与侧栏演示顺畅  
- [ ] 能讲 `RegistrationServiceTest` 与角色表  
- [ ] `TEST_CASES.md` 每行有负责人（A/B/C/D）  
- [ ] `mvn clean test` 全绿  
- [ ] 彩排按 §6.2 跑通一轮  

---

## 11. 相关文档

| 文档 | 用途 |
| --- | --- |
| [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) | 总表 §2 功能主责 |
| [TEST_CASES.md](../TEST_CASES.md) | 正式用例表 |
| [TEST_DATABASE.md](../TEST_DATABASE.md) | 9 个演示账号 |
| [README_CN.md](../README_CN.md) | 功能与业务规则 |
