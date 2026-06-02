# 成员 A — 模块 1：影片浏览（Home）

**路由：** `/` · `/film/:id`  
**组内目标：** 约 25%（Exercise 1 ERD 牵头）  
**完整文件列表：** [四人分工.md §成员 A](../四人分工.md#成员-a--模块-1影片浏览home)

---

## 1. 模块职责

负责「找片、看详情、筛场次」**端到端**：

```text
FilmRecommendView / FilmDetailView
  → HcbsSearchService / FilmListingService / FilmCatalogService
  → ShowingRepository 等
  → Film / Showing / City / Cinema / PriceRule …
```

含海报配置（`FilmPosterCatalog`、`HcbsMediaCatalog`）与静态图片资源。

---

## 2. 核心文件

| 类型 | 文件 |
| --- | --- |
| Service | `HcbsSearchService`, `FilmListingService`, `FilmCatalogService`, `PosterResourceService` |
| View | `FilmRecommendView`, `FilmDetailView`, `AdditiveShowingFilterPanel`, `FilmPoster` |
| Test | `HcbsSearchServiceTest`, `FilmListingServiceTest`, `FilmCatalogServiceTest` |
| 用例 | TC_001, TC_002 |

---

## 3. 答辩 4 分钟

1. `/` → 筛选 → Search → 海报/表格  
2. `/film/{id}` → 场次 Grid → Book 按钮（跳转 B 模块，A 只讲 navigate）  
3. ERD：City → Cinema → Screen → Showing ← Film  
4. 测试：`FilmListingServiceTest` 对应 TC_002  

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=HcbsSearchServiceTest,FilmListingServiceTest,FilmCatalogServiceTest
```

---

## 4. 协作边界

| 同事 | 交界 |
| --- | --- |
| **B** | 详情页 Book → `/booking?showingId=` |
| **C** | Home 对所有人开放，不处理登录 |
| **D** | Admin 排片改 Showing；A 提供列表展示 |

---

## 5. 自检

- [ ] 能演示 TC_001/002  
- [ ] 能画列表相关 ERD  
- [ ] View 未 `import repository`  
- [ ] 本模块 3 个测试全绿  

---

## 6. 相关文档

- [home/README.md](../../src/main/java/com/hcbs/web/home/README.md)  
- [TEST_CASES.md](../TEST_CASES.md)  
- [MEMBER_B_订票.md](MEMBER_B_订票.md)
