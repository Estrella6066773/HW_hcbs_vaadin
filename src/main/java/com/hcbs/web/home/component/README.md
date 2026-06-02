# home/component — Home 专用组件

本文件夹组件**只在 home 和 auth 页面使用**（auth 借用 BackToHomeAction）。

## 文件清单

| 文件 | 类型 | 用途 |
|------|------|------|
| `FilmPoster.java` | Div 子类 | 影片封面（有图/缺失占位） |
| `AdditiveShowingFilterPanel.java` | Div 子类 | 叠加筛选面板 |
| `BackToHomeAction.java` | Button 子类 | 返回主页按钮 |

---

## FilmPoster.java

### 职责

根据数据库里的 `posterUrl` 路径显示 `<img>`，文件不存在则显示灰色「缺失」框。

### 构造参数

```java
new FilmPoster(posterResources, film.posterUrl(), altText, optionalClassNames...)
```

| 参数 | 来源 |
|------|------|
| `posterResources` | `PosterResourceService`（成员 B） |
| `posterUrl` | DTO 字段，如 `/images/posters/spirited-away.jpg` |
| `altText` | 无障碍替代文字 |

### 逻辑

```text
posterResources.isAvailable(url)?
  ├─ true  → new Image(url) + class "film-poster-image"
  └─ false → Div "film-poster-missing" + 文字「缺失」
```

### 答辩演示

种子数据里 **Solitude** 的海报文件故意缺失，可在主页看到「缺失」占位效果。

### 样式 class（在 styles.css）

- `.film-poster-slot` — 容器
- `.film-poster-image` — 正常图片
- `.film-poster-missing` — 缺失占位

**注意：** View 里不要自己拼路径或生成图片，路径来自 DTO。

---

## AdditiveShowingFilterPanel.java

### 职责

主页筛选 UI：**叠加筛选**——填了的条件才生效，空字段忽略。

### 构造参数

```java
new AdditiveShowingFilterPanel(searchService, onSearch, onClear)
```

| 回调 | 触发时机 |
|------|---------|
| `onSearch` | 用户点 Search 或 Enter |
| `onClear` | 用户点 Clear |

在 `FilmRecommendView` 里分别绑定 `this::runSearch` 和 `this::showAllFilms`。

### 内部 Vaadin 组件

| 组件 | 字段 | 行为 |
|------|------|------|
| `ComboBox<CityOption>` | city | 选城市后刷新影院列表 |
| `ComboBox<CinemaOption>` | cinema | 依赖城市 |
| `DatePicker` | date | 选日期 |
| `TextField` | filmTitle | 片名模糊匹配，支持 Enter 搜索 |
| `Button` | Search / Clear | 搜索 / 清空 |

### 关键方法

| 方法 | 说明 |
|------|------|
| `getFilter()` | 读当前表单 → `ShowingListingFilter` DTO |
| `applyFilter(filter)` | 从 URL 恢复表单（`beforeEnter` 用） |
| `setBrowseSummary(count, filtered)` | 显示「N films on display / matching search」 |
| `refreshActiveFilters()` | 显示当前激活的筛选条件文案 |

### 数据从哪来？

全部通过 `HcbsSearchService`：
- `listCities()`
- `listCinemas(cityId)`
- 筛选结果由 View 层调用 `searchFilmsByShowings`，不在此组件内查库

---

## BackToHomeAction.java

### 职责

继承 `Button`，文案 "Back to home"，点击导航回 `FilmRecommendView`。

### 两种构造

```java
new BackToHomeAction()              // 回 bare /
new BackToHomeAction(filter)        // 回 /?city=...&date=... 保留筛选
```

### 使用位置

- `FilmDetailView` — 带 filter，返回时保留搜索状态
- `LoginView` / `RegisterView` — 无 filter，简单回主页

### 交互代码

```java
addClickListener(event -> getUI().ifPresent(ui -> {
    if (filter != null && ShowingFilterQuery.hasCriteria(filter)) {
        ui.navigate(FilmRecommendView.class, ShowingFilterQuery.toQueryParameters(filter));
    } else {
        ui.navigate(FilmRecommendView.class);
    }
}));
```

---

## 组件关系图

```text
FilmRecommendView
  ├── AdditiveShowingFilterPanel ──→ HcbsSearchService
  ├── FilmPoster ──→ PosterResourceService
  └── RouterLink → FilmDetailView
        ├── FilmPoster
        ├── BackToHomeAction ──→ FilmRecommendView + ShowingFilterQuery
        └── Grid Book → BookingView
```
