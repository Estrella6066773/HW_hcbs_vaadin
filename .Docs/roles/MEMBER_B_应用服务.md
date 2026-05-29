# 成员 B — 应用服务与业务规则

**角色：** 应用服务与 DTO  
**组内目标：** 约 25%（Exercise 2 组内占比最高，约 42%）  
**负责包：** `com.hcbs.dto.*`、`com.hcbs.service.listing.*`、`com.hcbs.service.booking.*`、`com.hcbs.service.cancellation.*`、`com.hcbs.config.DataLoader.java`  
**合并前须请谁 Review：** A（表结构）、C（界面字段）、D（可测性）

---

## 1. 你在项目中的位置

```text
web (C)  →  只依赖 dto + service.*
                ↓
[ 你 ]  Service 编排业务规则，调用 Repository，返回 DTO
                ↓
         repository + model (A)
```

你是**业务规则的唯一归属地**：7 日订票、计价、防重复占座、取消 50%、释放座位等，都应落在 Service 而非 View 或 Repository。

---

## 2. 核心交付清单

| 编号 | 工作项 | 主要类 |
| --- | --- | --- |
| B1 | 影片列表 / 场次搜索 | `FilmListingService` |
| B2 | 订票 | `BookingService` |
| B3 | 取消 | `CancellationService` |
| B4 | DTO 层 | `com.hcbs.dto` 下全部 record |
| B5 | 启动灌数入口 | `DataLoader` → 调用 `HcbsTestDataSeeder` |
| B6 | 为 C 提供稳定 API | 见 §5 各 View 对照表 |
| B7 | 封面路径与校验 | `FilmCatalogService`、`PosterResourceService`、`FilmPosterCatalog`（种子路径常量） |

---

## 3. 三个核心 Service（必须精通）

### 3.1 `FilmListingService` — 列表与筛选

| 方法 | 作用 |
| --- | --- |
| `listCities()` / `listCinemas(cityId)` | 筛选下拉数据 → `CityOption`、`CinemaOption` |
| `search(ShowingListingFilter)` | 调用 A 的 `ShowingRepository.searchShowings` |
| `searchUpcoming(filter)` | 无日期时只保留今天及以后场次（主页用） |
| `searchShowings(...)` | 组装 `ShowingRow`（含简介、演员、余座） |
| `countAvailableSeatsAcross(rows)` | 主页统计卡片 |

**`ShowingRow` 字段来源：** 场次 + 影片 + 影院 + 银幕 + `FilmActorRepository` 演员名 + `BookingSeatRepository.countActiveReservationsForShowing` 算余座。

**测试：** `FilmListingServiceTest`（TC_001、TC_002）。

---

### 3.2 `BookingService` — 订票

| 方法 | 作用 |
| --- | --- |
| `listBookableShowings()` | 今天起 **7 日内** 场次 → `ShowingOption` |
| `listAvailableSeats(showingId, seatArea)` | 过滤已占座，带单价 → `SeatOption` |
| `findBookingContext` / `loadSeatMap` | 座位图数据 → `BookingShowingContext`、`SeatMapSeat` |
| `searchCustomerPhones` | 员工代订：按手机号前缀搜客户 |
| `createBooking(...)` | 生成 `HCBS-` 参考号、写 `Booking` + `BookingSeat` |

**业务规则（答辩对照 [README_CN.md](../README_CN.md)）：**

| 规则 | 实现要点 |
| --- | --- |
| 7 日窗口 | `listBookableShowings`、`isBookableShowing`：今天 ≤ 放映日 ≤ 今天+7 |
| 过期不可订 | 放映日早于今天则拒绝 |
| 防重复占座 | 订票前调 `existsActiveReservationForShowingAndSeat` |
| 计价 | `PriceRuleRepository` + 上厅 +£2 |
| 参考号 | `HCBS-` + UUID 片段，唯一 |
| 权限 | 客户自助 vs 员工代订：`CurrentUserService`、`UserRole` |

**测试：** `BookingServiceTest`（TC_003–TC_007）。

---

### 3.3 `CancellationService` — 取消

| 方法 | 作用 |
| --- | --- |
| `findBookingSummary(reference)` | → `BookingSummary` |
| `canCancel(booking)` | 仅 `CONFIRMED` 且 **今天早于放映日** |
| `calculateCancellationCharge` | 总价 × 50%，两位小数 |
| `cancelBooking` | 改状态、记取消费、**删除** `BookingSeat` 释放座位 |
| `listAccessibleBookings` / 按手机号查 | 客户只看自己的；员工可看全部 |

**测试：** `CancellationServiceTest`（TC_008–TC_010）。

---

## 4. DTO 清单（与界面对照）

DTO 均为 **Java record**，只承载数据，无业务逻辑。

### 4.1 列表 / 主页

| DTO | 用途 | 主要消费者 |
| --- | --- | --- |
| `CityOption` / `CinemaOption` | 筛选 | `AdditiveShowingFilterPanel`、`FilmRecommendView` |
| `ShowingListingFilter` | 筛选条件封装 | `FilmListingService`、`HcbsSearchService` |
| `ShowingRow` | 场次表格一行 | 主页、`FilmListingService` |
| `ShowingListingResult` | 搜索结果包装 | 搜索服务 |
| `FilmCardDto` / `FilmDetailDto` | 海报墙、详情页 | `FilmRecommendView`、`FilmDetailView` |
| `FilmCatalogFilter` | 影片目录筛选 | `FilmCatalogService` |

### 4.2 订票 / 取消

| DTO | 用途 |
| --- | --- |
| `ShowingOption` | 订票页场次下拉 |
| `SeatOption` / `SeatMapSeat` | 座位列表与座位图 |
| `BookingShowingContext` | 当前场次上下文（片名、影院等） |
| `BookingReceipt` | 收据全文（`toReceiptText()`） |
| `BookingSummary` | 取消页订单摘要 |
| `CustomerBookingRow` | 「我的订单」表格行 |
| `PhoneSearchOption` | 代订时客户手机搜索 |

### 4.3 其他

| DTO | 说明 |
| --- | --- |
| `RegistrationRequest` | 注册表单 → `RegistrationService` |
| `UserOption` / `FilmAdminRow` | 管理页（扩展） |
| `ShowingTimeSlot` | 详情页场次时段 |

**原则：** 新增界面字段时，先加/改 DTO，再在 Service 中赋值；不要让 C 直接拿 `Film`、`Showing` 实体。

### 4.4 影片封面

| 类 / 字段 | 作用 |
| --- | --- |
| `FilmCardDto.posterUrl` / `FilmDetailDto.posterUrl` | 原样透出数据库中的路径（可为 `null`） |
| `FilmCatalogService.posterUrlFromDb` | **不做** fallback，不生成占位图 |
| `PosterResourceService.isAvailable` | 检查 `META-INF/resources` 下文件是否存在 |
| `PosterResourceService.requireLocalPath` | 管理端保存时要求路径以 `/images/posters/` 开头 |
| `FilmPosterCatalog` | 种子数据用的路径常量（`config` 包，与 `HcbsTestDataSeeder` 协作） |
| `AdminCatalogService.saveFilm` | 写入 `posterUrl` 前校验本地路径格式 |

**分工：** B 负责路径规则与 DTO；C 的 `FilmPoster` 组件根据 `isAvailable` 决定显示图片或「缺失」；A 维护 `Film.posterUrl` 字段。

---

## 5. Web 层对你的依赖（帮 C 联调）

| 路由 | View | 你提供的 Service / DTO |
| --- | --- | --- |
| `/` | `FilmRecommendView` | `HcbsSearchService`、`FilmCatalogService`（扩展，见 §7） |
| `/film/{id}` | `FilmDetailView` | `FilmCatalogService` → `FilmDetailDto` |
| `/booking` | `BookingView` | **`BookingService`**、`BookingReceipt` |
| `/cancellation` | `CancellationView` | **`CancellationService`**、`BookingSummary` |
| `/my-bookings` | `MyBookingsView` | `CancellationService.listAccessibleBookings` |
| `/register` | `RegisterView` | `RegistrationService`（扩展） |
| `/admin` | `AdminDataView` | `AdminCatalogService`（扩展） |

课程核心三页：**列表（主页）→ 订票 → 取消**；答辩时重点讲这三个 Service 的规则即可。

---

## 6. `DataLoader` 与种子数据

```java
// DataLoader.java — 你维护的入口
if (cityRepository.count() > 0) return;
testDataSeeder.seedAll();
```

- **你只负责**「何时触发灌数」；具体种子内容在 `HcbsTestDataSeeder`、`HcbsMediaCatalog`（`config` 包，与 A/D 协作）。  
- 改价目、场次边界日期时，确认 A 的实体与 `PriceRule` 仍一致。  
- **测试：** `DataLoaderTest`（D 维护，你需保证种子与业务规则一致）。

---

## 7. 项目扩展模块（非矩阵原文，但代码已存在）

以下类在仓库中已使用，**合并前建议你评审接口**，维护人可由组内再指定：

| 包 / 类 | 作用 |
| --- | --- |
| `service.search.HcbsSearchService` | 主页叠加筛选、统计 |
| `service.catalog.FilmCatalogService` | 影片详情、推荐墙 |
| `service.admin.AdminCatalogService` | 管理页：影片、场次、用户 |
| `service.auth.RegistrationService` | 客户注册 |
| `security.*` | 登录用户、`CurrentUserService` |

若答辩只覆盖课程三功能，**不必**展开 admin/注册细节，但应说明它们同样遵守「Service → DTO → View」。

---

## 8. 协作边界

| 同事 | 协作内容 |
| --- | --- |
| **A** | 改 `searchShowings`、占座查询前与你对齐；新增实体字段你补 DTO |
| **C** | 只注入 Service；异常用 `Notification` 展示你的 `IllegalArgumentException` 文案 |
| **D** | 每条 TC 应对到具体测试方法；你改规则须同步 D 更新用例表 |

**禁止：** 在 `web` 写计价、7 日判断、取消可否；在 `repository` 写业务分支。

---

## 9. 本地开发与调试

```powershell
# 只跑与你相关的测试
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=BookingServiceTest,CancellationServiceTest,FilmListingServiceTest,DataLoaderTest
```

**调试技巧：**

- 伦敦晚场下厅 £12、两席 £24 — 见 `BookingServiceTest` 中断言。  
- 重复订座：对同一场次 `CONFIRMED` 座位再订应失败。  
- 取消：放映日当天 `canCancel` 为 false。

---

## 10. 答辩 3–5 分钟提纲

1. 三层分工：View 只调 Service，Service 只返回 DTO。  
2. **三个 Service 各管什么**（列表 / 订 / 取消）。  
3. 选 **一条** 规则讲透（建议：7 日窗口 + 防重复占座，或 50% 取消费 + 删 `BookingSeat`）。  
4. 打开 `BookingService.createBooking` 或 `CancellationService.cancelBooking` 指关键行。  
5. DTO 举例：`BookingReceipt` 为何不让 View 拼字符串（可选）。

---

## 11. 相关文档

| 文档 | 用途 |
| --- | --- |
| [TEST_CASES.md](../TEST_CASES.md) | TC_003–TC_010 与测试类对应 |
| [MEMBER_A_持久化与数据.md](MEMBER_A_持久化与数据.md) | Repository 查询 |
| [MEMBER_C_Web界面.md](MEMBER_C_Web界面.md) | 谁调用你的 API |
| [MEMBER_D_测试与交付.md](MEMBER_D_测试与交付.md) | 测试与打包 |

---

## 12. 自检清单

- [ ] 能不看稿说出 TC_003–TC_010 各对应哪个 Service 方法  
- [ ] 新增/修改 DTO 后 C 的 View 已编译通过  
- [ ] 业务规则未泄漏到 `web` 或 `repository`  
- [ ] `mvn test` 中 Booking / Cancellation / Listing / DataLoader 全绿  
- [ ] 改种子或价目后与 A 确认实体约束
