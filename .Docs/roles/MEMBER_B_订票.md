# 成员 B — 模块 2：订票（Booking）

**路由：** `/booking`  
**组内目标：** 约 25%（Exercise 2 实现占比最高）  
**完整文件列表：** [四人分工.md §成员 B](../四人分工.md#成员-b--模块-2订票-booking)

---

## 1. 模块职责

负责「选场次、选座、计价、下单、出收据」**端到端**：

```text
BookingView / SeatMapPicker
  → BookingService
  → BookingRepository / BookingSeatRepository
  → Booking / BookingSeat / Seat …
```

---

## 2. 核心文件

| 类型 | 文件 |
| --- | --- |
| Service | `BookingService` |
| View | `BookingView`, `SeatMapPicker` |
| Test | `BookingServiceTest`（**6 个用例**，答辩逐个能讲） |
| 配置 | `SeatGridFormat` |
| 用例 | TC_003–TC_007 |

**必读：** [B-阅读指引.md](../../src/main/java/com/hcbs/service/booking/B-阅读指引.md)

---

## 3. 业务规则（必背）

| 规则 | 要点 |
| --- | --- |
| 7 日窗口 | 今天 ≤ 放映日 ≤ 今天+7 |
| 防重复占座 | `existsActiveReservationForShowingAndSeat` |
| 计价 | `PriceRule` + 上厅 +£2 |
| 员工代订 | 必填客户手机；Guest 无账号 |
| 客户自助 | `customerPhone=null`，订单记本人 |

---

## 4. 答辩 4 分钟

1. 员工登录 → Book tickets → 选座 → Confirm → 收据  
2. 重复选同一座 → 报错（`rejectsDuplicateSeatForSameShowing`）  
3. 调用链：`BookingView` → `BookingService.createBooking` → `BookingSeatRepository`  

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=BookingServiceTest
```

---

## 5. 协作边界

| 同事 | 交界 |
| --- | --- |
| **A** | 场次/票价数据来源；详情页入口 |
| **C** | `CurrentUserService` 区分员工/客户；未登录 forward Login |
| **C** | 取消读取 B 创建的 Booking |

---

## 6. 自检

- [ ] 6 个测试用例能对应业务规则  
- [ ] 能演示 TC_003–007  
- [ ] 未在 View 写业务规则  

---

## 7. 相关文档

- [booking/README.md](../../src/main/java/com/hcbs/web/booking/README.md)  
- [MEMBER_C_账户与导航.md](MEMBER_C_账户与导航.md)
