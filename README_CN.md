# Horizon Cinemas 影院订票系统（HCBS）

**模块：** IN3338 面向对象开发  
**院校：** UWE Bristol  
**学年：** 2025–2026  

英文说明见 [README.md](README.md)。

本仓库为 HCBS 案例的小组课程作业实现，通过 Vaadin 网页界面提供**影片列表**、**订票**与**取消订票**三项核心功能，后端采用 Spring Boot 与 JPA。

---

## 目录

- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [小组分工](#小组分工)
- [答辩说明](#答辩说明)
- [环境要求](#环境要求)
- [如何运行](#如何运行)
- [如何测试](#如何测试)
- [业务规则](#业务规则)
- [演示数据](#演示数据)
- [提交说明](#提交说明)
- [延伸阅读](#延伸阅读)

---

## 功能概览

| 功能 | 路由 | 说明 |
| --- | --- | --- |
| **影片列表** | `/` | 按城市、影院、日期、片名筛选；展示简介、演员、类型、分级、场次与余座。 |
| **订票** | `/booking` | 选择场次与座位（下厅 / 上厅），生成含唯一参考号的收据。 |
| **取消订票** | `/cancellation` | 按参考号查询；放映日之前可取消，收取 50% 手续费。 |

按作业要求**不在范围内**：管理员 / 经理界面、登录与权限、支付流程。

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
│   ├── model/              # JPA 实体（成员 A）
│   ├── repository/         # 数据访问（成员 A）
│   ├── dto/                # 界面用数据传输对象（成员 B）
│   ├── service/
│   │   ├── listing/        # 影片列表服务
│   │   ├── booking/        # 订票服务
│   │   └── cancellation/   # 取消服务
│   ├── web/                # Vaadin 页面（成员 C）
│   └── config/             # 启动种子数据
├── src/test/java/          # 自动化测试（成员 D）
├── frontend/themes/hcbs/   # 主题样式（成员 C）
├── TEST_CASES.md           # 手工测试用例表
├── ARCHITECTURE.md         # 分层与依赖规则
├── CONTRIBUTION_MATRIX.md  # 成员 A–D 贡献矩阵
├── README.md               # 英文说明
└── README_CN.md            # 本文件（中文说明）
```

**依赖规则：** `web` 仅依赖 `service` 与 `dto`；`service` 依赖 `repository` 与 `model`；页面不得直接调用 Repository 或绑定 JPA 实体。

---

## 小组分工

按模块化包划分，便于四人并行开发：

| 成员 | 角色 | 主要负责 |
| --- | --- | --- |
| **A** | 持久化与数据设计 | `model/`、`repository/`、Exercise 1 的 ERD |
| **B** | 应用服务与业务规则 | `dto/`、`service/*`、`DataLoader` |
| **C** | Web 界面与主题 | `web/`、`frontend/themes/hcbs/` |
| **D** | 测试与交付 | `src/test/`、`TEST_CASES.md`、提交包 |

贡献比例与交付清单见 [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md)。合并代码前请遵守 [ARCHITECTURE.md](ARCHITECTURE.md) 中的评审规则。

---

## 答辩说明

演示（Presentation）占模块成绩 **20%**。本组采用**分项答辩**：**每位成员单独说明自己负责的部分**，不是由一人代讲全组。

| 成员 | 建议讲解内容（约 3–5 分钟） | 可演示材料 |
| --- | --- | --- |
| **A** | ERD 与表设计、实体关系、Repository 查询（如场次搜索、活跃占座） | ERD 图、关键实体类 |
| **B** | 三个 Service 的职责、业务规则（7 日订票、计价、取消手续费）、DTO 如何隔离界面与数据库 | 服务类、DTO、`DataLoader` |
| **C** | 三个页面的交互流程、为何只依赖 Service、主题与布局 | 浏览器现场操作 |
| **D** | 测试策略、`TEST_CASES.md`、自动化测试覆盖、已知缺陷与范围外功能 | 测试类、`mvn test` 结果 |

**要求：** 全组成员须到场；导师提问时，各人应能回答**自己模块**内的设计与局限。详细提纲见贡献矩阵第 5 节。

---

## 环境要求

- **JDK 21**（`java -version`）
- **Maven 3.9+**（`mvn -version`）
- 可访问 `http://localhost:8080` 的浏览器

若项目位于 OneDrive 目录，运行 `mvn test` 前请先关闭正在运行的开发服务，避免 `frontend/generated` 被占用导致失败。

---

## 如何运行

在项目根目录执行：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

浏览器打开：

```text
http://localhost:8080
```

**导航：** 影片列表（首页）· 订票 · 取消（侧栏菜单）。

若修改了 Vaadin 依赖或主题，需先执行一次：

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
| `DataLoaderTest` | 种子数据（城市、影院、座位） |
| `UiThemeTest` | 自定义 `hcbs` 主题 |

手工场景见 [TEST_CASES.md](TEST_CASES.md)（TC_001–TC_011）。

打包：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" package
```

---

## 业务规则

| 规则 | 实现位置 |
| --- | --- |
| 订票参考号唯一 | `BookingService` 生成并校验 |
| 最多提前 7 天订票 | `validateBookingDate` |
| 同一场次同一座位不可重复预订 | 仅统计状态为 `CONFIRMED` 的占座 |
| 票价 | 城市 × 时段 × 座位区域（`PriceRule`） |
| 至少提前 1 天取消 | `CancellationService.canCancel` |
| 取消手续费 | 订票总价的 50% |
| 放映日当天不可取消 | 拒绝并提示 |
| 取消后释放座位 | 删除对应 `BookingSeat`，可再次预订 |

---

## 演示数据

首次启动时，`DataLoader` 会写入：

- **城市：** London、Birmingham、Bristol、Cardiff（每城至少 2 家影院）
- **银幕：** 每影院 2 块，每块 50 座（下厅 25 + 上厅 25）
- **影片与演员：** 示例片目及演职员关联
- **场次：** 未来数日内的放映
- **票价：** 案例下厅价目表；上厅在下厅基础上 +£2
- **用户：** `staff`、`admin`、`manager`（无登录界面；订票使用 `BOOKING_STAFF`）

---

## 提交说明

请以 Blackboard 最新 brief 为准，通常包括：

1. **Exercise 1：** ERD、逻辑模式、简短设计说明（PDF）。
2. **Exercise 2：** 完整源码 `Group_No.zip`，并附 README（如何运行）；建议同时包含 `README_CN.md`。
3. **Exercise 3：** 测试证据——`TEST_CASES.md`、测试类，可选截图。
4. **贡献矩阵：** [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md)，全组到场、**分项答辩**。

建议压缩包结构：

```text
Group_No.zip
├── src/
├── pom.xml
├── README.md
├── README_CN.md
├── ARCHITECTURE.md
├── CONTRIBUTION_MATRIX.md
├── TEST_CASES.md
└── （可选）docs/ERD.pdf
```

---

## 延伸阅读

- [ARCHITECTURE.md](ARCHITECTURE.md) — 分层、模块、代码评审归属  
- [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) — 成员 A–D 贡献与答辩分工  
- [TEST_CASES.md](TEST_CASES.md) — 测试用例表  
- 案例与作业说明：`.Docs/req/`

---

## 学术诚信

本作业为 IN3338 小组提交，全体成员对提交内容负责。请勿抄袭其他小组成果；报告中须注明引用来源。
