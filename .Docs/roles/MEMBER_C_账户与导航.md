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

- [auth/README.md](../../src/main/java/com/hcbs/web/auth/README.md)  
- [shell/README.md](../../src/main/java/com/hcbs/web/shell/README.md)  
- [cancellation/README.md](../../src/main/java/com/hcbs/web/cancellation/README.md)
