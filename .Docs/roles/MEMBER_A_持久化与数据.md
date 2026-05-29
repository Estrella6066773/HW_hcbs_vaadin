# 成员 A — 持久化与数据设计

**角色：** 持久化与数据设计  
**组内目标：** 约 25%（Exercise 1 组内占比最高，约 40%）  
**负责包：** `com.hcbs.model.*`、`com.hcbs.repository.*`  
**合并前须请谁 Review：** B（服务接口）、D（测试数据与用例）

---

## 1. 你在项目中的位置

```text
[ 你 ] model + repository
         ↑ 被 service 调用
         ↓ 映射 H2 数据库表
```

界面层**不得**直接使用实体或 Repository；所有查询经 Service 暴露为 DTO。你定义「数据长什么样、怎么查」，B 定义「业务怎么用这些数据」。

---

## 2. 核心交付清单

| 编号 | 工作项 | 状态 | 主要文件 |
| --- | --- | --- | --- |
| A1 | 需求分析与 ERD | ☑ | `.Docs/req/`（若有 PDF）、答辩材料 |
| A2 | 逻辑数据库模式 | ☑ | 各 `@Entity` 的 `@Table`、字段、索引 |
| A3 | 设计说明（规范化、票价存储等） | ☑ | 见下文 §4 |
| A4 | JPA 实体 | ☑ | `src/main/java/com/hcbs/model/` |
| A5 | Repository 与关键查询 | ☑ | `src/main/java/com/hcbs/repository/` |
| A6 | 与 B 协作：占座释放、DTO 字段 | ☑ | `BookingSeatRepository` |

---

## 3. 实体一览（必须能讲清关系）

### 3.1 地理与影院

| 实体 | 表名 | 要点 |
| --- | --- | --- |
| `City` | `city` | 四城：London、Birmingham、Bristol、Cardiff |
| `Cinema` | `cinema` | 多对一 `City`；每城 2 家影院 |
| `Screen` | `screen` | 多对一 `Cinema`；含座位容量等 |
| `Seat` | `seat` | 多对一 `Screen`；`SeatArea`：下厅 STANDARD / 上厅 UPPER |

### 3.2 影片与场次

| 实体 | 要点 |
| --- | --- |
| `Film` | 片名、简介、时长、评分等；**`posterUrl`** 存本地封面路径（如 `/images/posters/spirited-away.jpg`，可空） |
| `Actor` | 演员 |
| `FilmActor` | 影片–演员多对多关联表 |
| `Showing` | 场次：`Film` + `Screen` + 日期时间；`TimeBand`（早/午/晚）；`ShowingStatus` |
| | 唯一约束：同银幕、同日期、同开始时间不可重复 |

### 3.3 票价与订单

| 实体 | 要点 |
| --- | --- |
| `PriceRule` | 按 `City` + `TimeBand` + `SeatArea` 定价；唯一约束防重复价目 |
| `Booking` | 参考号 `bookingReference`（唯一）；关联 `Showing`、客户/代订人；`BookingStatus` |
| `BookingSeat` | 订单–场次–座位；取消时由 B 的服务删除行以释放座位 |

### 3.4 用户（扩展，与登录相关）

| 实体 | 要点 |
| --- | --- |
| `User` | 用户名、邮箱、手机、角色 `UserRole`、状态 `UserStatus` |
| 枚举 | `UserRole`（客户/员工/管理员等）、`UserStatus`、`BookingStatus`、`ShowingStatus`、`TimeBand`、`SeatArea` |

**答辩必讲：** 一张简化 ERD（City → Cinema → Screen → Seat；Film → Showing ← Screen；Booking → BookingSeat → Seat）。

---

## 4. 设计要点（写进说明 / 答辩用）

1. **票价存在 `PriceRule` 表**，不按场次写死价格；B 的 `BookingService` 按城市、时段、座位区域查价。上厅在同城同时段下厅价基础上 +£2（业务在 Service 层实现，表存基础价）。  
2. **占座以 `BookingSeat` + `Booking.status = CONFIRMED` 为准**；取消后删除 `BookingSeat` 行，座位才可再订。  
3. **场次唯一性** 由 `Showing` 表级唯一约束保证，避免同一银幕重复排片。  
4. **索引：** `Booking` 上对 `customer_user_id`、`created_by_user_id`、`showing_showing_id` 建索引，便于按客户、场次查订单。  
5. **封面路径：** `Film.posterUrl` 只存应用内路径字符串，**不存图片二进制**；实际文件在 `src/main/resources/META-INF/resources/images/posters/`。路径是否有效由 B/C 在 Service 与界面层判断，你只需保证字段长度与可空性合理。

---

## 5. Repository 清单（你的核心 API）

| 类 | 职责 | 重点方法 |
| --- | --- | --- |
| `CityRepository` | 城市 | `findAll` |
| `CinemaRepository` | 影院 | `findByCity` |
| `ScreenRepository` | 银幕 | 按影院查银幕 |
| `SeatRepository` | 座位 | `findByScreenAndSeatArea` |
| `FilmRepository` | 影片 | 常规 CRUD |
| `ActorRepository` / `FilmActorRepository` | 演员与卡司 | 列表页演员名 |
| `ShowingRepository` | 场次 | **`searchShowings`**（列表筛选） |
| `PriceRuleRepository` | 价目 | 按城市、时段、区域查价 |
| `BookingRepository` | 订单 | 按客户、参考号、手机号等 |
| `BookingSeatRepository` | 订座明细 | **`existsActiveReservationForShowingAndSeat`**、**`countActiveReservationsForShowing`** |

### 5.1 你必须能逐行讲解的查询

**场次搜索** — `ShowingRepository.searchShowings`：

- 可选筛选：城市 ID、影院 ID、日期、片名（模糊，忽略大小写）。  
- 排序：日期、开始时间升序。  
- 被 `FilmListingService` 调用，对应测试 TC_002。

**活跃占座** — `BookingSeatRepository.existsActiveReservationForShowingAndSeat`：

- 仅当关联 `Booking.status == CONFIRMED` 时才算占用。  
- 被 `BookingService` 用于防重复订座，对应 TC_005。

**余座统计** — `countActiveReservationsForShowing`：

- 列表页「可用座位数」= 银幕总座 − 已确认占座数（在 Service 中组装为 `ShowingRow`）。

---

## 6. 日常开发：改什么、怎么验

### 6.1 常见修改场景

| 场景 | 你可能改 | 须通知 |
| --- | --- | --- |
| 新增字段（如影片分级） | `Film` + 迁移/删库说明 | B（DTO）、C（表格列）、D（用例） |
| 调整搜索条件 | `ShowingRepository` JPQL | B `FilmListingService`、D TC_002 |
| 占座规则变更 | `BookingSeatRepository` 查询 | B 订票/取消服务、D TC_005/008 |
| 新表/新实体 | `model` + `repository` | 全组架构评审 |

### 6.2 本地验证

```powershell
# 改实体后建议全量测试（尤其 DataLoader、Listing、Booking）
mvn "-Dmaven.repo.local=.m2/repository" clean test
```

- 种子数据由 `HcbsTestDataSeeder` 写入（`config` 包，你与 B/D 协作）；**改实体后常需删库**：停止应用 → 删除 `./data/hcbs.lock.db` 与 `./data/hcbs.mv.db` → 重启。见 [TEST_DATABASE.md](../TEST_DATABASE.md)、[DEV_TROUBLESHOOTING.md](../DEV_TROUBLESHOOTING.md)。  
- H2 控制台：`http://localhost:8080/h2-console`（启动横幅中的端口为准）。

### 6.3 禁止事项

- 不要在 `web` 或 `service` 包里直接改别人的业务逻辑来「绕过」模型问题。  
- 不要让界面层 `import com.hcbs.repository`。

---

## 7. 协作边界

| 同事 | 与你相关的接口 |
| --- | --- |
| **B** | 需要稳定实体关系与 Repository 方法；DTO 字段名需与你实体语义一致 |
| **C** | 不碰 Repository；若表格缺列，由 B 加 DTO 字段 |
| **D** | 依赖种子数据与 TC_005/008 等；你改查询后请同步说明预期结果 |

**不属于你单独维护（了解即可）：** `com.hcbs.config` 种子类、`com.hcbs.security`、扩展 Service（`catalog`、`admin`、`auth`）。若种子插入违反约束，你会是第一个被找的人。

---

## 8. 答辩 3–5 分钟提纲

1. **ERD**：主要实体与基数（1:N、M:N）。  
2. **`Showing` 唯一约束** 为何这样设计。  
3. **`searchShowings`**：四个可选参数与排序。  
4. **占座查询**：为何只认 `CONFIRMED`；取消后如何释放（`BookingSeat` 删除）。  
5. **演示**：H2 中打开 `showing`、`booking_seat` 表各一条样例（可选）。

---

## 9. 相关文档

| 文档 | 用途 |
| --- | --- |
| [ARCHITECTURE.md](../ARCHITECTURE.md) | 分层与依赖规则 |
| [TEST_DATABASE.md](../TEST_DATABASE.md) | 种子场景、边界日期场次 |
| [README_CN.md](../README_CN.md) §业务规则 | 7 日订票、50% 取消费（实现多在 B，数据支撑在你） |
| [MEMBER_B_应用服务.md](MEMBER_B_应用服务.md) | 谁调用你的 Repository |

---

## 10. 自检清单（提交 / 答辩前）

- [ ] 能不看稿画出核心 ERD  
- [ ] 能说明 `PriceRule` 唯一约束的三列含义  
- [ ] 能口述 `searchShowings` 与 `existsActiveReservation...` 的 JPQL 意图  
- [ ] 改实体后已通知 B/D，且 `mvn test` 通过  
- [ ] 未在 `web` 层引入 Repository 依赖
