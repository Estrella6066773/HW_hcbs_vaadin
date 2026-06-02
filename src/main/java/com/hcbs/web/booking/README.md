# booking — Book tickets 板块

对应侧栏 **Book tickets**：Desk booking for any showing。

## 文件清单

| 文件 | 路由 | 说明 |
|------|------|------|
| `BookingView.java` | `/booking` | 订票主页面 |
| [component/SeatMapPicker.java](component/README.md) | — | 10×10 座位图 |

## 依赖

| 类型 | 名称 | 用途 |
|------|------|------|
| Service | `BookingService` | 场次、座位图、创建订单 |
| Service | `CurrentUserService` | 登录状态、员工/客户判断 |
| View | `LoginView` | 未登录时转发 |
| 工具 | `ShowingFilterQuery` | 从 URL 读 `showingId` |

---

## BookingView.java

### 路由与安全

```java
@Route(value = "booking", layout = MainLayout.class)
@AnonymousAllowed   // 注解允许访问，但 beforeEnter 里会检查登录
```

实际逻辑：**未登录用户在 `beforeEnter` 被转发到 LoginView**，不是用 `@RolesAllowed` 拦。

### 页面布局

```text
PageHero
└── HorizontalLayout (booking-workspace)
    ├── 左：formPanel（选座表单）
    │     ├── customerPhone ComboBox（仅员工可见）
    │     ├── showing ComboBox
    │     ├── pickerHost（标题 + SeatMapPicker）
    │     └── Confirm booking 按钮
    └── 右：receiptPanel
          ├── TextArea 收据
          └── 政策说明卡片
```

### 完整流程（答辩必背）

#### 第 1 步：进入页面 `beforeEnter()`

```text
从 URL 读 showingId
  ├─ 未登录 → forwardTo(LoginView?redirect=booking...)
  └─ 已登录 → buildWorkspace()（仅首次）→ loadShowing(showingId)
```

**跨板块交互：** home 详情页 Book 按钮带 `?showingId=456` 进来，这里直接加载该场次。

#### 第 2 步：构建界面 `buildWorkspace()`

根据 `currentUserService.isEmployee()` 分两种模式：

| | 员工柜台 | 客户自助 |
|--|---------|---------|
| customerPhone | 显示，必填 | 隐藏 |
| PageHero 文案 | Employee desk | Self-service |
| createBooking 的 phone | 员工输入的客户手机 | null（用当前登录用户） |

#### 第 3 步：加载场次 `loadShowing()` → `applyContext()`

```java
bookingService.findBookingContext(showingId)  // 影片名、影院、同片其他场次
bookingService.listSeatMap(showingId)         // 座位列表 SeatMapSeat
```

`SeatMapPicker.setShowtimes()` 显示同一天其他场次 chip，可切换。

#### 第 4 步：用户选座

`SeatMapPicker` 内部 Button 网格，点击切换选中状态（见 component 文档）。

#### 第 5 步：确认订票 `confirm()`

```text
校验：已登录、有场次、至少选一个座、员工需填手机
  ↓
bookingService.createBooking(showingId, seatIds, phone)
  ↓
receipt.setValue(bookingReceipt.toReceiptText())
seatPicker.setSeats(...)  // 刷新座位图（已售变灰）
Notification.show("Booking confirmed")
```

异常：`catch (RuntimeException ex) → Notification.show(ex.getMessage())`  
**不在 View 里解析业务错误**，直接显示 Service 抛的消息。

### 触发事件汇总（答辩「什么事件触发什么」）

| 事件 | 处理器 | 结果 |
|------|--------|------|
| 进入 `/booking` | `beforeEnter` | 检查登录、加载场次 |
| URL 带 showingId | `loadShowing` | 预选场次 |
| ComboBox 换 showing | `switchShowing` | 换座位图 |
| 座位 Button 点击 | `SeatMapPicker.toggleSeat` | 选中/取消 |
| Confirm 按钮 | `confirm()` | 调用 Service 下单 |
| 未登录访问 | `beforeEnter` | 跳转 login |

### 使用的 Vaadin 组件

| 组件 | 用途 |
|------|------|
| `ComboBox<PhoneSearchOption>` | 员工搜客户手机（懒加载） |
| `ComboBox<ShowingOption>` | 选手动场次 |
| `SeatMapPicker` | 自定义座位图 |
| `TextArea` | 收据预览 |
| `Notification` | 成功/错误提示 |
| `HorizontalLayout` | 左右分栏 |

### configurePhoneComboBox — 懒加载搜索

```java
comboBox.setItems(query -> search.apply(filter).stream()
    .skip(query.getOffset())
    .limit(query.getLimit()));
```

用户打字时按前缀查 `BookingService.searchCustomerPhones`，Vaadin ComboBox 分页加载。

---

## 与其他板块的交互

```text
home/FilmDetailView
  ──Book──→ booking/BookingView (?showingId)

auth/LoginView
  ←──forwardTo── booking (未登录)
  ──login 成功 redirect──→ booking

shell/MainLayout
  ──侧栏 Book tickets──→ booking (仅员工)
```

**注意：** 客户从详情页 Book 也会进 booking，但侧栏不显示 Book tickets 菜单项（客户通过详情页或 Account 中心进入）。
