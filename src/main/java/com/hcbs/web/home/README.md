# home — Home 板块

对应侧栏 **Home**：Browse films and find showtimes。

> **如何跳转到代码（重要）**
>
> - **Markdown 预览**里点击链接常会显示「找不到」——预览不支持 `文件.java:行号` 这种路径。
> - 请在本文件的 **编辑区**（左侧源码，不要开预览）**Ctrl + 点击**（Mac：**Cmd + 点击**）下面的 `.java` 链接，会打开对应文件。
> - 打开文件后按 **Ctrl + G**（Mac **Ctrl + G** 或 **Cmd + L**），输入表格里的 **行号**（如 `79`）即可跳到方法。
> - 或 **Ctrl + P**，输入 `FilmRecommendView.java:79` 一步打开并定位。

## 文件清单

| 文件 | 路由 | 说明 |
|------|------|------|
| [FilmRecommendView.java](FilmRecommendView.java) | `/` | 主页：海报墙 + 筛选 |
| [FilmDetailView.java](FilmDetailView.java) | `/film/:id` | 影片详情 + 场次表 |
| [ShowingFilterQuery.java](ShowingFilterQuery.java) | — | URL 查询参数工具类 |
| [component/](component/README.md) | — | FilmPoster、筛选面板等 |

## 依赖的 Service

| Service | 用途 |
|---------|------|
| [HcbsSearchService.java](../../service/search/HcbsSearchService.java) | 搜影片、搜场次、城市/影院下拉 |
| [PosterResourceService.java](../../service/catalog/PosterResourceService.java) | 判断海报文件是否存在 |

---

## 端到端流程（方法索引）

| 步骤 | 方法 | 源文件 | 行号 |
|------|------|--------|:----:|
| 1 进入首页 | `beforeEnter()` | [FilmRecommendView.java](FilmRecommendView.java) | 79 |
| 2 URL → 筛选 DTO | `fromQueryParameters()` | [ShowingFilterQuery.java](ShowingFilterQuery.java) | 63 |
| 3 同步筛选面板 | `applyFilter()` | [AdditiveShowingFilterPanel.java](component/AdditiveShowingFilterPanel.java) | 141 |
| 4a 展示全部影片 | `displayAllFilms()` | [FilmRecommendView.java](FilmRecommendView.java) | 104 |
| 4b 展示筛选结果 | `displayFilteredFilms()` | [FilmRecommendView.java](FilmRecommendView.java) | 112 |
| 5 用户点 Search | `runSearch()` | [FilmRecommendView.java](FilmRecommendView.java) | 93 |
| 6 筛选 DTO → URL | `toQueryParameters()` | [ShowingFilterQuery.java](ShowingFilterQuery.java) | 35 |
| 7 读面板当前筛选 | `getFilter()` | [AdditiveShowingFilterPanel.java](component/AdditiveShowingFilterPanel.java) | 97 |
| 8 查全部影片 | `searchFilms()` | [HcbsSearchService.java](../../service/search/HcbsSearchService.java) | 31 |
| 9 按场次筛影片 | `searchFilmsByShowings()` | [HcbsSearchService.java](../../service/search/HcbsSearchService.java) | 50 |
| 10 渲染海报墙 | `renderPosters()` / `createPosterCard()` | [FilmRecommendView.java](FilmRecommendView.java) | 256 / 262 |
| 11 渲染轮播 | `renderFeatureCarousel()` / `createFeatureSlide()` | [FilmRecommendView.java](FilmRecommendView.java) | 119 / 169 |
| 12 进入详情页 | `setParameter()` | [FilmDetailView.java](FilmDetailView.java) | 63 |
| 13 查详情 + 场次 | `getFilmDetail()` | [HcbsSearchService.java](../../service/search/HcbsSearchService.java) | 58 |
| 14 渲染详情 UI | `buildDetail()` | [FilmDetailView.java](FilmDetailView.java) | 80 |
| 15 返回首页 | `BackToHomeAction` 构造 | [BackToHomeAction.java](component/BackToHomeAction.java) | 16 |
| 16 点 Book 跳订票 | `withShowingId()` | [ShowingFilterQuery.java](ShowingFilterQuery.java) | 55 |
| 17 订票页读场次 ID | `showingIdFrom()` / `BookingView.beforeEnter()` | [ShowingFilterQuery.java](ShowingFilterQuery.java) / [BookingView.java](../../booking/BookingView.java) | 74 / 69 |

---

## FilmRecommendView.java — 主页 `/`

### 路由注解

[`FilmRecommendView.java:33-35`](FilmRecommendView.java)

```java
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@AnonymousAllowed   // 未登录也能浏览
```

### 页面组成（从上到下）

构造器 [`FilmRecommendView()`](FilmRecommendView.java) 中 `add(...)` 顺序见 [FilmRecommendView.java:75](FilmRecommendView.java)：

1. **PageHero** — 页头标题
2. **featureCarousel** — 精选影片轮播（Banner 图）
3. **[AdditiveShowingFilterPanel](component/AdditiveShowingFilterPanel.java)** — 城市/影院/日期/片名筛选
4. **posterGrid** — 影片海报卡片网格

### 核心流程

#### 进入页面：[`beforeEnter()`](FilmRecommendView.java)

```text
读 URL 查询参数 → ShowingFilterQuery.fromQueryParameters()  [ShowingFilterQuery.java:63]
  ├─ 有筛选条件 → filterPanel.applyFilter()               [AdditiveShowingFilterPanel.java:141]
  │               + displayFilteredFilms()                  [FilmRecommendView.java:112]
  └─ 无条件     → displayAllFilms()                         [FilmRecommendView.java:104]
```

判断是否有筛选：[`ShowingFilterQuery.hasCriteria()`](ShowingFilterQuery.java)

#### 用户点 Search：[`runSearch()`](FilmRecommendView.java)

[`FilmRecommendView.java:95-97`](FilmRecommendView.java)

```java
ui.navigate(FilmRecommendView.class, ShowingFilterQuery.toQueryParameters(filterPanel.getFilter()));
//           ↑ FilmRecommendView.java:96    ↑ ShowingFilterQuery.java:35   ↑ getFilter() AdditiveShowingFilterPanel.java:97
```

把筛选条件写进 URL（如 `/?city=1&date=2026-06-01`），刷新后可分享、可后退。  
清空筛选：[`showAllFilms()`](FilmRecommendView.java)

#### 展示数据

| 方法 | 调用 Service | 展示 |
|------|-------------|------|
| [`displayAllFilms()`](FilmRecommendView.java) | [`searchFilms()`](../../service/search/HcbsSearchService.java) | 全部在映影片 |
| [`displayFilteredFilms()`](FilmRecommendView.java) | [`searchFilmsByShowings()`](../../service/search/HcbsSearchService.java) | 符合场次的影片 |

两者都会调用 [`renderFeatureCarousel()`](FilmRecommendView.java) 和 [`renderPosters()`](FilmRecommendView.java)，并更新 [`setBrowseSummary()`](component/AdditiveShowingFilterPanel.java)。

#### 轮播 [`renderFeatureCarousel()`](FilmRecommendView.java)

- 预定义 4 张 Banner（[FilmRecommendView.java:124-128](FilmRecommendView.java)）
- [`createFeatureSlide()`](FilmRecommendView.java) 用 `RouterLink` 链到 `FilmDetailView`
- [`enableFeatureCarousel()`](FilmRecommendView.java) 里 `executeJs(...)` 注入前端 JS 实现自动滚动（4.2 秒切换）
- 控件：[`createFeatureControls()`](FilmRecommendView.java)

#### 海报卡片 [`createPosterCard()`](FilmRecommendView.java)

- [`FilmPoster`](component/FilmPoster.java) 显示封面
- [`RouterLink`](FilmRecommendView.java) → `FilmDetailView`；筛选模式下 [`toQueryParameters()`](ShowingFilterQuery.java) 带上 query（[FilmRecommendView.java:273-274](FilmRecommendView.java)）

### 答辩：Vaadin 组件示例

- `ComboBox` — [AdditiveShowingFilterPanel.java:32-33](component/AdditiveShowingFilterPanel.java)
- `RouterLink` — [FilmRecommendView.java:271](FilmRecommendView.java)（海报）、[FilmRecommendView.java:189](FilmRecommendView.java)（轮播）
- `Button` — [FilmRecommendView.java:151](FilmRecommendView.java)（轮播箭头）
- `Checkbox` — [FilmRecommendView.java:199](FilmRecommendView.java)（Auto scroll）
- `Image` — [FilmRecommendView.java:171](FilmRecommendView.java)（Banner 图）

---

## FilmDetailView.java — 详情 `/film/:id`

### 路由注解

[`FilmDetailView.java:31-34`](FilmDetailView.java)

```java
@Route(value = "film", layout = MainLayout.class)
public class FilmDetailView implements HasUrlParameter<Long>
```

`HasUrlParameter<Long>` 表示 URL 路径参数 `/film/123` 中的 `123` 会传给 [`setParameter()`](FilmDetailView.java)。

### 核心流程

#### [`setParameter(BeforeEvent event, Long filmId)`](FilmDetailView.java)

1. [`ShowingFilterQuery.fromQueryParameters()`](ShowingFilterQuery.java) — 从 URL 读筛选条件（[FilmDetailView.java:69](FilmDetailView.java)）
2. [`searchService.getFilmDetail(filmId, filter)`](../../service/search/HcbsSearchService.java) — 拿 `FilmDetailDto`（[FilmDetailView.java:71](FilmDetailView.java)）
3. [`buildDetail()`](FilmDetailView.java) — 渲染 UI

### [`buildDetail()`](FilmDetailView.java) 页面结构

```text
BackToHomeAction(filter)                    [BackToHomeAction.java:16]  [FilmDetailView.java:99]
  ↓
海报 + 标题/简介/演员                        [FilmPoster.java:13]       [FilmDetailView.java:81-92]
  ↓
Grid<ShowingRow> 场次表格                    [FilmDetailView.java:111]
  └─ 每行 Book 按钮 → navigate(BookingView)  [FilmDetailView.java:121-123]
```

筛选提示：[`ShowingFilterQuery.hasCriteria()`](ShowingFilterQuery.java) — [FilmDetailView.java:104](FilmDetailView.java)

#### Book 按钮 — 跨板块跳转（答辩重点）

[`FilmDetailView.java:121-123`](FilmDetailView.java)

```java
book.addClickListener(e -> ui.navigate(
    BookingView.class,
    ShowingFilterQuery.withShowingId(filter, row.showingId())));
//  ↑ FilmDetailView.java:122              ↑ ShowingFilterQuery.java:55
```

从 home 跳到 booking，URL 形如 `/booking?showingId=456`。  
订票页读取：[`BookingView.beforeEnter()`](../../booking/BookingView.java) → [`showingIdFrom()`](ShowingFilterQuery.java)

### 使用的 Vaadin 组件

| 组件 | 用途 | 代码位置 |
|------|------|----------|
| `Grid<ShowingRow>` | 场次表格 | [FilmDetailView.java:111](FilmDetailView.java) |
| `GridVariant.LUMO_ROW_STRIPES` | 斑马纹样式 | [FilmDetailView.java:128](FilmDetailView.java) |
| `addComponentColumn` | 表格列里放 Button | [FilmDetailView.java:117](FilmDetailView.java) |
| `FilmPoster` | 封面 | [FilmDetailView.java:81](FilmDetailView.java) |
| `BackToHomeAction` | 返回按钮 | [FilmDetailView.java:99](FilmDetailView.java) |

---

## ShowingFilterQuery.java — URL 参数工具

**不是 View**，是纯 Java 工具类，负责筛选条件与 URL 互转。

### 参数名

[`ShowingFilterQuery.java:19-23`](ShowingFilterQuery.java)

| 常量 | URL 键 | 含义 |
|------|--------|------|
| `PARAM_CITY` | `city` | 城市 ID |
| `PARAM_CINEMA` | `cinema` | 影院 ID |
| `PARAM_DATE` | `date` | 日期 ISO 格式 |
| `PARAM_TITLE` | `title` | 片名关键词 |
| `PARAM_SHOWING` | `showingId` | 场次 ID（订票用） |

### 主要方法

| 方法 | 方向 | 行号 |
|------|------|------|
| [`hasCriteria(filter)`](ShowingFilterQuery.java) | 判断是否在筛选 | [ShowingFilterQuery.java:30](ShowingFilterQuery.java) |
| [`toQueryParameters(filter)`](ShowingFilterQuery.java) | 筛选 → URL | [ShowingFilterQuery.java:35](ShowingFilterQuery.java) |
| [`fromQueryParameters(params)`](ShowingFilterQuery.java) | URL → 筛选 | [ShowingFilterQuery.java:63](ShowingFilterQuery.java) |
| [`withShowingId(filter, id)`](ShowingFilterQuery.java) | 筛选 + 场次 ID（详情页 Book 用） | [ShowingFilterQuery.java:55](ShowingFilterQuery.java) |
| [`showingIdFrom(params)`](ShowingFilterQuery.java) | 从 URL 取 showingId（BookingView 用） | [ShowingFilterQuery.java:74](ShowingFilterQuery.java) |

内部解析：[`parseLong()`](ShowingFilterQuery.java) · [`parseDate()`](ShowingFilterQuery.java) · [`firstValue()`](ShowingFilterQuery.java)

### 为什么用 URL 传参？

- 用户刷新页面筛选不丢失（[`beforeEnter`](FilmRecommendView.java) 再次 [`fromQueryParameters`](ShowingFilterQuery.java)）
- 从详情返回主页可保留筛选（[`BackToHomeAction`](component/BackToHomeAction.java)）
- 演示时可复制链接给评委

---

## 与其他板块的交互

```text
home FilmRecommendView.beforeEnter()     [FilmRecommendView.java:79]
  → RouterLink createPosterCard()        [FilmRecommendView.java:262]
  → FilmDetailView.setParameter()        [FilmDetailView.java:63]

FilmDetailView Book 按钮                  [FilmDetailView.java:121]
  → ShowingFilterQuery.withShowingId()   [ShowingFilterQuery.java:55]
  → BookingView.beforeEnter()            [BookingView.java:69]
  → ShowingFilterQuery.showingIdFrom()   [ShowingFilterQuery.java:74]

ShowingFilterQuery
  ←→ AdditiveShowingFilterPanel          [AdditiveShowingFilterPanel.java:97] getFilter / [141] applyFilter
  ←→ BackToHomeAction                    [BackToHomeAction.java:16]
```
