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
| 银幕容量 | 每块银幕 **100** 座（10×10 网格，列布局 3+过道+4+过道+3） |
| 座位编号 | `R01C01` … `R10C10`（行 1 靠近银幕），区域 `STANDARD`（无上下层） |
| 票价表（城×时段） | 12 条 `PriceRule`（4 城 × 3 时段 × 1 区域） |
| 最多提前 7 天订票 | 含 `today+7` 场次；`today+8` 用于拒绝测试 |
| 至少提前 1 天取消、50% 手续费 | 种子订单 `HCBS-SEED001`（明天场次，London 下厅 £12） |
| 放映日当天不可取消 | 含 `today` Birmingham 家庭片晚场 |
| 演示账户 | 三类角色各 3 个，共 9 个；密码均为 `demo`（见 `DemoAccountCatalog`） |
| 客户自助注册 | 通过 `/register` 创建 `CUSTOMER` 账户（邮箱、用户名唯一） |
| 用户角色 | `CUSTOMER`（客户）、`BOOKING_STAFF`（订票员）、`ADMIN`（管理员）；与程序设计一致，未单独保留案例中的 Manager 角色 |

---

## 2. 数据规模（约）

| 实体 | 数量 |
| --- | --- |
| City | 4 |
| Cinema | 8 |
| Screen | 24（4 城 × 旗舰店 4 + 分店 2） |
| Seat | 2,400（24 银幕 × 100 座） |
| Film | 12（5 部核心 + 7 部扩展，见 `HcbsMediaCatalog`） |
| Actor | 12 |
| FilmActor | 22+ |
| PriceRule | 12 |
| Showing | 42（18 核心测试场次 + 24 扩展场次） |
| User | 9（种子，三类角色各 3）+ 注册新增 |
| Booking（种子） | 2（`HCBS-SEED001`、`HCBS-SEED002`） |
| BookingSeat（种子） | 3 |

---

## 3. 票价（标准座 / £）

| 城市 | 早场 Morning | 午场 Afternoon | 晚场 Evening |
| --- | ---: | ---: | ---: |
| Birmingham | 5 | 6 | 7 |
| Bristol | 6 | 7 | 8 |
| Cardiff | 5 | 6 | 7 |
| London | 10 | 11 | 12 |

---

## 3b. 座位网格

- 每块银幕 10 行 × 10 列，编号 `R{行}C{列}`（如 `R05C08`）。
- 列 1–3、过道、列 4–7、过道、列 8–10（订票页按此排版）。
- 种子订单：`HCBS-SEED001` 使用 `R05C05`；`HCBS-SEED002` 使用 `R03C02`、`R03C03`。

---

## 4. 场次目录（相对 `LocalDate.now()`，按插入顺序）

| # | 偏移 | 城市 / 影院 | 影片 | 时段 | 测试用途 |
| ---: | --- | --- | --- | --- | --- |
| 1 | **+3** | London Central · Screen 1 | Spirited Away | Evening | **锚点场次**：`findAll()[0]`，标准座 £12，`BookingServiceTest` |
| 2 | +3 | London Central · Screen 2 | The Legend of 1900 | Morning | 同城多场次 |
| 3 | +3 | London East | Spirited Away | Afternoon | 二级影院筛选 |
| 4 | +3 | London Central · Screen 3 | Solitude | Morning | 纪录片早场 |
| 5 | +4 | Birmingham Bullring | The Wasted Times | Afternoon | 伯明翰筛选 |
| 6 | +3 | Birmingham New Street | Harry Potter and the Order of the Phoenix | Morning | 二级影院 |
| 7 | +2 | Birmingham Bullring | Spirited Away | Evening | 日期筛选 |
| 8 | +5 | Bristol Harbour | Harry Potter and the Order of the Phoenix | Evening | 布里斯托尔 |
| 9 | +3 | Bristol Clifton | The Legend of 1900 | Morning | 二级影院 |
| 10 | +3 | Bristol Harbour | The Wasted Times | Afternoon | 片名 / 城市筛选 |
| 11 | +2 | Cardiff Bay | Solitude | Morning | 加的夫 day+2 |
| 12 | +4 | Cardiff Bay | The Legend of 1900 | Evening | 加的夫旗舰 |
| 13 | +6 | Cardiff Central | Spirited Away | Evening | 二级影院 |
| 14 | **+7** | London Central | Spirited Away | Evening | 订票上限（允许） |
| 15 | **+8** | London Central | The Legend of 1900 | Evening | 超过 7 日（应拒绝） |
| 16 | **+1** | London Central | The Wasted Times | Evening | 种子订单 + 可取消 |
| 17 | **0** | Birmingham Bullring | Harry Potter and the Order of the Phoenix | Evening | 当日取消应拒绝 |
| 18 | **-1** | Cardiff Bay | Solitude | Afternoon | 过期场次，不可订 |

---

## 5. 预置订单（手工取消测试）

| 字段 | 值 |
| --- | --- |
| 参考号 | `HCBS-SEED001` |
| 场次 | London Central · Screen 2 · The Wasted Times · **today + 1** · Evening |
| 客户 | `alice`（`staff` 代订） |
| 座位 | 下厅 `L1` |
| 总价 | £12.00（由票价规则自动计算） |
| 状态 | `CONFIRMED` |

| 参考号 | `HCBS-SEED002` |
| 场次 | Cardiff Bay · The Legend of 1900 · **today + 4～5** · Evening |
| 客户 | `bob`（自助订票） |
| 座位 | 下厅 2 张 |
| 状态 | `CONFIRMED` |

在 **Cancellation** 页输入 `HCBS-SEED001` 可验证查询与取消（今日取消成功，手续费 £6）。客户 `bob` 登录后可在 **My bookings** 看到 `HCBS-SEED002`。

### 扩展影片（`HcbsMediaCatalog`）

| 键 | 片名 | 类型 | 分级 |
| --- | --- | --- | --- |
| Midnight | Call Me by Your Name | Thriller | 15 |
| Velvet | Maleficent | Romance | 12A |
| Iron | Inception | Action | 12A |
| Lantern | The Cabin in the Woods | Animation | U |
| Quiet | Rock Star | Horror | 15 |
| North | Reign Over Me | Adventure | 12A |
| Paper | Cirque du Soleil: O | Comedy | 12 |

---

## 6. 影院与银幕布局

| 类型 | 影院键 | 银幕数 | 容量 |
| --- | --- | ---: | --- |
| 旗舰店 | London-Central, Birmingham-Bullring, Bristol-Harbour, Cardiff-Bay | 4 | 各 100 座 |
| 分店 | London-East, Birmingham-NewStreet, Bristol-Clifton, Cardiff-Central | 2 | 各 100 座 |

---

## 7. 版本库与隐私

`./data/` 及 `*.mv.db`、`*.trace.db` 已写入 `.gitignore`，**不应**提交到远程仓库。种子数据由 `HcbsTestDataSeeder` 在本地自动生成，组员各自运行应用即可得到相同测试库。

---

## 8. 重置与访问 H2

> **开发备忘（非正式版）：** 若因座位格式、枚举、种子或 OneDrive 同步导致 H2 锁库、90020、旧数据结构与当前代码不一致等异常，**先删 `./data/` 下库文件再启动，不要改代码去迁就旧库。** 详见 [DEV_TROUBLESHOOTING.md](DEV_TROUBLESHOOTING.md)。

**清空并重建（推荐开发时）：**

1. 停止应用（并确认无多余 `java` 进程）  
2. 删除 `./data/hcbs.lock.db`、`./data/hcbs.mv.db`（及 `.trace.db` 如有）；或直接删除整个 `./data/`  
3. 重新 `spring-boot:run`（`ddl-auto=create` 会建表，`HcbsTestDataSeeder` 会按当前代码灌数）

**H2 控制台：** `http://localhost:8080/h2-console`  
- JDBC URL: `jdbc:h2:file:./data/hcbs`  
- 用户名: `sa`，密码留空  

**单元测试：** 使用内存库 `jdbc:h2:mem:...`，每测方法独立，同样走 `HcbsTestDataSeeder`。

---

## 9. 代码入口

| 类 / 常量 | 作用 |
| --- | --- |
| `HcbsTestDataSeeder` | 全部种子逻辑 |
| `HcbsTestDataSeeder.ANCHOR_SHOWING_DAY_OFFSET` | 锚点场次日期偏移（默认 3） |
| `HcbsTestDataSeeder.SEED_BOOKING_REFERENCE` | 种子订单参考号 |
| `DataLoader` | 启动时若库为空则调用 seeder |

修改测试数据时请只改 `HcbsTestDataSeeder`，并同步更新本文件与 `TEST_CASES.md`。

---

## 10. `app_user` 表（账户）

| 列 | 约束 | 说明 |
| --- | --- | --- |
| `username` | 唯一、非空 | 登录名，3–32 字符 |
| `email` | 唯一、非空 | 注册邮箱，保存为小写 |
| `password_hash` | 非空 | BCrypt 哈希 |
| `full_name` | 非空 | 显示名称 |
| `phone` | 可空 | 联系电话 |
| `role` | 非空 | `CUSTOMER` / `BOOKING_STAFF` / `ADMIN` |
| `status` | 非空 | `ACTIVE` / `DISABLED` |
| `created_at` / `updated_at` | 非空 / 可空 | 注册与最近更新时间 |

`booking` 表通过 `customer_user_id`、`created_by_user_id` 外键关联客户与操作人，并建有查询索引。

**演示账号一览（密码均为 `demo`）：**

| 类型 | 用户名 | 显示名 |
| --- | --- | --- |
| 客户 | `alice` | Alice Chen |
| 客户 | `bob` | Bob Walker |
| 客户 | `carol` | Carol Murphy |
| 订票员 | `staff` | Sam Staff |
| 订票员 | `desk01` | Emma Desk |
| 订票员 | `desk02` | Liam Desk |
| 管理员 | `admin` | Ava Admin |
| 管理员 | `admin01` | Olivia Admin |
| 管理员 | `admin02` | Noah Admin |

定义见 `com.hcbs.config.DemoAccountCatalog`；登录页 `/login` 按角色分组显示手机号（密码均为 `demo`）。
