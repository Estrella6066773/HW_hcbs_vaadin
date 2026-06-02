# 成员 A — Film Listing + 场馆/价格数据

**答辩主责：** 影片列表、筛选、详情；四城影院与票价数据  
**组内目标：** 约 25%（Exercise 1 组内占比最高）  
**合并前须请谁 Review：** B（占座/余座）、D（User 实体边界）

---

## 1. 你在项目中的位置

```text
用户：浏览 / 筛选场次
  → FilmRecommendView / FilmDetailView (你)
  → FilmListingService / HcbsSearchService / FilmCatalogService (你)
  → ShowingRepository.searchShowings 等 (你)
  → City / Cinema / Film / Showing / PriceRule (你)
```

你负责 **「看得见场次与票价从哪来」** 的整条链路。订票、取消、登录由 B/C/D 主责，但你须能回答与自己数据相关的交叉问题（如余座怎么算）。

---

## 2. 核心交付清单

| 编号 | 工作项 | 主要文件 |
| --- | --- | --- |
| A1 | ERD（列表/场馆/票价） | 答辩 PDF；`model` 中 City–Cinema–Screen–Film–Showing–PriceRule |
| A2 | 场次搜索 | `ShowingRepository.searchShowings` |
| A3 | 列表 Service | `FilmListingService`, `HcbsSearchService`, `FilmCatalogService`, `PosterResourceService` |
| A4 | 列表 UI | `FilmRecommendView`, `FilmDetailView`, `AdditiveShowingFilterPanel`, `FilmPoster` |
| A5 | **模块测试** | `FilmListingServiceTest`, `HcbsSearchServiceTest`, `FilmCatalogServiceTest` |
| A6 | DTO 协作 | `ShowingRow`, `CityOption`, `FilmCardDto` 等与 B 对齐字段 |

---

## 3. 必须掌握的实体与关系

| 实体 | 要点 |
| --- | --- |
| `City` / `Cinema` / `Screen` | 四城、每城 2 影院；银幕容量 |
| `Film` / `Actor` / `FilmActor` | 片名、简介、演员、`posterUrl` |
| `Showing` | 场次 + `TimeBand`；同银幕同日期同时间唯一 |
| `PriceRule` | `City` + `TimeBand` + `SeatArea` → 价格 |
| `Seat` | 座位区域（下厅/上厅）；余座 = 总座 − 已确认占座（Service 中计算） |

**不负责主责：** `Booking`/`BookingSeat` 业务（B/C）；`User` 实体细节（D 主责，你了解即可）。

---

## 4. 关键 API

| 类 / 方法 | 作用 |
| --- | --- |
| `ShowingRepository.searchShowings` | 城市/影院/日期/片名筛选 → TC_002 |
| `FilmListingService.search` / `searchUpcoming` | 组装 `ShowingRow`（含余座、演员） |
| `HcbsSearchService` | 主页叠加筛选与统计卡片 |
| `FilmCatalogService` | 影片详情、推荐墙 |
| `PosterResourceService` | 本地海报是否存在 |

---

## 5. 负责的测试（答辩必讲）

| 测试类 | 用例 | 你必须能说明 |
| --- | --- | --- |
| `FilmListingServiceTest` | TC_001, TC_002 | 列表有数据；筛选生效 |
| `HcbsSearchServiceTest` | 主页搜索 | 无日期时只显示今天及以后 |
| `FilmCatalogServiceTest` | 详情/海报 | `posterUrl` 路径规则 |

**答辩话术示例：**「我负责列表模块；`FilmListingServiceTest` 验证筛选与 `ShowingRow` 组装；TC_002 对应 `searchShowings` 四个可选参数。」

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=FilmListingServiceTest,HcbsSearchServiceTest,FilmCatalogServiceTest
```

---

## 6. 答辩 3–5 分钟提纲

1. **演示**：`/` → 选城市/日期 → Search → 表格；可选 `/film/{id}`。  
2. **ERD**：City → Cinema → Screen → Showing ← Film；PriceRule 挂 City。  
3. **代码**：指 `searchShowings` 与 `FilmListingService.toShowingRow`（余座）。  
4. **测试**：指 `FilmListingServiceTest` 中 TC_002 相关方法。  
5. **边界**：票价存在 `PriceRule` 表，不按场次写死。

---

## 7. 协作边界

| 同事 | 协作 |
| --- | --- |
| **B** | 占座查询影响余座；改 `Showing` 约束前对齐 |
| **C** | 取消不直接改你的 Repository |
| **D** | `User` 与登录；列表页不处理权限细节 |

---

## 8. 自检清单

- [ ] 能画出列表相关 ERD  
- [ ] 能演示 TC_001/002 并对应测试方法名  
- [ ] 能口述 `View → Service → Repository → Entity` 一条链  
- [ ] 未在列表 View 中 `import repository`  
- [ ] 本模块 `mvn test` 全绿

---

## 9. 相关文档

| 文档 | 用途 |
| --- | --- |
| [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) §6 | 答辩顺序与小抄 |
| [TEST_CASES.md](../TEST_CASES.md) | TC_001–002 |
| [MEMBER_B_应用服务.md](MEMBER_B_应用服务.md) | 订票（邻接模块） |
