# 成员 A 答辩稿 — 首页（Home）与场次筛选

> **分工：** 成员 A — 影片浏览模块（`home` 包）  
> **答辩重点功能：** 叠加式场次筛选（城市 / 影院 / 日期 / 片名）  
> **演示路径：** 启动应用 → 浏览器 `/` → 填筛选条件 → Search → 看海报列表变化 → 复制 URL 刷新验证

按答辩 PPT「思路一 → 思路二 → 思路三」顺序作答。标黄题为 A 必答重点。

---

## 思路一：技术框架与数据库

### 1. 用 3–5 句话解释 Vaadin / Spring Boot

**Vaadin：** 用 Java 写用户界面的 Web 框架。每个页面是一个带 `@Route` 的 Java 类（如 `FilmRecommendView`），继承 `VerticalLayout`、`Div` 等组件拼装界面。用户操作在服务端触发监听器，Vaadin 自动把 DOM 与服务器状态同步，**不需要单独写 React/Vue 前端项目**。

**Spring Boot：** 提供依赖注入、安全、JPA 持久化。View 通过构造器注入 `HcbsSearchService` 等 Bean，由 Spring 管理生命周期。

**本项目：** 前端在 `com.hcbs.web.*`，业务在 `com.hcbs.service.*`，实体在 `com.hcbs.model.*`，开发库常用 H2。

---

### 2. JPA 注解（结合 A 模块用到的表）

| 注解 | 含义 | 本模块示例 |
|------|------|------------|
| `@Entity` | 标记为 JPA 实体，对应一张表 | `City`、`Cinema`、`Showing`、`Film` |
| `@Table(name = "...")` | 指定表名 | `City` → 表 `city` |
| `@Id` | 主键 | `cityId`、`showingId` |
| `@GeneratedValue` | 主键自增 | `GenerationType.IDENTITY` |
| `@Column` | 列约束（非空、长度等） | `City.name` |
| `@ManyToOne` | 多对一关联 | `Showing` → `Film`、`Screen` |

示例（城市表）：

```13:24:src/main/java/com/hcbs/model/City.java
@Entity
@Table(name = "city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cityId;

    @Column(nullable = false, unique = true, length = 80)
    private String name;
```

场次与影片关联：

```19:35:src/main/java/com/hcbs/model/Showing.java
@Entity
public class Showing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showingId;

    @ManyToOne(optional = false)
    private Film film;

    @ManyToOne(optional = false)
    private Screen screen;
```

---

### 3. 哪个类对应哪张表？（筛选相关）

| Java 类 | 数据库表 | 与筛选的关系 |
|---------|----------|--------------|
| `City` | `city` | 下拉「城市」数据源 |
| `Cinema` | `cinema` | 下拉「影院」数据源（按城市过滤） |
| `Showing` | `showing`（默认表名） | 场次：按城市/影院/日期/片名查询的核心 |
| `Film` | `film` | 片名匹配、最终展示影片卡片 |
| `Screen` | `screen` | 场次 → 影院链路 |

---

### 4. 哪个属性对应哪列？

以 `City` 为例：

| 类属性 | 数据库列 | 说明 |
|--------|----------|------|
| `cityId` | `city_id`（或默认驼峰映射） | 主键，写入 URL `?city=`、写入 `ShowingListingFilter.cityId` |
| `name` | `name` | 界面 ComboBox 显示的文字 |

筛选 DTO **不映射表**，只传查询条件：

```9:9:src/main/java/com/hcbs/dto/ShowingListingFilter.java
public record ShowingListingFilter(Long cityId, Long cinemaId, LocalDate date, String filmTitle) {
```

---

### 5. 为什么要用 Repository？怎么用？

**原因：** 把「怎么查数据库」从业务逻辑里拆出去；Service 只调接口，换数据库或写复杂查询时不改 View。

**用法：** 继承 `JpaRepository<实体, 主键类型>`，Spring 自动实现增删改查；复杂条件可写自定义查询。

```6:7:src/main/java/com/hcbs/repository/CityRepository.java
public interface CityRepository extends JpaRepository<City, Long> {
}
```

A 模块调用链：`FilmListingService.listCities()` → `cityRepository.findAll()` → 转成 `CityOption` 给 ComboBox。

---

### 6. 【标黄 · 必答】用代码解释系统里应用的一个 Vaadin 部件

**推荐答：`ComboBox<CityOption>`（城市下拉框）**  
文件：`AdditiveShowingFilterPanel.java`

**是什么：** Vaadin 自带下拉选择组件，支持占位符、清空按钮、选项列表、值变化监听。

**在项目里干什么：**

1. 展示所有城市（`setItems`）；
2. 用户选城市后，级联刷新影院列表；
3. 选中项类型是 `CityOption`（id + 名称），提交筛选时只取 `cityId`。

```33:58:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
    private final ComboBox<CityOption> city = new ComboBox<>("City");
    // ...
        city.setItems(searchService.listCities());
        city.setClearButtonVisible(true);
        city.setPlaceholder("Any city");
        city.addValueChangeListener(event -> {
            Long cityId = event.getValue() == null ? null : event.getValue().cityId();
            cinema.setItems(searchService.listCinemas(cityId));
            cinema.clear();
            refreshActiveFilters();
        });
```

**口述要点（30 秒）：**

- `ComboBox` 是 Vaadin Flow 的表单控件，泛型 `CityOption` 表示每一项的数据类型。
- `setItems` 绑定数据源；`addValueChangeListener` 是**触发事件**（换城市 → 重载影院）。
- `CityOption` 重写 `toString()` 返回城市名，所以下拉里显示的是名称而不是 id。

**可补充：** 同文件还有 `DatePicker`（日期）、`TextField`（片名）、`Button`（Search/Clear）、`HorizontalLayout`（筛选条布局）。

---

### 7. Vaadin 的优点（结合 Home 说 2 点即可）

1. **前后端统一用 Java**：筛选面板、首页列表都在 `home` 包，和 `HcbsSearchService` 同一语言，减少前后端分离的接口对齐成本。  
2. **组件化 + 路由集成**：`@Route("")` 映射首页，`navigate` + `QueryParameters` 可把筛选条件放进 URL，刷新/分享链接仍能保持状态。

---

### 8. Spring 在本项目中的作用（A 模块视角）

| 作用 | 在筛选流程中的体现 |
|------|-------------------|
| **依赖注入** | `FilmRecommendView(HcbsSearchService, PosterResourceService)` 构造器注入 |
| **@Service** | `HcbsSearchService`、`FilmListingService` 由容器创建 |
| **JPA + Repository** | `CityRepository`、`ShowingRepository` 查城市、场次 |
| **Security** | 首页 `@AnonymousAllowed`，未登录可浏览筛选 |

View **不直接**访问 Repository，只调 Service — 符合分层。

---

## 思路二：功能实现与逻辑

### 1. 【标黄 · 必答】用代码解释「场次筛选」功能如何实现

**功能名：** Additive Session Search（叠加筛选）— 用户填了哪些条件，就只按哪些条件收窄；**空字段不参与筛选**。

**涉及文件（按运行顺序）：**

| 顺序 | 文件 | 职责 |
|:----:|------|------|
| ① | `AdditiveShowingFilterPanel.java` | UI：四字段 + Search/Clear；`getFilter()` / `applyFilter()` |
| ② | `ShowingListingFilter.java` | 筛选条件 DTO |
| ③ | `ShowingFilterQuery.java` | DTO ↔ URL 查询参数 |
| ④ | `FilmRecommendView.java` | 页面编排：`beforeEnter`、`runSearch`、查库、渲染海报 |
| ⑤ | `HcbsSearchService.java` | `searchFilmsByShowings`、`listCities`、`listCinemas` |
| ⑥ | `FilmListingService.java` | 按条件查场次 `searchUpcoming` |
| ⑦ | `FilmCatalogService.java` | 按 filmId 列表生成 `FilmCardDto` |

---

#### 流程 A：用户点击 Search（最常演示）

```
用户改 ComboBox/DatePicker/TextField
    → 点 Search
    → FilmRecommendView.runSearch()
    → filterPanel.getFilter() 得到 ShowingListingFilter
    → ShowingFilterQuery.toQueryParameters() 写入 URL
    → navigate 本页
    → beforeEnter 再次执行
    → displayFilteredFilms → 查库 → 刷新海报网格
```

**① UI 收成 DTO：**

```107:112:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
    public ShowingListingFilter getFilter() {
        Long cityId = city.getValue() == null ? null : city.getValue().cityId();
        Long cinemaId = cinema.getValue() == null ? null : cinema.getValue().cinemaId();
        return ShowingListingFilter.of(cityId, cinemaId, date.getValue(), filmTitle.getValue());
    }
```

**② 写入 URL（可分享、可刷新）：**

```93:98:src/main/java/com/hcbs/web/home/FilmRecommendView.java
    private void runSearch() {
        getUI().ifPresent(ui -> ui.navigate(
                FilmRecommendView.class,
                ShowingFilterQuery.toQueryParameters(filterPanel.getFilter())));
    }
```

URL 示例：`/?city=1&cinema=2&date=2026-06-15&title=bat`

**③ 进入页面时统一加载数据：**

```79:91:src/main/java/com/hcbs/web/home/FilmRecommendView.java
    public void beforeEnter(BeforeEnterEvent event) {
        ShowingListingFilter fromUrl = ShowingFilterQuery.fromQueryParameters(event.getLocation().getQueryParameters());
        if (ShowingFilterQuery.hasCriteria(fromUrl)) {
            filterPanel.applyFilter(fromUrl);
            filteredBrowse = true;
            displayFilteredFilms(fromUrl);
        } else {
            filteredBrowse = false;
            displayAllFilms();
        }
    }
```

**④ 按场次筛出「能看的影片」并显示：**

```112:117:src/main/java/com/hcbs/web/home/FilmRecommendView.java
    private void displayFilteredFilms(ShowingListingFilter filter) {
        List<FilmCardDto> films = searchService.searchFilmsByShowings(filter);
        renderFeatureCarousel(searchService.searchFilms(FilmCatalogFilter.of("")));
        renderPosters(films);
        filterPanel.setBrowseSummary(films.size(), true);
    }
```

**⑤ 业务层：先查场次，再 DISTINCT 影片 id，再拿卡片：**

```52:58:src/main/java/com/hcbs/service/search/HcbsSearchService.java
    public List<FilmCardDto> searchFilmsByShowings(ShowingListingFilter filter) {
        List<Long> filmIds = filmListingService.searchUpcoming(filter).stream()
                .map(ShowingRow::filmId)
                .distinct()
                .toList();
        return filmCatalogService.listFilmCards(filmIds);
    }
```

**口述 1 分钟总结：**  
面板负责「人机交互 + 条件对象」；`ShowingFilterQuery` 负责「条件与 URL 同步」；`beforeEnter` 是**唯一查列表的入口**（Search 不直接查库，而是改 URL 再触发 `beforeEnter`）；Service 按场次过滤后映射成首页海报用的 `FilmCardDto`。

---

#### 流程 B：打开带参数的链接 / 刷新页面

跳过点击 Search，直接走 `beforeEnter` → `applyFilter(fromUrl)` 把 URL 填回控件 → `displayFilteredFilms`。

```135:159:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
    public void applyFilter(ShowingListingFilter filter) {
        // 按 cityId / cinemaId 在 listCities、listCinemas 里 findFirst，setValue 到 ComboBox
        date.setValue(filter.date());
        filmTitle.setValue(filter.filmTitle() == null ? "" : filter.filmTitle());
        refreshActiveFilters();
    }
```

---

#### 流程 C：Clear 清空

`clearFilters()` 清空控件 → `onClear` → `showAllFilms()` → 空 URL → `displayAllFilms()`。

---

#### 关键设计词（答辩加分）

| 术语 | 含义 |
|------|------|
| **Additive** | 只填城市就只按城市筛；四字段可任意组合 |
| **DTO** | `ShowingListingFilter` 只存 id/日期/片名，不存影院全名 |
| **URL 即状态** | 筛选结果与地址栏绑定，便于演示「复制链接」 |
| **级联下拉** | 选城市 → `listCinemas(cityId)` → 清空影院 |

---

### 2. 注册时手机号 / 邮箱校验（非 A 主责 · 简答）

在 **`auth`** 模块（如 `RegisterView` / 校验 Service）。A 只需说：「我负责 Home 筛选，注册校验由成员 C 实现。」

---

### 3. 选座是单独页还是弹窗？（非 A 主责 · 简答）

本项目选座在 **`booking/BookingView`** 独立路由页（`/booking`），不是弹窗。  
优点：流程清晰、URL 可带 `showingId`；缺点：多一次页面跳转。  
A 从首页点 Book 会跳到详情再进订票（见 `FilmDetailView`）。

---

### 4. 管理导航栏的文件？

**`com.hcbs.web.shell.MainLayout.java`**

- 侧栏：`refreshDrawer()`
- 顶栏：`refreshHeader()`
- 所有 `@Route(..., layout = MainLayout.class)` 的页面共用

---

### 5. 员工和客户看到不同页面吗？

**是。** `MainLayout.refreshDrawer()` 按角色显示不同菜单：

| 角色 | 侧栏（典型） |
|------|----------------|
| 未登录 | Home |
| CUSTOMER | Home + My bookings |
| STAFF | Home + Book tickets + Cancellation |
| ADMIN | 再加 Data admin |

首页 **`FilmRecommendView` 未登录可访问**（`@AnonymousAllowed`），筛选功能演示不需要登录。

---

### 6. 【A 模块】浏览 / 筛选流程：文件、流程、触发事件

| 步骤 | 文件 | 触发事件 |
|------|------|----------|
| 打开首页 | `FilmRecommendView` | 路由进入 → **`beforeEnter`** |
| 显示全部影片 | `displayAllFilms()` | URL 无筛选参数 |
| 用户改条件 | `AdditiveShowingFilterPanel` | ComboBox/TextField **`ValueChangeListener`**（只更新提示文字） |
| 执行搜索 | `FilmRecommendView.runSearch()` | Search 按钮 **click** 或片名框 **Enter** |
| URL 解析 | `ShowingFilterQuery` | `beforeEnter` 内自动 |
| 回填面板 | `applyFilter()` | `beforeEnter` 内 |
| 查库 | `HcbsSearchService.searchFilmsByShowings` | `beforeEnter` → `displayFilteredFilms` |
| 渲染海报 | `renderPosters()` | 同上 |
| 清空 | `clearFilters()` + `showAllFilms()` | Clear 按钮 **click** |
| 进详情 | `FilmDetailView` | 海报卡片 **click**（可带 query 保持筛选上下文） |

**构造时挂面板（只执行一次）：**

```52:52:src/main/java/com/hcbs/web/home/FilmRecommendView.java
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showAllFilms);
```

---

## 思路三：Java 语法（结合筛选代码）

### 1. 某方法的返回类型和参数是什么？

**示例 1：`getFilter()`**

```107:112:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
    public ShowingListingFilter getFilter() {
```

| 项目 | 答案 |
|------|------|
| 返回类型 | `ShowingListingFilter`（record DTO） |
| 参数 | 无 |

**示例 2：`searchFilmsByShowings`**

```52:52:src/main/java/com/hcbs/service/search/HcbsSearchService.java
    public List<FilmCardDto> searchFilmsByShowings(ShowingListingFilter filter) {
```

| 项目 | 答案 |
|------|------|
| 返回类型 | `List<FilmCardDto>` |
| 参数 | `ShowingListingFilter filter` |

**示例 3：`beforeEnter`**

| 项目 | 答案 |
|------|------|
| 返回类型 | `void` |
| 参数 | `BeforeEnterEvent event` |

---

### 2. Lambda 表达式是什么？你代码里哪里用了？

**定义：** Lambda 是**匿名函数**的简写，常用来实现「只有一个抽象方法的接口」（函数式接口），例如监听器、`Runnable`。

**示例 1：城市变化监听（`ValueChangeListener`）**

```53:58:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
        city.addValueChangeListener(event -> {
            Long cityId = event.getValue() == null ? null : event.getValue().cityId();
            cinema.setItems(searchService.listCinemas(cityId));
            cinema.clear();
            refreshActiveFilters();
        });
```

`event -> { ... }` 等价于「当 ComboBox 的值变了就执行这段逻辑」。

**示例 2：方法引用传给面板（`Runnable`）**

```52:52:src/main/java/com/hcbs/web/home/FilmRecommendView.java
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showAllFilms);
```

`this::runSearch` 是 Lambda 的简写，表示「点击 Search 时调用当前 View 的 `runSearch()` 方法」。

**示例 3：Stream 里按 id 过滤（`applyFilter`）**

```140:143:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
            city.setValue(searchService.listCities().stream()
                    .filter(option -> option.cityId().equals(filter.cityId()))
                    .findFirst()
                    .orElse(null));
```

`option -> option.cityId().equals(...)` 是 Lambda，对列表每一项做判断。

---

## 答辩现场 3 分钟话术（背这一段即可）

1. **我负责首页 `FilmRecommendView` 和筛选面板 `AdditiveShowingFilterPanel`。**
2. **Vaadin 部件：** 用 `ComboBox<CityOption>` 做城市选择，选城市后监听器级联刷新影院下拉。
3. **筛选逻辑：** 四个条件可叠加，空的不参与；点 Search 把条件变成 `ShowingListingFilter`，再写到 URL；`beforeEnter` 读 URL、回填面板、调用 `HcbsSearchService.searchFilmsByShowings` 查「有匹配场次的影片」，最后用 `FilmCardDto` 画海报。
4. **演示：** 选城市 + 日期 → Search → 看列表变短 → 复制地址栏 URL → 新标签打开仍相同结果 → Clear 恢复全部。

---

## 速查：A 模块文件清单

| 类型 | 路径 |
|------|------|
| 主页 | `web/home/FilmRecommendView.java` |
| 筛选 UI | `web/home/component/AdditiveShowingFilterPanel.java` |
| URL 工具 | `web/home/ShowingFilterQuery.java` |
| 条件 DTO | `dto/ShowingListingFilter.java` |
| 下拉 DTO | `dto/CityOption.java`、`dto/CinemaOption.java` |
| 卡片 DTO | `dto/FilmCardDto.java` |
| 搜索入口 | `service/search/HcbsSearchService.java` |
| 场次查询 | `service/listing/FilmListingService.java` |
| 实体 | `model/City.java`、`Cinema.java`、`Showing.java`、`Film.java` |
| 导航壳 | `web/shell/MainLayout.java` |

更细阅读顺序见：[MEMBER_A_影片浏览.md](./MEMBER_A_影片浏览.md)、[home/README.md](../../src/main/java/com/hcbs/web/home/README.md)。
