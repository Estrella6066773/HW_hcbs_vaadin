# home — Home 板块

对应侧栏 **Home**：Browse films and find showtimes。

## 文件清单

| 文件 | 路由 | 说明 |
|------|------|------|
| `FilmRecommendView.java` | `/` | 主页：海报墙 + 筛选 |
| `FilmDetailView.java` | `/film/:id` | 影片详情 + 场次表 |
| `ShowingFilterQuery.java` | — | URL 查询参数工具类 |
| [component/](component/README.md) | — | FilmPoster、筛选面板等 |

## 依赖的 Service

| Service | 用途 |
|---------|------|
| `HcbsSearchService` | 搜影片、搜场次、城市/影院下拉 |
| `PosterResourceService` | 判断海报文件是否存在 |

---

## FilmRecommendView.java — 主页 `/`

### 路由注解

```java
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@AnonymousAllowed   // 未登录也能浏览
```

### 页面组成（从上到下）

1. **PageHero** — 页头标题
2. **featureCarousel** — 精选影片轮播（Banner 图）
3. **AdditiveShowingFilterPanel** — 城市/影院/日期/片名筛选
4. **posterGrid** — 影片海报卡片网格

### 核心流程

#### 进入页面：`beforeEnter()`

```text
读 URL 查询参数 (ShowingFilterQuery.fromQueryParameters)
  ├─ 有筛选条件 → applyFilter + displayFilteredFilms
  └─ 无条件     → displayAllFilms
```

#### 用户点 Search：`runSearch()`

```java
ui.navigate(FilmRecommendView.class, ShowingFilterQuery.toQueryParameters(filter));
```

把筛选条件写进 URL（如 `/?city=1&date=2026-06-01`），刷新后可分享、可后退。

#### 展示数据

| 方法 | 调用 Service | 展示 |
|------|-------------|------|
| `displayAllFilms()` | `searchFilms("")` | 全部在映影片 |
| `displayFilteredFilms()` | `searchFilmsByShowings(filter)` | 符合场次的影片 |

#### 轮播 `renderFeatureCarousel()`

- 预定义 4 张 Banner（Spirited Away 等）
- 用 `RouterLink` 链到 `FilmDetailView`
- `executeJs(...)` 注入前端 JS 实现自动滚动（4.2 秒切换）

#### 海报卡片 `createPosterCard()`

- `FilmPoster` 显示封面
- `RouterLink` → `FilmDetailView`，若正在筛选则带上 query 参数

### 答辩：Vaadin 组件示例

- `ComboBox`（在 FilterPanel 里）
- `RouterLink`（海报点击跳转）
- `Button`（轮播箭头）
- `Checkbox`（Auto scroll）
- `Image`（Banner 图）

---

## FilmDetailView.java — 详情 `/film/:id`

### 路由注解

```java
@Route(value = "film", layout = MainLayout.class)
public class FilmDetailView implements HasUrlParameter<Long>
```

`HasUrlParameter<Long>` 表示 URL 路径参数 `/film/123` 中的 `123` 会传给 `setParameter()`。

### 核心流程

#### `setParameter(BeforeEvent event, Long filmId)`

1. 从 URL 读筛选条件（与主页共享 `ShowingFilterQuery`）
2. 调用 `searchService.getFilmDetail(filmId, filter)` 拿 `FilmDetailDto`
3. 调用 `buildDetail()` 渲染 UI

#### `buildDetail()` 页面结构

```text
BackToHomeAction（返回主页，保留筛选）
  ↓
海报 + 标题/简介/演员 (FilmPoster + H2 + Paragraph)
  ↓
Grid<ShowingRow> 场次表格
  └─ 每行有 Book 按钮 → navigate(BookingView, showingId)
```

#### Book 按钮 — 跨板块跳转（答辩重点）

```java
book.addClickListener(e -> ui.navigate(
    BookingView.class,
    ShowingFilterQuery.withShowingId(filter, row.showingId())
));
```

从 home 跳到 booking，URL 形如 `/booking?showingId=456`。

### 使用的 Vaadin 组件

| 组件 | 用途 |
|------|------|
| `Grid<ShowingRow>` | 场次表格（影院、日期、时间、余座） |
| `GridVariant.LUMO_ROW_STRIPES` | 斑马纹样式 |
| `addComponentColumn` | 在表格列里放 Button |
| `FilmPoster` | 封面 |
| `BackToHomeAction` | 返回按钮 |

---

## ShowingFilterQuery.java — URL 参数工具

**不是 View**，是纯 Java 工具类，负责筛选条件与 URL 互转。

### 参数名

| 常量 | URL 键 | 含义 |
|------|--------|------|
| `PARAM_CITY` | `city` | 城市 ID |
| `PARAM_CINEMA` | `cinema` | 影院 ID |
| `PARAM_DATE` | `date` | 日期 ISO 格式 |
| `PARAM_TITLE` | `title` | 片名关键词 |
| `PARAM_SHOWING` | `showingId` | 场次 ID（订票用） |

### 主要方法

| 方法 | 方向 |
|------|------|
| `toQueryParameters(filter)` | 筛选 → URL |
| `fromQueryParameters(params)` | URL → 筛选 |
| `withShowingId(filter, id)` | 筛选 + 场次 ID（详情页 Book 用） |
| `showingIdFrom(params)` | 从 URL 取 showingId（BookingView 用） |

### 为什么用 URL 传参？

- 用户刷新页面筛选不丢失
- 从详情返回主页可保留筛选
- 演示时可复制链接给评委

---

## 与其他板块的交互

```text
home (FilmRecommendView)
  → RouterLink → home (FilmDetailView)
  → Book 按钮 → booking (BookingView)

home (ShowingFilterQuery)
  ←→ booking (BookingView.beforeEnter 读 showingId)
  ←→ home/component (FilterPanel, BackToHomeAction)
```
