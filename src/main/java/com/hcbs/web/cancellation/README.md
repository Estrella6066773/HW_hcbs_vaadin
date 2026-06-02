# cancellation — Cancellation 板块

对应侧栏 **Cancellation**（员工）和 **My bookings**（客户）。两个 View 共用 `CancellationService`，UI 不同。

## 文件清单

| 文件 | 路由 | 角色 | 说明 |
|------|------|------|------|
| `CancellationView.java` | `/cancellation` | STAFF、ADMIN | 柜台：按手机号查任意订单 |
| `MyBookingsView.java` | `/my-bookings` | CUSTOMER | 自助：只看自己的订单 |

## 依赖

| Service | 方法 | 用途 |
|---------|------|------|
| `CancellationService` | `searchPhonesWithBookings` | 员工搜手机 |
| | `listBookingsByPhone` | 按手机列订单 |
| | `listAccessibleBookings` | 客户列自己的订单 |
| | `cancelBooking` | 执行取消 |

---

## CancellationView.java — 员工取消柜台

### 路由与安全

```java
@Route(value = "cancellation", layout = MainLayout.class)
@RolesAllowed({"BOOKING_STAFF", "ADMIN"})
```

未授权角色访问会由 Spring Security 拦截。

### 页面布局

```text
PageHero ("Refund control")
└── HorizontalLayout
    ├── lookupPanel（左）
    │     ├── ComboBox 客户手机
    │     └── Grid 订单列表 + Cancel 按钮
    └── rulePanel（右）
          └── 取消规则说明（50% 手续费等）
```

### 流程

#### 1. 搜手机号

```java
customerPhone.setItems(query -> cancellationService.searchPhonesWithBookings(filter)...);
```

用户选择或输入手机 → `loadBookings(phone)`：

```java
bookingsGrid.setItems(cancellationService.listBookingsByPhone(phone));
```

#### 2. Grid 列

| 列 | 数据 |
|----|------|
| Reference | 订单号 HCBS-xxx |
| Film | 片名 |
| Date / Time | 场次 |
| Tickets / Total | 张数、总价 |
| Status | CONFIRMED / CANCELLED |
| Action | Cancel 按钮 |

#### 3. Cancel 按钮逻辑

```java
boolean cancellable = row.status() == CONFIRMED && row.canCancel();
cancel.setEnabled(cancellable);
```

- `canCancel()` 来自 DTO（成员 B 根据「是否放映日」等规则计算）
- **View 不做 7 日/当日判断**，只读 DTO 布尔值

#### 4. 执行取消

```java
cancellationService.cancelBooking(reference);
loadBookings(activePhone);  // 刷新表格
Notification.show("Booking cancelled: " + reference);
```

### 规则面板（右侧文案）

| 规则 | 展示 |
|------|------|
| Before showing day | Allowed |
| Same day | Rejected |
| Cancellation charge | 50% of total |

文案是 UI 说明；实际校验在 `CancellationService`。

---

## MyBookingsView.java — 客户自助

### 路由与安全

```java
@Route(value = "my-bookings", layout = MainLayout.class)
@RolesAllowed("CUSTOMER")
```

### 与 CancellationView 的区别

| | CancellationView | MyBookingsView |
|--|-----------------|----------------|
| 谁用 | 员工 | 客户 |
| 查订单 | 输入任意客户手机 | 自动 `listAccessibleBookings()` |
| 搜手机 ComboBox | 有 | 无 |
| PageHero | 用共享 PageHero | 内联 `pageHero()` 方法 |

### 流程

1. 构造时：`grid.setItems(cancellationService.listAccessibleBookings())`
2. 点 Cancel → `cancellationService.cancelBooking(reference)` → 刷新 grid

逻辑与员工版相同，只是数据来源不同（当前登录用户 scope）。

---

## 答辩：取消流程话术

> 取消功能分两个入口：员工在 Cancellation 页按手机号查单，客户在 My bookings 看自己的订单。Grid 每行有 Cancel 按钮，是否可点由 DTO 的 canCancel 决定，View 不自己算 50% 手续费或日期规则。点击后调用 CancellationService.cancelBooking，成功则 Notification 提示并刷新 Grid。

---

## 使用的 Vaadin 组件

| 组件 | 在哪 |
|------|------|
| `ComboBox<String>` | CancellationView 搜手机 |
| `Grid<CustomerBookingRow>` | 两个 View 都有 |
| `addComponentColumn` | Grid 里放 Cancel Button |
| `Notification` | 成功/失败反馈 |
| `PageHero` | CancellationView |

---

## 与其他板块的交互

```text
shell/MainLayout
  ├─ 员工 → CancellationView
  └─ 客户 → MyBookingsView

auth/AccountCenterView
  ├─ 客户 → MyBookingsView 快捷按钮
  └─ 员工 → CancellationView 快捷按钮

booking/BookingView
  └── 创建的订单 → 在此取消
```

演示用种子参考号：`HCBS-SEED001` 等（见项目 README）。
