# 成员 C — 模块 3：账户 · 取消 · 导航

**路由：** `/login` · `/register` · `/cancellation` · `/my-bookings` · 全局侧栏  
**组内目标：** 约 25%  
**完整文件列表：** [四人分工.md §成员 C](../四人分工.md#成员-c--模块-3账户--取消--导航)

---

## 1. 模块职责

负责「用户是谁、能去哪、订后怎么办、菜单长什么样」**端到端**：

```text
登录注册：LoginView → RegistrationService → SecurityConfig / User
取消：CancellationView / MyBookingsView → CancellationService → Booking
导航：MainLayout.refreshDrawer → 按 UserRole 显示菜单
```

> 本模块合并 auth + cancellation + shell，避免「只做取消太轻、Auth 与导航割裂」。

---

## 2. 核心文件

| 子域 | 文件 |
| --- | --- |
| 账户 | `LoginView`, `RegisterView`, `LogoutView`, `AccountCenterView`, `RegistrationService` |
| 安全 | `SecurityConfig`, `HcbsUserDetailsService`, `CurrentUserService`, `AuthUiService`, `DemoAccountCatalog` |
| 取消 | `CancellationView`, `MyBookingsView`, `CancellationService` |
| 导航 | `MainLayout`, `AppShell`, `PageHero` |
| 工具 | `PhoneNumbers` |
| Test | `RegistrationServiceTest`, `CancellationServiceTest` |
| 用例 | TC_008–TC_011；注册/角色演示（手工） |

---

## 相关代码总览

从本页链接可直接在 IDE 中打开对应文件。建议按 **阅读顺序** 通读一遍，答辩前再按 **入口方法** 回看重点。

### 建议阅读顺序

| 步骤 | 主题 | 从这里开始 |
| --- | --- | --- |
| 1 | 全局导航与角色菜单 | [MainLayout.java](../../src/main/java/com/hcbs/web/shell/MainLayout.java) → `refreshDrawer()` |
| 2 | 登录与安全链 | [SecurityConfig.java](../../src/main/java/com/hcbs/config/SecurityConfig.java) → [LoginView.java](../../src/main/java/com/hcbs/web/auth/LoginView.java) → `submitLogin()` |
| 3 | 注册与当前用户 | [RegistrationService.java](../../src/main/java/com/hcbs/service/auth/RegistrationService.java) → [RegisterView.java](../../src/main/java/com/hcbs/web/auth/RegisterView.java) |
| 4 | 取消业务与页面 | [CancellationService.java](../../src/main/java/com/hcbs/service/cancellation/CancellationService.java) → [CancellationView.java](../../src/main/java/com/hcbs/web/cancellation/CancellationView.java) / [MyBookingsView.java](../../src/main/java/com/hcbs/web/cancellation/MyBookingsView.java) |
| 5 | 自动化用例 | [CancellationServiceTest.java](../../src/test/java/com/hcbs/service/cancellation/CancellationServiceTest.java) · [RegistrationServiceTest.java](../../src/test/java/com/hcbs/service/auth/RegistrationServiceTest.java) |

### 板块 README（先读说明再读代码）

| 文档 | 内容 |
| --- | --- |
| [auth/README.md](../../src/main/java/com/hcbs/web/auth/README.md) | 登录、注册、账户中心流程 |
| [shell/README.md](../../src/main/java/com/hcbs/web/shell/README.md) | `MainLayout` 顶栏/侧栏、`afterNavigation` |
| [cancellation/README.md](../../src/main/java/com/hcbs/web/cancellation/README.md) | 员工柜台 vs 客户自助取消 |

### Web 层（Vaadin 页面）

| 路由 | 文件 | 答辩/阅读要点 |
| --- | --- | --- |
| `/login` | [LoginView.java](../../src/main/java/com/hcbs/web/auth/LoginView.java) | `HttpServletRequest.login`、`?redirect=`、`buildDemoAccountPanel` |
| `/register` | [RegisterView.java](../../src/main/java/com/hcbs/web/auth/RegisterView.java) | 仅客户注册 → `RegistrationService` |
| `/logout` | [LogoutView.java](../../src/main/java/com/hcbs/web/auth/LogoutView.java) | `beforeEnter` → `AuthUiService.signOut` |
| `/account` | [AccountCenterView.java](../../src/main/java/com/hcbs/web/auth/AccountCenterView.java) | 未登录/已登录两套面板；`layout = MainLayout` |
| `/cancellation` | [CancellationView.java](../../src/main/java/com/hcbs/web/cancellation/CancellationView.java) | 员工按手机号查单；演示 `HCBS-SEED001` |
| `/my-bookings` | [MyBookingsView.java](../../src/main/java/com/hcbs/web/cancellation/MyBookingsView.java) | 客户只看本人订单 |
| （全局） | [MainLayout.java](../../src/main/java/com/hcbs/web/shell/MainLayout.java) | **`refreshDrawer()`** 四种角色侧栏 |
| （全局） | [AppShell.java](../../src/main/java/com/hcbs/web/shell/AppShell.java) | `@Theme("hcbs")` 全站样式 |
| （组件） | [PageHero.java](../../src/main/java/com/hcbs/web/component/PageHero.java) | 登录/注册/取消页统一页头 |

包级说明：[auth/package-info.java](../../src/main/java/com/hcbs/web/auth/package-info.java) · [cancellation/package-info.java](../../src/main/java/com/hcbs/web/cancellation/package-info.java) · [shell/package-info.java](../../src/main/java/com/hcbs/web/shell/package-info.java)

### Service 与安全

| 文件 | 说明 | 关键方法 |
| --- | --- | --- |
| [RegistrationService.java](../../src/main/java/com/hcbs/service/auth/RegistrationService.java) | 客户注册校验与入库 | `registerCustomer` · `validate` |
| [CancellationService.java](../../src/main/java/com/hcbs/service/cancellation/CancellationService.java) | 取消规则、手续费、释座 | `canCancel` · `cancelBooking` · `calculateCancellationCharge` |
| [SecurityConfig.java](../../src/main/java/com/hcbs/config/SecurityConfig.java) | Vaadin + Spring Security | `configure` · `passwordEncoder` |
| [HcbsUserDetailsService.java](../../src/main/java/com/hcbs/security/HcbsUserDetailsService.java) | 按手机号加载用户 | `loadUserByUsername` |
| [CurrentUserService.java](../../src/main/java/com/hcbs/security/CurrentUserService.java) | 当前用户、角色判断 | `isAuthenticated` · `requireCurrentUser` |
| [AuthUiService.java](../../src/main/java/com/hcbs/security/AuthUiService.java) | 登出 | `signOut` |
| [DemoAccountCatalog.java](../../src/main/java/com/hcbs/config/DemoAccountCatalog.java) | 演示账号（密码 `demo`） | `all` |

### 工具 · 模型 · 数据（本模块会用到）

| 文件 | 说明 |
| --- | --- |
| [PhoneNumbers.java](../../src/main/java/com/hcbs/util/PhoneNumbers.java) | 登录/注册/取消共用手机号规范化 |
| [RegistrationRequest.java](../../src/main/java/com/hcbs/dto/RegistrationRequest.java) | 注册表单 DTO |
| [CustomerBookingRow.java](../../src/main/java/com/hcbs/dto/CustomerBookingRow.java) | 取消列表 Grid 行 |
| [BookingSummary.java](../../src/main/java/com/hcbs/dto/BookingSummary.java) | 取消结果摘要 |
| [User.java](../../src/main/java/com/hcbs/model/User.java) · [UserRole.java](../../src/main/java/com/hcbs/model/UserRole.java) · [UserStatus.java](../../src/main/java/com/hcbs/model/UserStatus.java) | 用户与角色（侧栏逻辑依赖 `UserRole`） |
| [UserRepository.java](../../src/main/java/com/hcbs/repository/UserRepository.java) | 注册/登录查库 |

### 测试与用例表

| 文件 | 对应用例 |
| --- | --- |
| [CancellationServiceTest.java](../../src/test/java/com/hcbs/service/cancellation/CancellationServiceTest.java) | TC_008–010（`cancelsBookingAndAppliesFiftyPercentCharge`、`rejectsSameDayCancellation` 等） |
| [RegistrationServiceTest.java](../../src/test/java/com/hcbs/service/auth/RegistrationServiceTest.java) | 注册校验（重复用户名/手机/密码等） |
| [TEST_CASES.md](../TEST_CASES.md) | TC_008–011 全表 |

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=RegistrationServiceTest,CancellationServiceTest
```

### 协作只读（非 C 主责，取消/登录联调时会碰到）

| 文件 | 关系 |
| --- | --- |
| [BookingRepository.java](../../src/main/java/com/hcbs/repository/BookingRepository.java) | C 只读查单、按手机检索 |
| [BookingSeatRepository.java](../../src/main/java/com/hcbs/repository/BookingSeatRepository.java) | 取消时删除座位占用 |
| [BackToHomeAction.java](../../src/main/java/com/hcbs/web/home/component/BackToHomeAction.java) | 登录/注册页「回首页」（A 维护） |
| [BookingView.java](../../src/main/java/com/hcbs/web/booking/BookingView.java) | 未登录访问时转发 `LoginView`（B↔C 联调示例） |

---

## 3. 三类角色（答辩必讲）

| 角色 | 侧栏 |
| --- | --- |
| 未登录 | 仅 Home |
| CUSTOMER | Home + My bookings |
| BOOKING_STAFF | Home + Book tickets + Cancellation |
| ADMIN | 上述 + Data admin |

关键代码：`MainLayout.refreshDrawer()`。

---

## 4. 取消规则

| 规则 | 说明 |
| --- | --- |
| 提前 1 天 | `today.isBefore(showDate)` |
| 50% 手续费 | 总价 × 0.5 |
| 当日拒绝 | TC_010 |
| 释放座位 | 删除 `BookingSeat` |

---

## 5. 答辩 5 分钟

1. 未登录访问 `/booking` → Login → redirect 回来  
2. 员工 → Cancellation → `HCBS-SEED001` 取消  
3. 讲解 `MainLayout` 四种角色菜单  
4. 测试：`CancellationServiceTest` + `RegistrationServiceTest`  

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=RegistrationServiceTest,CancellationServiceTest
```

---

## 6. 协作边界

| 同事 | 交界 |
| --- | --- |
| **B** | 只读 `Booking` / `BookingRepository` |
| **D** | Admin 启停 User；D 不实现登录 |
| **A** | `BackToHomeAction` 在 auth 页使用（A 维护组件） |

---

## 7. 自检

- [ ] 能演示三种角色侧栏差异  
- [ ] 能讲 Login 流程（`HttpServletRequest.login`）  
- [ ] TC_008–011 对应测试方法  

---

## 8. 相关文档

- 源码导航见上文 **[相关代码总览](#相关代码总览)**  
- [auth/README.md](../../src/main/java/com/hcbs/web/auth/README.md)  
- [shell/README.md](../../src/main/java/com/hcbs/web/shell/README.md)  
- [cancellation/README.md](../../src/main/java/com/hcbs/web/cancellation/README.md)  
- [四人分工.md §成员 C](../四人分工.md#成员-c--模块-3账户--取消--导航)（完整文件表）
