# 成员 D — 测试与交付

**角色：** 测试、文档协调与提交交付  
**组内目标：** 约 25%（Exercise 3 组内占比最高，约 62%）  
**负责范围：** `src/test/**`、[TEST_CASES.md](../TEST_CASES.md)、本贡献矩阵维护、提交包与答辩协调  
**合并前须请谁 Review：** A/B/C（被测代码变更）

---

## 1. 你在项目中的位置

```text
A/B/C 实现  →  你写/维护测试与用例表  →  验证业务规则  →  打包提交 + 组织答辩
```

你不「代替」他人讲解模块，但你要保证：**每条课程规则都有测试或手工用例对应**，且 `mvn test` 在干净环境下可重复通过。

---

## 2. 核心交付清单

| 编号 | 工作项 | 交付物 |
| --- | --- | --- |
| D1 | 测试策略 | 单元测试 + 关键路径手工测试 |
| D2 | 自动化测试 | `src/test/java/com/hcbs/**` |
| D3 | 测试用例表 | [TEST_CASES.md](../TEST_CASES.md) TC_001–TC_011 |
| D4 | 缺陷跟踪 | 重复订座、超期订、当日取消等 |
| D5 | 提交打包 | `Group_No.zip` 结构核对 |
| D6 | 答辩协调 | 顺序、计时、每人 3–5 分钟 |
| D7 | 贡献矩阵 | [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) 与 [roles/](README.md) |

---

## 3. 自动化测试地图

### 3.1 核心业务（答辩必提）

| 测试类 | 覆盖 Service | 对应用例 |
| --- | --- | --- |
| `FilmListingServiceTest` | `FilmListingService` | TC_001、TC_002 |
| `BookingServiceTest` | `BookingService` | TC_003–TC_007 |
| `CancellationServiceTest` | `CancellationService` | TC_008–TC_010 |
| `DataLoaderTest` | 种子数据完整性 | 启动数据、城市影院数量；含 `persistsPosterImageUrlOnEveryFilm` |

### 3.2 扩展与基础设施

| 测试类 | 覆盖内容 |
| --- | --- |
| `FilmCatalogServiceTest` | 影片目录、详情 DTO、**封面路径为 `/images/posters/*.jpg`** |
| `HcbsSearchServiceTest` | 主页搜索与统计 |
| `RegistrationServiceTest` | 客户注册 |
| `HcbsPortAllocatorTest` | 端口分配（开发便利） |
| `UiThemeTest` | Vaadin 主题名 `hcbs` |

### 3.3 影片封面相关断言

| 测试 | 断言要点 |
| --- | --- |
| `DataLoaderTest.persistsPosterImageUrlOnEveryFilm` | 每部影片 `posterUrl` 以 `/images/posters/` 开头且以 `.jpg` 结尾 |
| `FilmCatalogServiceTest.listsRecommendedFilmsWithPosters` | DTO 路径格式；`PosterResourceService.isAvailable(SOLITUDE)` 为 false |
| 手工 | 主页与详情：有图显示 JPEG，**Solitude** 显示「缺失」 |

### 3.4 运行命令

```powershell
# 全量（提交前必跑）
mvn "-Dmaven.repo.local=.m2/repository" clean test

# 仅核心业务
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=BookingServiceTest,CancellationServiceTest,FilmListingServiceTest,DataLoaderTest

# 打包
mvn "-Dmaven.repo.local=.m2/repository" package
```

**环境注意：** 项目在 OneDrive 时，测试前关闭正在运行的 `spring-boot:run`，避免锁 `data/` 或 `frontend/generated/`。见 [DEV_TROUBLESHOOTING.md](../DEV_TROUBLESHOOTING.md)。

---

## 4. TC_001–TC_011 速查（你必须能对应到代码）

| ID | 名称 | 预期 | 自动化 / 手工 |
| --- | --- | --- | --- |
| TC_001 | 列表展示场次 | 表格含片名、影院、时间、余座等 | `FilmListingServiceTest` + 手工 `/` |
| TC_002 | 筛选场次 | 城市/影院/日期/片名 | `FilmListingServiceTest` |
| TC_003 | 成功订票 | 生成 `BookingReceipt`、参考号 | `BookingServiceTest.createsBooking...` |
| TC_004 | 收据字段完整 | `BookingReceipt.toReceiptText()` | Service 测试 + `BookingView` 手工 |
| TC_005 | 防重复占座 | 同场同座第二次失败 | `rejectsDuplicateSeatForSameShowing` |
| TC_006 | 计价 | 伦敦晚场下厅 £12×2=£24 | `BookingServiceTest` 中断言 |
| TC_007 | 7 日限制 | 超过 7 天拒绝 | `rejectsBookingMoreThanSevenDaysInAdvance` |
| TC_008 | 成功取消 | 状态 CANCELLED、座位释放 | `cancelsBookingAndAppliesFiftyPercentCharge` |
| TC_009 | 50% 手续费 | £12 订单取消费 £6 | 同上 |
| TC_010 | 当日不可取消 | 放映日当天拒绝 | `rejectsSameDayCancellation` |
| TC_011 | 无效参考号 | 友好错误提示 | 手工 `CancellationView` |

**B 改业务规则时：** 你先更新测试断言，再改 [TEST_CASES.md](../TEST_CASES.md) 的 Expected 列。

---

## 5. 测试数据与重置（与 A/B 协作）

| 文档 | 内容 |
| --- | --- |
| [TEST_DATABASE.md](../TEST_DATABASE.md) | 四城、场次边界日期、演示用户、`HCBS-SEED001` |
| [README_CN.md](../README_CN.md) §演示数据 | 12 个演示账号，密码 `demo` |

**重置库（集成 / 手工测试前常用）：**

1. 停止应用  
2. 删除 `./data/hcbs.lock.db` 与 `./data/hcbs.mv.db`  
3. 重新 `spring-boot:run` 或 `mvn test`

座位格式或种子变更后，**删库验证**，不要为旧库改测试期望值（除非需求真变）。

---

## 6. 手工测试脚本（示例）

按 [README_CN.md](../README_CN.md) 流程，建议准备一张勾选表：

1. **列表：** `/` → 选 London → Search → 表格有行。  
2. **订票：** `/booking` → 选今日+3 天内场次 → 选下厅座位 → Confirm → 收据含 `HCBS-`。  
3. **重复订座：** 记下座位，再订同场同座 → 应失败（TC_005）。  
4. **取消：** `/cancellation` → 输入上一步参考号 → 显示 50% 费用 → Cancel → 成功。  
5. **当日取消：** 用种子中「今天放映」的订单（若有）→ 应拒绝（TC_010）。  
6. **无效参考号：** `HCBS-INVALID` → Notification（TC_011）。

记录 **Actual Result** 填回 [TEST_CASES.md](../TEST_CASES.md)。

---

## 7. 提交包结构（D5）

```text
Group_No.zip
├── src/
├── pom.xml
├── README.md
├── .Docs/
│   ├── README_CN.md
│   ├── CONTRIBUTION_MATRIX.md
│   ├── ARCHITECTURE.md
│   ├── TEST_CASES.md
│   ├── TEST_DATABASE.md
│   ├── roles/                    ← 四人角色说明
│   └── req/                      （可选）ERD.pdf
```

**打包前检查：**

- [ ] 不含 `data/`、`target/`、`.m2/`、`frontend/generated/`  
- [ ] `git status` 干净或仅含应提交文件  
- [ ] `mvn clean test` 通过  
- [ ] CONTRIBUTION_MATRIX 姓名、学号、签字日期已填  

---

## 8. 答辩协调（D6）

| 事项 | 建议 |
| --- | --- |
| 顺序 | A → B → C → D（或导师指定） |
| 时长 | 每人 3–5 分钟 |
| 演示 | 应用可由 **C** 操作浏览器；你负责计时与过渡 |
| 你的内容 | 测试策略、TC 与自动化对应、`mvn test` 结果摘要、已知局限 |
| 不做 | 代替 A/B/C 讲 ERD、Service 实现、界面结构 |

个人材料：幻灯片或一页提纲 + 测试覆盖说明（见 [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) §5.3）。

---

## 9. 协作边界

| 成员 | 你如何协作 |
| --- | --- |
| **A** | 实体/查询变更 → 更新 DataLoader 相关断言、TC_002/005 |
| **B** | 规则变更 → 先改 `*ServiceTest`，再通知你更新用例表 |
| **C** | UI 文案、路由变更 → 更新手工步骤；`UiThemeTest` 失败则反馈 C |
| **全组** | 贡献矩阵与 [roles/](README.md) 姓名映射；Git 提交与占比一致说明 |

**公平性：** 若某人 Git 占比低于 15%，答辩前需补充说明（见 CONTRIBUTION_MATRIX §8）。

---

## 10. 缺陷分类（D4 参考）

| 类型 | 示例 | 归属修复 |
| --- | --- | --- |
| 业务逻辑 | 7 日判断错误 | B |
| 查询/占座 | 取消后座位仍显示占用 | A + B |
| 界面 | 按钮无响应、样式错位 | C |
| 测试/种子 | 测试偶发失败、种子缺场次 | D + A/B |
| 环境 | H2 锁库 90020 | 删库，见 DEV_TROUBLESHOOTING |

---

## 11. 答辩 3–5 分钟提纲

1. **测试金字塔**：以 Service 单元测试为主，UI 主题测试 + 手工 TC 为辅。  
2. **举 2 个例子**：TC_005 对应哪条测试方法；TC_009 断言什么金额。  
3. **`mvn test` 结果**：通过类数量、总耗时（截图即可）。  
4. **故意非法输入**：超 7 天、重复座位、当日取消——测过什么。  
5. **已知局限**：未做支付、未做 E2E 浏览器自动化（若适用）。

---

## 12. 相关文档

| 文档 | 用途 |
| --- | --- |
| [TEST_CASES.md](../TEST_CASES.md) | 正式用例表 |
| [TEST_DATABASE.md](../TEST_DATABASE.md) | 种子与边界数据 |
| [MEMBER_A/B/C 角色说明](README.md) | 被测模块速查 |
| [README_CN.md](../README_CN.md) §如何测试 | 与仓库一致的命令 |

---

## 13. 自检清单

- [ ] `mvn clean test` 全绿  
- [ ] TC_001–TC_011 均有 Expected，手工项已填 Actual  
- [ ] 提交 zip 无 `data/`、`target/`  
- [ ] CONTRIBUTION_MATRIX 与 roles 文档已链到组内真实姓名  
- [ ] 答辩彩排：每人 3–5 分钟，你已计时试跑一轮
