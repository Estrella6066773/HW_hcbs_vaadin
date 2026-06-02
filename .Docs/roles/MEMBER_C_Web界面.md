# 成员 C — Cancellation + My Bookings

**答辩主责：** 员工取消柜台、客户我的订单  
**组内目标：** 约 25%  
**合并前须请谁 Review：** B（订单/占座状态）、D（角色与路由权限）

---

## 1. 你在项目中的位置

```text
员工：/cancellation → CancellationService (你)
客户：/my-bookings  → CancellationService.listAccessibleBookings (你)
  → CancellationView / MyBookingsView (你)
  → Booking / BookingSeat (与 A/B 协作)
```

你负责 **取消政策与订单可见性**：提前 1 天、50% 手续费、当日拒绝、取消后释放座位；客户只能看自己的单。

---

## 2. 核心交付清单

| 编号 | 工作项 | 主要文件 | 路由 |
| --- | --- | --- | --- |
| C1 | 取消 Service | `CancellationService` | — |
| C2 | 取消 DTO | `BookingSummary`, `CustomerBookingRow` | — |
| C3 | 员工取消 UI | `CancellationView` | `/cancellation` |
| C4 | 客户订单 UI | `MyBookingsView` | `/my-bookings` |
| C5 | **模块测试** | `CancellationServiceTest` | — |

> 列表、订票、登录/导航/管理由 A/B/D 主责；你只需了解侧栏如何进入你的页面。

---

## 3. `CancellationService` 必讲方法

| 方法 | 作用 |
| --- | --- |
| `findBookingSummary` | 按参考号查单 |
| `canCancel` | 仅 `CONFIRMED` 且今天早于放映日 |
| `calculateCancellationCharge` | 总价 × 50% |
| `cancelBooking` | 改状态、记费用、**删除** `BookingSeat` |
| `listAccessibleBookings` | 客户只看自己；员工可看全部 |

### 业务规则

| 规则 | 说明 |
| --- | --- |
| 提前 1 天 | `today.isBefore(showDate)` 才可取消 |
| 50% 手续费 | `totalCost * 0.5` |
| 当日拒绝 | TC_010 |
| 释放座位 | 取消后删 `BookingSeat`，TC_008 |

---

## 4. 页面流程（答辩演示）

### 4.1 `CancellationView`（员工，TC_008–011）

1. 输入 `HCBS-SEED001` → Find booking。  
2. 显示可否取消、50% 费用说明。  
3. Confirm cancel → 状态更新。

### 4.2 `MyBookingsView`（客户）

1. 客户账号登录（如 `bob`）。  
2. 列表仅自己的订单 → 可选取消。

**你只负责：** 绑定与 `Notification`；**不算费、不判断日期**——一律调 Service。

---

## 5. 负责的测试（答辩必讲）

| 测试类 | 用例 | 关键方法 |
| --- | --- | --- |
| `CancellationServiceTest` | TC_008–009 | `cancelsBookingAndAppliesFiftyPercentCharge` |
| | TC_010 | `rejectsSameDayCancellation` |

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=CancellationServiceTest
```

**答辩话术示例：**「我负责取消；`CancellationServiceTest` 验证 50% 手续费 £6 和当日拒绝；取消后删除 `BookingSeat` 释放座位。」

---

## 6. 答辩 3–5 分钟提纲

1. **演示**：`/cancellation` + `HCBS-SEED001`；或客户 `/my-bookings`。  
2. **规则**：提前 1 天、50%、当日不可取消。  
3. **代码**：指 `cancelBooking` 与 `canCancel`。  
4. **调用链**：`CancellationView` → `CancellationService` → `BookingRepository`。  
5. **测试**：指 `CancellationServiceTest` 方法名。

---

## 7. 协作边界

| 同事 | 协作 |
| --- | --- |
| **B** | 订票创建 `CONFIRMED` 与占座 |
| **A** | `BookingSeat` 数据模型 |
| **D** | `@RolesAllowed`：`CancellationView` 员工；`MyBookingsView` 客户 |

---

## 8. 自检清单

- [ ] 员工与客户两条路径都能演示  
- [ ] 能对应 TC_008–011（011 可手工说明）  
- [ ] `CancellationServiceTest` 全绿  
- [ ] View 无 repository、无手写 0.5 计费  

---

## 9. 相关文档

| 文档 | 用途 |
| --- | --- |
| [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) §6 | 答辩顺序（你在 B 之后） |
| [TEST_DATABASE.md](../TEST_DATABASE.md) | `HCBS-SEED001` 场景 |
| [MEMBER_B_应用服务.md](MEMBER_B_应用服务.md) | 订票模块 |
