# booking/component — SeatMapPicker 座位图

答辩推荐用这个组件回答：**「讲一个 Vaadin 组件及其代码」**。

## SeatMapPicker.java

### 类定义

```java
public class SeatMapPicker extends VerticalLayout
```

自定义复合组件，不是 Vaadin 内置，由多个 `Button` + `Div` 拼成 10×10 座位网格。

### 座位布局（与课程规格一致）

```text
每行 10 列：3 + 过道 + 4 + 过道 + 3
行号 1–10，列号显示在按钮上
```

布局规则来自 `SeatGridFormat`（config 包，成员 A/B 定义）。

### 内部结构（从上到下）

| 区域 | class | 说明 |
|------|-------|------|
| showtimeStrip | `booking-showtime-strip` | 同片其他场次 chip 按钮 |
| screenBlock | `booking-screen-block` | 「Screen」标识 |
| seatGridHost | `booking-seat-grid` | 座位按钮矩阵 |
| selectionHint | `booking-selection-hint` | 「已选 N 座 · £总价」 |
| legend | `booking-seat-legend` | 图例：Available / Reserved / Selected |

### 核心 API（BookingView 如何调用）

```java
SeatMapPicker seatPicker = new SeatMapPicker();

// 1. 设置同片场次切换
seatPicker.setShowtimes(chips, activeShowingId, this::switchShowing);

// 2. 加载座位数据
seatPicker.setSeats(bookingService.listSeatMap(showingId));

// 3. 读取用户选择
Set<SeatMapSeat> selected = seatPicker.getSelectedSeats();

// 4. 下单后清空并刷新
seatPicker.clearSelection();
seatPicker.setSeats(updatedList);
```

### 座位数据 `SeatMapSeat`（DTO）

| 字段 | 用途 |
|------|------|
| `seatId` | 提交给 `createBooking` |
| `seatNumber` | 如 "A5"，显示在 tooltip |
| `ticketPrice` | 单价，选中后汇总 |
| `available` | false = 已售，按钮禁用 |

### 座位按钮状态

```java
applySeatStyle(button, seat):
  !available     → booking-seat-reserved（灰，disabled）
  selectedIds 含 → booking-seat-selected（高亮）
  否则           → booking-seat-available（可点）
```

### 点击逻辑 `toggleSeat()`

```java
if (selectedIds.contains(seatId)) {
    selectedIds.remove(seatId);  // 取消选择
} else {
    selectedIds.add(seatId);     // 选中
}
applySeatStyle(...);
fireSelectionChanged();  // 更新底部 hint 文字
```

### ShowtimeOption record

```java
public record ShowtimeOption(Long showingId, LocalTime startTime) {
    public String label() { return TIME.format(startTime); }  // "18:30"
}
```

chip 点击 → 回调 `onShowtimeSelected.accept(showingId)` → BookingView 的 `switchShowing()`。

---

## 答辩示例话术

> SeatMapPicker 是我们封装的 Vaadin 组件。它继承 VerticalLayout，内部用 Grid 布局的 Button 表示每个座位。数据来自 BookingService.listSeatMap 返回的 SeatMapSeat 列表。用户点击 available 状态的 Button 会 toggle 选中，选中的座位 ID 集合通过 getSelectedSeats() 交给 BookingView，最终传给 BookingService.createBooking。我们不在 View 里判断座位是否重复售出，那是 Service 的责任。

---

## 样式相关 class

在 `styles.css` 中搜索 `booking-seat` 可找到完整样式：
- `booking-seat-available`
- `booking-seat-selected`
- `booking-seat-reserved`
- `booking-seat-aisle`（过道空隙）

---

## 为什么不用弹窗选座？

当前实现：**订票页内嵌座位图**（非弹窗 Dialog）。

| 方案 | 优点 | 缺点 |
|------|------|------|
| 同页嵌入（现方案） | 收据、政策同屏可见；员工柜台操作连贯 | 页面较长 |
| Dialog 弹窗 | 焦点集中 | 员工需多次开关；收据区被挡住 |

答辩若被问到设计选择，可按上表回答。
