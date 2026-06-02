# 成员 B — Booking / 订票

**答辩主责：** 订票业务、座位图、收据  
**组内目标：** 约 25%（Exercise 2 组内实现占比最高）  
**合并前须请谁 Review：** A（占座查询）、C（取消释放座位）、D（员工代订 UI）

---

## 1. 你在项目中的位置

```text
用户：订票
  → BookingView / SeatMapPicker (你)
  → BookingService (你)
  → BookingRepository / BookingSeatRepository (与 A 协作)
  → Booking / BookingSeat (与 A 协作)
```

你是 **订票规则的唯一归属**：7 日窗口、计价、唯一参考号、防重复占座、员工代订。列表、取消、登录由 A/C/D 主责。

---

## 2. 核心交付清单

| 编号 | 工作项 | 主要文件 |
| --- | --- | --- |
| B1 | 订票 Service | `BookingService` |
| B2 | 订票 DTO | `ShowingOption`, `SeatMapSeat`, `BookingReceipt`, `BookingShowingContext`, `PhoneSearchOption` |
| B3 | 订票 UI | `BookingView`, `SeatMapPicker` |
| B4 | **模块测试** | `BookingServiceTest` |
| B5 | 协作 | 与 A 对齐 `existsActiveReservationForShowingAndSeat` |

---

## 3. `BookingService` 必讲方法

| 方法 | 作用 |
| --- | --- |
| `listBookableShowings` | 今天起 7 日内场次 |
| `listSeatMap` / `findBookingContext` | 座位图数据 |
| `searchCustomerPhones` | 员工代订搜客户手机 |
| `createBooking` | 写订单、占座、生成 `HCBS-` 参考号 |

### 业务规则（对照 README_CN）

| 规则 | 实现要点 |
| --- | --- |
| 7 日窗口 | 今天 ≤ 放映日 ≤ 今天+7 |
| 防重复占座 | `existsActiveReservationForShowingAndSeat` |
| 计价 | `PriceRule` + 上厅 +£2 |
| 员工代订 | `CurrentUserService.isEmployee()` + 客户手机 |
| 客户自助 | 订单记在当前客户名下 |

---

## 4. 负责的测试（答辩必讲）

| 测试类 | 用例 | 关键测试方法（示例） |
| --- | --- | --- |
| `BookingServiceTest` | TC_003–007 | `createsBookingWithReceiptValuesAndReservesSeats` |
| | TC_005 | `rejectsDuplicateSeatForSameShowing` |
| | TC_007 | `rejectsBookingMoreThanSevenDaysInAdvance` |
| | TC_006 | 伦敦晚场 £12×2=£24 |

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=BookingServiceTest
```

**答辩话术示例：**「我负责订票；`BookingServiceTest` 覆盖成功订票、重复占座拒绝和 7 日限制；TC_005 对应 `rejectsDuplicateSeatForSameShowing`。」

---

## 5. 答辩 3–5 分钟提纲

1. **演示**：`/booking` → 选场次 → 选座 → Confirm → 收据含 `HCBS-`。  
2. **规则**：讲清 7 日 + 防重复占座（或计价）。  
3. **代码**：指 `createBooking` 关键行。  
4. **调用链**：`BookingView` → `BookingService` → `BookingSeatRepository`。  
5. **测试**：指 `BookingServiceTest` 中 1–2 个方法名。

---

## 6. 协作边界

| 同事 | 协作 |
| --- | --- |
| **A** | 余座、价目、`searchShowings` |
| **C** | 取消后 `BookingSeat` 删除，座位可再订 |
| **D** | 登录角色决定员工/客户 UI |

**禁止：** 在 `BookingView` 写计价或 7 日判断；在 Repository 写业务分支。

---

## 7. 自检清单

- [ ] 能演示完整订票并说出参考号字段  
- [ ] 能对应 TC_003–007 与测试方法名  
- [ ] 能口述 View→Service→Repository 链  
- [ ] `BookingServiceTest` 全绿  
- [ ] View 无 `import repository`

---

## 8. 相关文档

| 文档 | 用途 |
| --- | --- |
| [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) §6 | 答辩顺序 |
| [TEST_CASES.md](../TEST_CASES.md) | TC_003–007 |
| [MEMBER_A_持久化与数据.md](MEMBER_A_持久化与数据.md) | 占座查询 |
| [MEMBER_C_Web界面.md](MEMBER_C_Web界面.md) | 取消模块 |
