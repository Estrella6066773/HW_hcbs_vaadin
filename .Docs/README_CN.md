# Horizon Cinemas 影院订票系统（HCBS）

**模块：** IN3338 面向对象开发  
**院校：** UWE Bristol  
**学年：** 2025–2026  

英文说明见 [README.md](../README.md)。

Horizon Cinemas 影院订票系统面向影院柜台人员，用于查询放映场次、完成订票与办理取消。本系统实现 HCBS 案例要求的**影片列表**、**订票**、**取消订票**三项核心功能。

> 小组成员、贡献比例与答辩安排见 **[CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md)**（同目录）。本文档仅介绍**系统功能**与**运行方式**。

---

## 目录

- [功能概览](#功能概览)
- [使用流程](#使用流程)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [系统架构](#系统架构)
- [环境要求](#环境要求)
- [如何运行](#如何运行)
- [如何测试](#如何测试)
- [业务规则](#业务规则)
- [演示数据](#演示数据)
- [相关文档](#相关文档)

---

## 功能概览

| 功能 | 路由 | 说明 |
| --- | --- | --- |
| **主页** | `/` | 默认海报宣传墙；搜索后显示统计卡片与场次表格（城市、影院、日期、片名可叠加）。 |
| **影片详情** | `/film/{id}` | 单部影片的简介、演员、评分与即将上映场次。 |
| **订票** | `/booking` | 在允许的时间范围内选择场次与座位区域（下厅 / 上厅），选定空闲座位后确认，生成带唯一参考号的收据。 |
| **取消订票** | `/cancellation` | 员工按参考号处理任意客户订单的取消。 |
| **我的订单** | `/my-bookings` | 客户查看并取消自己的订单。 |
| **登录** | `/login` | 手机号 + 密码登录；页内列出三类演示账号。 |
| **注册** | `/register` | 客户自助注册（用户名、邮箱、密码等）。 |
| **数据管理** | `/admin` | 管理员维护影片排期与账户状态（仅 `ADMIN` 角色）。 |

**未实现**（作业范围外）：支付、邮件验证、密码找回。

---

## 使用流程

### 影片列表

1. 打开首页。
2. 可选：设置城市、影院、日期、片名筛选条件。
3. 点击 **Search** 刷新表格。
4. 查看场次与余座后，再进入订票。

### 订票

1. 侧栏进入 **Booking**。
2. 在下拉框选择场次（仅列出今日起 7 日内的场次）。
3. 选择 **Seat area**（下厅或上厅）。
4. 勾选一个或多个可用座位。
5. 点击 **Confirm booking**，在收据区查看参考号、影片、日期时间、银幕、座位、总价与订票时间。

### 取消订票

1. 侧栏进入 **Cancellation**。
2. 输入订票参考号，点击 **Find booking**。
3. 查看是否允许取消及 50% 手续费说明。
4. 若允许，点击 **Cancel booking**；详情区会更新状态与费用。

---

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 语言 | Java 21 |
| 框架 | Spring Boot 3.2 |
| 界面 | Vaadin 24 |
| 持久化 | Spring Data JPA、H2（内存库） |
| 构建 | Maven |
| 测试 | JUnit 5、Spring Boot Test、AssertJ |

---

## 项目结构

```text
hcbs-vaadin/
├── src/main/java/com/hcbs/
│   ├── model/              # JPA 实体
│   ├── repository/         # 数据访问
│   ├── dto/                # 界面层使用的数据对象
│   ├── service/
│   │   ├── listing/        # 影片列表服务
│   │   ├── booking/        # 订票服务
│   │   └── cancellation/   # 取消服务
│   ├── web/                # Vaadin 页面与布局
│   └── config/             # 启动时种子数据
├── src/test/java/          # 自动化测试
├── frontend/themes/hcbs/   # 应用主题
├── README.md               # 功能说明（英文，仓库根目录）
└── .Docs/                  # 其余文档（本目录）
    ├── README_CN.md
    ├── ARCHITECTURE.md
    ├── CONTRIBUTION_MATRIX.md
    ├── TEST_CASES.md
    ├── TEST_DATABASE.md
    └── req/
```

---

## 系统架构

```text
web  →  service.*  →  repository  →  model
         ↑
        dto（面向界面的记录类型）
```

- **Web 层** 只调用应用服务，绑定 DTO，不直接使用 Repository 或 JPA 实体。
- **Service 层** 实现业务规则（票价、订票期限、取消政策）。
- **Repository 层** 负责持久化与查询（如场次筛选、有效占座统计）。

技术细节见 [ARCHITECTURE.md](ARCHITECTURE.md)。

---

## 环境要求

- **JDK 21**（`java -version`）
- **Maven 3.9+**（`mvn -version`）
- 现代浏览器（默认 `http://localhost:8080`；若端口被占用，以控制台启动横幅中的地址为准）

项目位于 OneDrive 时，执行 `mvn test` 前请先关闭正在运行的开发服务，避免 `frontend/generated` 与 `./data/hcbs.mv.db` 被占用。

**若启动报错 H2 锁库 / `90020` / `The file is locked`：** 当前为**开发构建**。先关闭其他运行实例，删除 `./data/hcbs.lock.db` 与 `./data/hcbs.mv.db` 后重启；**不要改代码**去兼容本机旧库。详见 [DEV_TROUBLESHOOTING.md](DEV_TROUBLESHOOTING.md)。

**版本库：** 本地运行产生的数据不会上传。`.gitignore` 已忽略 `data/`（H2 文件库）、`*.mv.db`、`target/`、`.m2/`、`frontend/generated/` 等。提交前请用 `git status` 确认未包含上述路径。

---

## 如何运行

在项目根目录执行：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

启动完成后，控制台会打印访问地址横幅，例如：

```text
============================================================
  HCBS is ready
  Web UI:     http://localhost:8080/
  H2 console: http://localhost:8080/h2-console
  Port:       8080
============================================================
```

**端口规则（应急备用）：** 优先 **8080** → 顺序尝试 **8081–8180** → 再尝试最多 **20** 个哈希端口 **8181–8280**。启动日志会打印实际访问地址；若 8080 被占用会注明 fallback。DevTools 热重启时会在绑定前重新选端口。手动指定：`-Dserver.port=9090` 或 `SERVER_PORT=9090`。

**导航：** 主页 · 订票 · 取消（侧栏）。

修改 Vaadin 依赖或主题后，需先执行：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" vaadin:prepare-frontend
```

---

## 如何测试

运行全部自动化测试：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" clean test
```

| 测试类 | 覆盖内容 |
| --- | --- |
| `BookingServiceTest` | 订票、计价、重复占座、7 日限制 |
| `CancellationServiceTest` | 取消费用、释放座位、当日不可取消 |
| `FilmListingServiceTest` | 搜索与简介字段 |
| `DataLoaderTest` | 种子数据 |
| `UiThemeTest` | `hcbs` 主题 |

手工场景见 [TEST_CASES.md](TEST_CASES.md)（TC_001–TC_011）。

打包：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" package
```

---

## 业务规则

| 规则 | 行为说明 |
| --- | --- |
| 订票参考号唯一 | 自动生成，前缀 `HCBS-` |
| 订票时间窗 | 自今日起至放映日前 7 天内 |
| 已过期场次 | 不可预订 |
| 座位占用 | 同一场次下，`CONFIRMED` 订单不得重复占用同一座位 |
| 票价 | 按城市、时段（早 / 午 / 晚）、座位区域计算 |
| 上厅加价 | 在同城市、同时段下厅价基础上 +£2 |
| 取消时限 | 仅当「今天早于放映日」时可取消 |
| 取消手续费 | 订票总价的 50% |
| 当日取消 | 拒绝并提示 |
| 取消后 | 释放座位记录，座位可再次预订 |

---

## 演示数据

首次启动时，`HcbsTestDataSeeder` 写入完整测试库（详见 [TEST_DATABASE.md](TEST_DATABASE.md)）：

| 数据 | 内容 |
| --- | --- |
| 城市 | London、Birmingham、Bristol、Cardiff（每城 2 家影院） |
| 银幕 | 旗舰店 4 块（50–120 座）；分店 2×50 座；下厅/上厅各半 |
| 影片与演员 | 5 部影片、4 名演员及关联 |
| 场次 | 含今天、明天、+3～+8 天及昨天等边界场次 |
| 票价 | 四城下厅价目表；上厅 +£2 |
| 用户 | 三类角色各 3 个演示账号（客户 / 订票员 / 管理员，共 9 个，密码 `demo`）；登录页按角色列出手机号；可通过 `/register` 新增客户 |
| 预置订单 | 参考号 `HCBS-SEED001`（归属客户 `alice`，由 `staff` 代订） |

客户自助订票记在自己名下；员工代订须选择客户。`app_user` 表含唯一用户名与邮箱、注册时间等字段。

**重置数据库：** 停止应用后删除 `./data/hcbs.lock.db` 与 `./data/hcbs.mv.db`，再重新启动即可重新灌入种子数据。座位格式或种子变更后，一律删库验证，勿改代码迁就旧数据——见 [DEV_TROUBLESHOOTING.md](DEV_TROUBLESHOOTING.md)。

---

## 相关文档

| 文档 | 用途 |
| --- | --- |
| [README.md](../README.md) | 英文功能说明（本稿中文对应版） |
| [文档索引](README.md) | 本目录文档一览 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 技术分层与依赖 |
| [TEST_CASES.md](TEST_CASES.md) | 手工测试用例表 |
| [TEST_DATABASE.md](TEST_DATABASE.md) | 测试数据库设计、种子数据场景与重置方法 |
| [DEV_TROUBLESHOOTING.md](DEV_TROUBLESHOOTING.md) | 开发环境：格式/锁库异常时删 `./data/`，勿改代码 |
| [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) | 成员贡献与答辩（不在本文档中说明） |
| [roles/README.md](roles/README.md) | 成员 A–D 各自负责范围的详细说明（单人单文档） |
| [req/](req/) | 案例与作业要求 |

---

## 学术诚信

本作业为 IN3338 课程提交。引用外部资料须在报告中注明来源，请勿抄袭其他小组作品。
