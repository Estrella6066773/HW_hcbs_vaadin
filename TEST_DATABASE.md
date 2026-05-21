# HCBS 测试数据库说明

应用使用 **H2**（默认文件库 `./data/hcbs`，见 `application.properties`）。启动时若库为空，由 `HcbsTestDataSeeder` 写入全套测试数据。

设计目标：同时满足 **案例业务规则** 与 **TEST_CASES.md / 单元测试** 所需场景。

---

## 1. 与案例要求的对应

| 案例要求 | 测试库实现 |
| --- | --- |
| Birmingham、Bristol、Cardiff、London 四城 | 4 条 `City` |
| 每城至少 2 家影院 | 每城 2 条 `Cinema`，共 8 家 |
| 每家影院最多 6 块银幕 | 旗舰店 4 块 + 分店 2 块（主店 ≤6） |
| 银幕容量 50–120 | 旗舰店容量 50 / 80 / 100 / 120；分店各 2×50 |
| 下厅与上厅 | 每块银幕座位对半：`L*` 下厅、`U*` 上厅 |
| 下厅票价表（城×时段） | 24 条 `PriceRule`（4 城 × 3 时段 × 2 区域）；上厅 = 下厅 + £2 |
| 最多提前 7 天订票 | 含 `today+7` 场次；`today+8` 用于拒绝测试 |
| 至少提前 1 天取消、50% 手续费 | 种子订单 `HCBS-SEED001`（明天场次，总价 £12） |
| 放映日当天不可取消 | 含 `today` 场次 |
| 三类用户 | `staff` / `admin` / `manager` |

---

## 2. 数据规模（约）

| 实体 | 数量 |
| --- | --- |
| City | 4 |
| Cinema | 8 |
| Screen | 24（4 城 × 旗舰店 4 + 分店 2） |
| Seat | 1,700（随容量变化） |
| Film | 5 |
| Actor | 4 |
| FilmActor | 8 |
| PriceRule | 24 |
| Showing | 12+（含边界日期） |
| User | 3 |
| Booking（种子） | 1（`HCBS-SEED001`） |
| BookingSeat（种子） | 1 |

---

## 3. 票价（下厅 / £）

| 城市 | 早场 Morning | 午场 Afternoon | 晚场 Evening |
| --- | ---: | ---: | ---: |
| Birmingham | 5 | 6 | 7 |
| Bristol | 6 | 7 | 8 |
| Cardiff | 5 | 6 | 7 |
| London | 10 | 11 | 12 |

上厅各档 +£2（如 London 晚场上厅 £14）。

---

## 4. 场次与测试场景（相对 `LocalDate.now()`）

| 偏移天数 | 示例用途 | 示例 |
| --- | --- | --- |
| **-1** | 不能预订已过期场次 | Cardiff 纪录片 午场 |
| **0** | 当日取消应拒绝 | Birmingham 家庭片 晚场 |
| **+1** | 可取消；种子订单放映日 | London Central 剧情片 晚场 + `HCBS-SEED001` |
| **+2** | 列表筛选 | Cardiff 纪录片 早场 |
| **+3** | 主测试锚点；London 晚场 £12 | London Central Screen1《Skyline Run》18:30 |
| **+4** | 多城筛选 | Birmingham 午场 |
| **+7** | 订票上限（允许） | London 晚场 |
| **+8** | 超过 7 日（应拒绝） | London 科幻 晚场 |

**自动化测试锚点：** `ShowingRepository.findAll()` 按主键升序时，第一条为 **London Central、today+3、晚场、下厅 £12**，供 `BookingServiceTest` 使用。

---

## 5. 预置订单（手工取消测试）

| 字段 | 值 |
| --- | --- |
| 参考号 | `HCBS-SEED001` |
| 场次 | London Central，放映日 = **today + 1** |
| 座位 | 下厅 `L1` |
| 总价 | £12.00 |
| 状态 | `CONFIRMED` |

在 **Cancellation** 页输入 `HCBS-SEED001` 可验证查询与取消（今日取消应成功并扣 £6 手续费）。

---

## 6. 影院与银幕布局

| 类型 | 影院键 | 银幕数 | 容量 |
| --- | --- | ---: | --- |
| 旗舰店 | London-Central, Birmingham-Bullring, Bristol-Harbour, Cardiff-Bay | 4 | 50, 80, 100, 120 |
| 分店 | London-East, Birmingham-NewStreet, Bristol-Clifton, Cardiff-Central | 2 | 50, 50 |

---

## 7. 重置与访问 H2

**清空并重建（推荐开发时）：**

1. 停止应用  
2. 删除 `./data/hcbs.mv.db`（及同目录 `.trace.db` 如有）  
3. 重新 `spring-boot:run`（`ddl-auto=create` 会建表，`DataLoader` 会灌数）

**H2 控制台：** `http://localhost:8080/h2-console`  
- JDBC URL: `jdbc:h2:file:./data/hcbs`  
- 用户名: `sa`，密码留空  

**单元测试：** 使用内存库 `jdbc:h2:mem:...`，每测方法独立，同样走 `HcbsTestDataSeeder`。

---

## 8. 代码入口

| 类 | 作用 |
| --- | --- |
| `HcbsTestDataSeeder` | 全部种子逻辑 |
| `DataLoader` | 启动时若库为空则调用 seeder |
| `HcbsTestDataSeeder.SEED_BOOKING_REFERENCE` | 种子订单参考号常量 |

修改测试数据时请只改 `HcbsTestDataSeeder`，并同步更新本文件与 `TEST_CASES.md`。
