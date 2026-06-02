# B 分工阅读指引 — Booking + BookingServiceTest

> **负责人：B**  
> **范围：** 订票业务（Service 层为主）+ `BookingServiceTest` + 自己负责的 Web 界面  
> **原则：** 按**功能/接口**分工，不是按「前后端」分工；前端只看 `booking` 包内代码，其余模块知道入口即可。

---

## 一、你要讲清楚什么

答辩或演示时，B 需要能完整说明：

1. **订票流程**：选场次 → 加载座位图 → 选座 → 确认 → 生成订单与收据  
2. **两种角色**：员工柜台（代客订票、必填客户手机） vs 客户自助（只能为自己订票）  
3. **业务规则**：7 天内可订、座位不可重复、价格按城市/时段/座位区域计算、Guest 订票  
4. **测试覆盖**：`BookingServiceTest` 中每个用例对应哪条规则  

---

## 二、建议阅读顺序

```text
1. BookingServiceTest          ← 先读测试，知道「应该发生什么」
2. BookingService              ← 核心业务逻辑（最重要）
3. Model + Repository          ← 数据结构与持久化
4. DTO                         ← Service 与 View 之间的数据形状
5. booking/BookingView         ← UI 如何调用 Service
6. booking/component/SeatMapPicker  ← 座位图组件
7. 外部依赖（只扫一眼）         ← Showing、Seat、PriceRule、CurrentUserService
```

---

## 三、核心业务（必读）

### 3.1 Service — 你的主战场

| 文件 | 说明 |
|------|------|
| [`BookingService.java`](BookingService.java) | **核心**。场次列表、座位图、搜客户手机、创建订单、计价、校验 |

**重点方法（建议逐行读懂）：**

| 方法 | 作用 |
|------|------|
| `listBookableShowings()` | 今天起 7 天内可订场次 |
| `findBookingContext(showingId)` | 场次上下文 + 同片同日其他场次 |
| `listSeatMap(showingId)` | 10×10 座位图数据（含是否可售、单价） |
| `searchCustomerPhones(prefix)` | 员工搜客户手机（至少 3 位前缀） |
| `createBooking(showingId, seatIds, customerPhone)` | **下单入口**，事务内写 Booking + BookingSeat |
| `calculateTicketPrice()` / `calculateTotalCost()` | 查 PriceRule 算价 |
| `validateBookingDate()` | 不能订过期 / 超过 7 天 |
| `validateSeatsAvailable()` | 座位归属屏幕、不能重复售出 |
| `generateBookingReference()` | 生成 `HCBS-XXXXXXXX` 唯一单号 |

---

### 3.2 领域模型

| 文件 | 说明 |
|------|------|
| [`model/Booking.java`](../../model/Booking.java) | 订单主表：reference、showing、customer/guestPhone、总价、状态 |
| [`model/BookingSeat.java`](../../model/BookingSeat.java) | 订单-座位明细；`(showing, seat)` 唯一约束防重复 |
| [`model/BookingStatus.java`](../../model/BookingStatus.java) | `CONFIRMED` / `CANCELLED`（取消逻辑属 C，但你要知道状态含义） |

**Booking 关键字段：**

- `createdBy`：谁操作的（员工或客户本人）  
- `customer`：注册用户；Guest 订时为 `null`  
- `guestPhone`：无账号时用手机号标识客户  

---

### 3.3 Repository

| 文件 | 说明 |
|------|------|
| [`repository/BookingRepository.java`](../../repository/BookingRepository.java) | 按 reference 查单、按客户/手机查历史（部分查询给 C 用） |
| [`repository/BookingSeatRepository.java`](../../repository/BookingSeatRepository.java) | **`existsActiveReservationForShowingAndSeat`** — 判断座位是否已售 |

Booking 流程里最依赖的是 `BookingSeatRepository.existsActiveReservationForShowingAndSeat`。

---

### 3.4 DTO（Booking 专用）

| 文件 | 说明 |
|------|------|
| [`dto/BookingReceipt.java`](../../dto/BookingReceipt.java) | 下单返回的收据；`toReceiptText()` 供 View 显示 |
| [`dto/BookingShowingContext.java`](../../dto/BookingShowingContext.java) | 页面标题区：片名、影院、屏幕、日期、同片场次 |
| [`dto/SeatMapSeat.java`](../../dto/SeatMapSeat.java) | 座位图一格：seatId、编号、单价、available |
| [`dto/ShowingOption.java`](../../dto/ShowingOption.java) | 场次下拉选项 |
| [`dto/ShowingTimeSlot.java`](../../dto/ShowingTimeSlot.java) | 同片场次 chip |
| [`dto/PhoneSearchOption.java`](../../dto/PhoneSearchOption.java) | 员工搜客户手机下拉 |
| [`dto/SeatOption.java`](../../dto/SeatOption.java) | `listAvailableSeats` 用（View 主要用 SeatMapSeat） |

**不必深入（属 C）：** `BookingSummary.java`、`CustomerBookingRow.java` — 取消/我的订单用。

---

### 3.5 测试 — BookingServiceTest

| 文件 | 说明 |
|------|------|
| [`test/.../BookingServiceTest.java`](../../../../test/java/com/hcbs/service/booking/BookingServiceTest.java) | **你的测试职责**，共 6 个用例 |

| 测试方法 | 验证的业务规则 |
|----------|----------------|
| `createsBookingWithReceiptValuesAndReservesSeats` | 员工代 Alice 订 2 座，总价 £24，座位被占用 |
| `rejectsDuplicateSeatForSameShowing` | 同一座位不能重复订 |
| `rejectsBookingMoreThanSevenDaysInAdvance` | 超过 7 天不可订 |
| `customerBooksForSelfWithoutCustomerPicker` | 客户自助，`customerPhone=null`，customer=bookedBy |
| `createsGuestBookingWhenPhoneHasNoAccount` | 员工为无账号手机订 Guest 单 |
| `searchesCustomerPhonesByPrefix` | 手机前缀搜索（≥3 位） |

**测试环境要点：**

- `@SpringBootTest` + 内存 H2，`ddl-auto=create-drop`  
- `@WithMockUser` 模拟员工 `13800238001` / 客户 `13800138001`  
- 种子数据来自 [`config/DataLoader.java`](../../config/DataLoader.java) → [`HcbsTestDataSeeder.java`](../../config/HcbsTestDataSeeder.java)  
- 演示账号定义在 [`config/DemoAccountCatalog.java`](../../config/DemoAccountCatalog.java)  

**运行测试：**

```bash
./mvnw test -Dtest=BookingServiceTest
```

---

## 四、Web 界面（只看 booking 包）

### 4.1 页面与组件

| 文件 | 说明 |
|------|------|
| [`web/booking/BookingView.java`](../../web/booking/BookingView.java) | 路由 `/booking`；登录检查、选场、选座、确认 |
| [`web/booking/component/SeatMapPicker.java`](../../web/booking/component/SeatMapPicker.java) | 10×10 座位图；选中/取消、场次 chip 切换 |
| [`web/booking/README.md`](../../web/booking/README.md) | **BookingView 流程详解**（答辩必看） |
| [`web/booking/component/README.md`](../../web/booking/component/README.md) | **SeatMapPicker 组件详解** |

### 4.2 BookingView 关键流程

```text
beforeEnter
  ├─ 未登录 → forwardTo LoginView（带 redirect）
  └─ 已登录 → loadShowing(?showingId)

buildWorkspace
  ├─ 员工：显示 customerPhone ComboBox
  └─ 客户：隐藏手机框

confirm()
  └─ bookingService.createBooking(showingId, seatIds, phone)
       → receipt 显示 → 刷新座位图
```

### 4.3 样式（选读）

| 文件 | 说明 |
|------|------|
| [`frontend/themes/hcbs/styles.css`](../../../../../frontend/themes/hcbs/styles.css) | 搜索 `booking-` 前缀（约 642 行起）：workspace、seat-grid、seat-available/selected/reserved |

### 4.4 共用 Web 工具（只读相关方法）

| 文件 | 只看什么 |
|------|----------|
| [`web/home/ShowingFilterQuery.java`](../../web/home/ShowingFilterQuery.java) | `PARAM_SHOWING`、`withShowingId()` — URL 传 `?showingId=` |
| [`web/component/PageHero.java`](../../web/component/PageHero.java) | 页面顶部标题区（BookingView 用到） |

---

## 五、外部依赖（了解接口即可，细节找对应同学）

Booking 会用到 A 维护的数据，但**实现不在 B 范围**：

### 5.1 A 负责 — 场次 / 场馆 / 价格

| 文件 | B 需要知道什么 |
|------|----------------|
| [`model/Showing.java`](../../model/Showing.java) | 场次：日期、时间、关联 Film/Screen |
| [`model/Seat.java`](../../model/Seat.java) | 座位编号、所属 Screen、SeatArea |
| [`model/PriceRule.java`](../../model/PriceRule.java) | 城市 + 时段 + 座位区域 → 单价 |
| [`repository/ShowingRepository.java`](../../repository/ShowingRepository.java) | `findById`、`findByFilmAndShowDate` |
| [`repository/SeatRepository.java`](../../repository/SeatRepository.java) | `findByScreenAndSeatArea` |
| [`repository/PriceRuleRepository.java`](../../repository/PriceRuleRepository.java) | `findByCityAndTimeBandAndSeatArea` |
| [`config/SeatGridFormat.java`](../../config/SeatGridFormat.java) | 座位编号 `R01C01` 格式与排序（座位图布局） |

### 5.2 D 负责 — 登录 / 角色

| 文件 | B 需要知道什么 |
|------|----------------|
| [`security/CurrentUserService.java`](../../security/CurrentUserService.java) | `requireCurrentUser()`、`isEmployee()` — 区分员工/客户 |
| [`model/User.java`](../../model/User.java) | 用户与 `UserRole` |
| [`model/UserRole.java`](../../model/UserRole.java) | `BOOKING_STAFF`、`CUSTOMER` 等 |
| [`util/PhoneNumbers.java`](../../util/PhoneNumbers.java) | 手机号规范化与校验 |

### 5.3 跨板块入口（扫一眼，不用深读）

| 文件 | 与 Booking 的关系 |
|------|-------------------|
| [`web/home/FilmDetailView.java`](../../web/home/FilmDetailView.java) | 详情页 **Book** 按钮 → `BookingView?showingId=`（A 的页面，一条 navigate） |
| [`web/auth/LoginView.java`](../../web/auth/LoginView.java) | 未登录时 forward 目标；登录后 redirect 回 booking |
| [`web/shell/MainLayout.java`](../../web/shell/MainLayout.java) | 侧栏 **Book tickets** 仅员工可见（约 148–149 行） |

---

## 六、与其他模块的分工边界

| 模块 | 范围 | 与 B（订票）的交界 |
|------|------|-------------|
| **A** | 影片浏览 Home | 提供 Showing/Seat/PriceRule；详情页 Book → booking |
| **B（你）** | 订票全流程 + `BookingServiceTest` | 创建订单；不实现取消 |
| **C** | 账户 · 取消 · 导航 | `CurrentUserService` 区分员工/客户；取消读你写的 Booking |
| **D** | 数据管理 · 平台 | 种子数据；Admin 排片 |

完整文件列表：[.Docs/四人分工.md](../../../../../../.Docs/四人分工.md)

**协作约定：**

- View **不解析业务错误**，直接显示 `BookingService` 抛出的 `message`  
- 座位是否已售、能否超 7 天订 — **只在 Service 判断**，不在 SeatMapPicker 里重复  
- 取消、退款、我的订单列表 — **不问 B**，问 C  

---

## 七、答辩速查

### 一句话架构

> `BookingView` 收集用户选择 → 调用 `BookingService.createBooking` → Service 校验权限与规则 → 写 `Booking` + `BookingSeat` → 返回 `BookingReceipt` 显示在页面。

### 推荐演示路径

1. 员工 `13800238001` 登录 → 侧栏 Book tickets  
2. 或从 home 详情页 Book（带 showingId）  
3. 选座 → Confirm → 看收据与座位变灰  
4. 再点同一座 → 应报错「already booked」（对应测试 `rejectsDuplicateSeatForSameShowing`）

### 配套文档

| 文档 | 用途 |
|------|------|
| [`web/booking/README.md`](../../web/booking/README.md) | 页面事件与流程 |
| [`web/booking/component/README.md`](../../web/booking/component/README.md) | SeatMapPicker 组件答辩话术 |
| [`web/答辩指南.md`](../../web/答辩指南.md) | 全项目答辩 Q&A（订票部分见第二节） |

---

## 八、文件清单（按路径）

### 必读（B 核心）

```
src/main/java/com/hcbs/service/booking/BookingService.java
src/test/java/com/hcbs/service/booking/BookingServiceTest.java
src/main/java/com/hcbs/model/Booking.java
src/main/java/com/hcbs/model/BookingSeat.java
src/main/java/com/hcbs/model/BookingStatus.java
src/main/java/com/hcbs/repository/BookingRepository.java
src/main/java/com/hcbs/repository/BookingSeatRepository.java
src/main/java/com/hcbs/dto/BookingReceipt.java
src/main/java/com/hcbs/dto/BookingShowingContext.java
src/main/java/com/hcbs/dto/SeatMapSeat.java
src/main/java/com/hcbs/dto/ShowingOption.java
src/main/java/com/hcbs/dto/ShowingTimeSlot.java
src/main/java/com/hcbs/dto/PhoneSearchOption.java
src/main/java/com/hcbs/web/booking/BookingView.java
src/main/java/com/hcbs/web/booking/component/SeatMapPicker.java
```

### 选读（依赖与上下文）

```
src/main/java/com/hcbs/security/CurrentUserService.java
src/main/java/com/hcbs/util/PhoneNumbers.java
src/main/java/com/hcbs/config/SeatGridFormat.java
src/main/java/com/hcbs/config/DemoAccountCatalog.java
src/main/java/com/hcbs/config/DataLoader.java
src/main/java/com/hcbs/model/Showing.java
src/main/java/com/hcbs/model/Seat.java
src/main/java/com/hcbs/model/PriceRule.java
src/main/java/com/hcbs/repository/ShowingRepository.java
src/main/java/com/hcbs/repository/SeatRepository.java
src/main/java/com/hcbs/repository/PriceRuleRepository.java
src/main/java/com/hcbs/web/home/ShowingFilterQuery.java
frontend/themes/hcbs/styles.css          （booking-* 样式）
```

### 边界文件（知道存在即可）

```
src/main/java/com/hcbs/web/home/FilmDetailView.java      ← Book 按钮入口
src/main/java/com/hcbs/web/auth/LoginView.java           ← 未登录跳转
src/main/java/com/hcbs/web/shell/MainLayout.java         ← 侧栏 Book tickets
src/main/java/com/hcbs/dto/BookingSummary.java           ← C 用
src/main/java/com/hcbs/dto/CustomerBookingRow.java       ← C 用
src/main/java/com/hcbs/service/cancellation/*            ← C 负责，不读
```
