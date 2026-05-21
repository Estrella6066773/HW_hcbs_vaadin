# HCBS 小组贡献矩阵（Contribution Matrix）

**模块**：IN3338 Object-Oriented Development  
**项目**：Horizon Cinemas Booking System (HCBS)  
**小组编号**：Group ___（请填写 Blackboard 上的组号）  
**学年**：2025–2026  
**提交日期**：___ / ___ / 2026  

架构说明见仓库根目录 [ARCHITECTURE.md](../ARCHITECTURE.md)。

---

## 1. 小组成员

| 序号 | 姓名（Name） | 学号（Student ID） | 校内邮箱（UWE Email） | 小组角色 |
| --- | --- | --- | --- | --- |
| 1 | 【填写】 | 【填写】 | 【填写】 | 持久化层负责人 |
| 2 | 【填写】 | 【填写】 | 【填写】 | 应用服务层负责人 |
| 3 | 【填写】 | 【填写】 | 【填写】 | Web 界面负责人 |
| 4 | 【填写】 | 【填写】 | 【填写】 | 测试与交付负责人 |

---

## 2. 模块化分工（按代码包）

| 成员 | 负责包 / 目录 | 禁止擅自修改（需对方 Review） |
| --- | --- | --- |
| **成员 1** | `com.hcbs.model/*`、`com.hcbs.repository/*` | `web/*`、`service/*` |
| **成员 2** | `com.hcbs.dto/*`、`com.hcbs.service.listing/*`、`com.hcbs.service.booking/*`、`com.hcbs.service.cancellation/*`、`com.hcbs.config/DataLoader.java` | `model` 结构变更须 1 号确认 |
| **成员 3** | `com.hcbs.web/*`、`frontend/themes/hcbs/*` | 不直接依赖 `repository`；只使用 `dto` + `service` |
| **成员 4** | `src/test/**`、`TEST_CASES.md`、提交包、演示材料、本矩阵 | 测试失败时协调 2/3 修复，不擅自改业务规则 |

### 成员 2 子模块（可再拆）

| 子模块 | 类 | 建议负责人（2 人时可合并） |
| --- | --- | --- |
| 列表 | `FilmListingService` | 成员 2A |
| 订票 | `BookingService` | 成员 2B |
| 取消 | `CancellationService` | 成员 2C |

四人组时均由成员 2 总负责，内部再分子任务。

---

## 3. 总体贡献比例（Overall Contribution）

| 成员 | Exercise 1<br>ERD (20%) | Exercise 2<br>实现 (50%) | Exercise 3<br>测试 (30%) | Presentation<br>(20%)* | **总体** |
| --- | ---: | ---: | ---: | ---: | ---: |
| 成员 1 | 30% | 20% | 10% | 25% | **25%** |
| 成员 2 | 10% | 35% | 15% | 20% | **25%** |
| 成员 3 | 5% | 30% | 10% | 30% | **25%** |
| 成员 4 | 5% | 15% | 65% | 25% | **25%** |

---

## 4. 分项工作明细

### 成员 1 — 持久化层

| 工作包 | 交付物 | 路径 |
| --- | --- | --- |
| ERD 与逻辑模式 | Exercise 1 PDF（单独提交） | `.Docs/` 或课程指定位置 |
| JPA 实体 | 全部实体与枚举 | `src/main/java/com/hcbs/model/` |
| 数据访问 | 含 `searchShowings`、活跃占座查询 | `src/main/java/com/hcbs/repository/` |
| 协作 | 评审 `dto` 字段是否够用 | 与成员 2 对接 |

### 成员 2 — 应用服务与 DTO

| 工作包 | 交付物 | 路径 |
| --- | --- | --- |
| 列表服务 | 筛选、余座、`ShowingRow` | `service/listing/FilmListingService.java` |
| 订票服务 | 7 日规则、计价、收据 DTO | `service/booking/BookingService.java` |
| 取消服务 | 50% 费用、释放座位 | `service/cancellation/CancellationService.java` |
| 界面契约 | 全部 `dto/*` | `src/main/java/com/hcbs/dto/` |
| 演示数据 | 四城种子数据 | `config/DataLoader.java` |

### 成员 3 — Web 层

| 工作包 | 交付物 | 路径 |
| --- | --- | --- |
| 影片列表 | 含 description 列 | `web/FilmListingView.java` |
| 订票 | 仅依赖 `BookingService` | `web/BookingView.java` |
| 取消 | 仅依赖 `CancellationService` | `web/CancellationView.java` |
| 布局与主题 | 导航、样式 | `web/MainLayout.java`、`web/AppShell.java`、`frontend/themes/hcbs/` |

### 成员 4 — 测试与交付

| 工作包 | 交付物 | 路径 |
| --- | --- | --- |
| 服务测试 | 订票 / 取消 / 列表 | `src/test/java/com/hcbs/service/**` |
| 配置与 UI 测试 | 种子数据、主题 | `DataLoaderTest`、`web/UiThemeTest` |
| 测试用例表 | TC_001–TC_011 | `TEST_CASES.md` |
| 提交与演示 | `Group_No.zip`、幻灯片 | 按 Blackboard 要求 |

---

## 5. 依赖规则（全组遵守）

1. `web` → 只允许依赖 `service.*` 与 `dto`（`BookingView` 中的 `SeatArea` 枚举除外）。  
2. `service` → 依赖 `repository`、`model`；向 `web` 只暴露 `dto`。  
3. `repository` → 只依赖 `model`。  
4. 合并前：改 `model`/`repository` 需成员 1 Review；改 `service`/`dto` 需成员 2 Review；改 `web` 需成员 3 Review。

---

## 6. 已知范围外功能

Admin / Manager 界面、登录鉴权、支付接口——按课程要求不实现。

---

## 7. 成员确认

| 成员 | 签名 | 日期 |
| --- | --- | --- |
| 成员 1 | | |
| 成员 2 | | |
| 成员 3 | | |
| 成员 4 | | |
