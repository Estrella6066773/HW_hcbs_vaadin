# HCBS 答辩完整稿 · Full Defense Script

> **项目 / Project：** Horizon Cinemas Booking System（Vaadin Flow + Spring Boot + JPA + H2）  
> **成员 A 负责 / Member A scope：** 首页 [`FilmRecommendView.java`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) + 场次筛选 [`AdditiveShowingFilterPanel.java`](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java)  
> **标黄必答 / Yellow highlights：** 思路一 Q6（Vaadin 部件）、思路二 Q1（筛选功能）— 由 A 主讲  
> **其余题目 / Other questions：** 按**整个项目**作答，附可点击源码路径

按答辩 PPT 顺序：**思路一 → 思路二 → 思路三**

---

# 思路一 · Track 1：技术框架与数据库 / Technical Framework & Database

---

## Q1. 用 3–5 句话解释 Vaadin / Spring Boot  
## Explain Vaadin / Spring Boot in 3–5 sentences

### Vaadin

**中文：**  
Vaadin 是用 **Java 写 Web 界面** 的框架。每个页面是一个 Java 类，用 `@Route` 映射 URL（例如首页 [`FilmRecommendView`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) 的 `@Route(value = "", layout = MainLayout.class)`）。页面继承 `VerticalLayout`、`Div` 等组件拼装 UI；用户点击按钮、改下拉框会在**服务端**触发监听器，Vaadin 自动同步浏览器 DOM，**不需要单独维护 React/Vue 前端工程**。

**English:**  
Vaadin is a Java-first web UI framework. Each screen is a Java class annotated with `@Route` (e.g. home at `""` in [`FilmRecommendView`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java)). Views compose layouts and components in Java; user events run on the server and Vaadin syncs the DOM — **no separate React/Vue frontend**.

### Spring Boot

**中文：**  
Spring Boot 提供**依赖注入**、**Spring Security** 登录鉴权、**Spring Data JPA** 访问数据库。View 通过构造器注入 Service（如 `HcbsSearchService`），Spring 自动创建并注入 Bean；业务与持久化在 `service` / `repository` 层，View 不直接写 SQL。

**English:**  
Spring Boot provides DI, Spring Security, and JPA. Views receive services via constructor injection; business logic lives in `service` and `repository` packages — views never touch SQL directly.

### 本项目分层 / Project layering

| 层 Layer | 包 Package | 示例 Example |
|----------|------------|--------------|
| 入口 Entry | [`HcbsApplication.java`](../../src/main/java/com/hcbs/HcbsApplication.java) | `@SpringBootApplication` 启动 |
| Web UI | [`com.hcbs.web.*`](../../src/main/java/com/hcbs/web/) | View、Layout、自定义组件 |
| 业务 Service | [`com.hcbs.service.*`](../../src/main/java/com/hcbs/service/) | 订票、筛选、注册 |
| 持久化 Repository | [`com.hcbs.repository.*`](../../src/main/java/com/hcbs/repository/) | JPA 接口 |
| 实体 Entity | [`com.hcbs.model.*`](../../src/main/java/com/hcbs/model/) | 与数据库表映射 |
| DTO | [`com.hcbs.dto.*`](../../src/main/java/com/hcbs/dto/) | 层间传输，非表映射 |
| 配置 Config | [`com.hcbs.config.*`](../../src/main/java/com/hcbs/config/) | Security、种子数据 |
| 数据库 DB | [`application.properties`](../../src/main/resources/application.properties) | H2 文件库 `./data/hcbs` |

---

## Q2. JPA 注解有哪些？分别什么意思？  
## What JPA annotations are used and what do they mean?

| 注解 Annotation | 中文含义 | English | 项目示例 Example |
|-----------------|----------|---------|------------------|
| `@Entity` | 标记 JPA 实体，对应数据库表 | Marks entity mapped to a table | 所有 [`model`](../../src/main/java/com/hcbs/model/) 下实体 |
| `@Table(name=…)` | 指定表名 | Table name | [`User`](../../src/main/java/com/hcbs/model/User.java) → `app_user` |
| `@Id` | 主键 | Primary key | `cityId`, `filmId`, `bookingId` |
| `@GeneratedValue` | 主键生成策略 | PK generation | `GenerationType.IDENTITY` 自增 |
| `@Column` | 列名、长度、非空、唯一 | Column constraints | `User.username` 唯一 50 字符 |
| `@ManyToOne` | 多对一外键关联 | Many-to-one FK | `Showing.film`, `Cinema.city` |
| `@JoinColumn` | 指定外键列名 | FK column name | `Booking.createdBy` → `created_by_user_id` |
| `@Enumerated(STRING)` | 枚举存字符串 | Enum as string | `UserRole`, `BookingStatus` |
| `@Index` | 数据库索引 | DB index | `Booking` 上 customer/showing 索引 |
| `@UniqueConstraint` | 联合唯一 | Composite unique | `Screen`: 同影院内厅号唯一 |
| `@PrePersist` / `@PreUpdate` | 插入/更新前回调 | Lifecycle hooks | [`User`](../../src/main/java/com/hcbs/model/User.java) 规范化 email/phone |

**代码示例 / Code example — 用户表：**

```22:56:src/main/java/com/hcbs/model/User.java
@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_status", columnList = "status")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;
    // ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
```

**代码示例 / Code example — 订单外键：**

```38:49:src/main/java/com/hcbs/model/Booking.java
    @ManyToOne(optional = false)
    private Showing showing;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(optional = true)
    @JoinColumn(name = "customer_user_id")
    private User customer;
```

---

## Q3. 哪个类对应哪张表？  
## Which Java class maps to which database table?

> Hibernate 默认表名：类名转 snake_case（如 `Film` → `film`）；显式 `@Table(name=…)` 以注解为准。

| Java 类 Class | 表名 Table | 业务含义 Purpose |
|---------------|------------|------------------|
| [`City`](../../src/main/java/com/hcbs/model/City.java) | `city` | 城市 |
| [`Cinema`](../../src/main/java/com/hcbs/model/Cinema.java) | `cinema` | 影院（属某城市） |
| [`Screen`](../../src/main/java/com/hcbs/model/Screen.java) | `screen` | 放映厅 |
| [`Seat`](../../src/main/java/com/hcbs/model/Seat.java) | `seat` | 座位 |
| [`Film`](../../src/main/java/com/hcbs/model/Film.java) | `film` | 影片 |
| [`Actor`](../../src/main/java/com/hcbs/model/Actor.java) | `actor` | 演员 |
| [`FilmActor`](../../src/main/java/com/hcbs/model/FilmActor.java) | `film_actor` | 影片–演员关联 |
| [`Showing`](../../src/main/java/com/hcbs/model/Showing.java) | `showing` | 场次 |
| [`PriceRule`](../../src/main/java/com/hcbs/model/PriceRule.java) | `price_rule` | 票价规则 |
| [`User`](../../src/main/java/com/hcbs/model/User.java) | `app_user` | 用户账号 |
| [`Booking`](../../src/main/java/com/hcbs/model/Booking.java) | `booking` | 订单 |
| [`BookingSeat`](../../src/main/java/com/hcbs/model/BookingSeat.java) | `booking_seat` | 订单–座位明细 |

**实体关系简图 / ER sketch:**

```
City 1—* Cinema 1—* Screen 1—* Seat
Film 1—* Showing *—1 Screen
Film *—* Actor (via FilmActor)
User 1—* Booking *—1 Showing
Booking 1—* BookingSeat *—1 Seat
```

---

## Q4. 哪个属性对应哪列？  
## Which field maps to which column?

### 示例 A：`City`（筛选下拉数据源）

| 类属性 Field | DB 列 Column | 用途 Usage |
|--------------|--------------|------------|
| `cityId` | `city_id` | 主键；URL `?city=`；[`ShowingListingFilter.cityId`](../../src/main/java/com/hcbs/dto/ShowingListingFilter.java) |
| `name` | `name` | ComboBox 显示文字 |

### 示例 B：`Film`

| 类属性 Field | DB 列 Column | 用途 Usage |
|--------------|--------------|------------|
| `filmId` | `film_id` | 主键；详情页 `/film/{id}` |
| `title` | `title` | 片名；筛选 partial match |
| `posterUrl` | `poster_url` | 海报路径 |
| `rating` | `rating` | 卡片展示评分 |

### 示例 C：`Booking`

| 类属性 Field | DB 列 Column | 用途 Usage |
|--------------|--------------|------------|
| `bookingReference` | `booking_reference` | 订单号如 `HCBS-…` |
| `totalCost` | `total_cost` | 总价 |
| `guestPhone` | `guest_phone` | 无账号顾客手机号 |
| `showing` | `showing_showing_id` | `@ManyToOne` 外键 |

### 示例 D：DTO **不是**表映射

| 类型 Type | 文件 File | 说明 Note |
|-----------|-----------|-----------|
| 筛选条件 | [`ShowingListingFilter`](../../src/main/java/com/hcbs/dto/ShowingListingFilter.java) | `cityId, cinemaId, date, filmTitle` — 仅查询用 |
| 下拉项 | [`CityOption`](../../src/main/java/com/hcbs/dto/CityOption.java) | `cityId + name` — UI 展示 |
| 海报卡片 | [`FilmCardDto`](../../src/main/java/com/hcbs/dto/FilmCardDto.java) | 首页列表展示 |
| 订单回执 | [`BookingReceipt`](../../src/main/java/com/hcbs/dto/BookingReceipt.java) | 订票成功后文本 |

---

## Q5. 为什么要用 Repository？Repository 怎么用？  
## Why Repository? How is it used?

### 为什么 / Why

**中文：**
1. **分层**：View → Service → Repository → DB，UI 不碰 SQL。
2. **Spring Data JPA** 自动实现 CRUD，减少样板代码。
3. 复杂查询写在 Repository 接口（JPQL / 方法名推导），Service 只调方法名。

**English:** Separation of concerns; Spring Data JPA implements CRUD; custom queries live in repository interfaces.

### 怎么用 / How

**① 简单 CRUD — 继承 `JpaRepository`：**

```6:7:src/main/java/com/hcbs/repository/CityRepository.java
public interface CityRepository extends JpaRepository<City, Long> {
}
```

**② 方法名查询 — [`UserRepository`](../../src/main/java/com/hcbs/repository/UserRepository.java)：`findByPhone`, `existsByEmailIgnoreCase` 等。

**③ 自定义 JPQL — 首页筛选核心 SQL：**

```26:41:src/main/java/com/hcbs/repository/ShowingRepository.java
    @Query("""
            SELECT s FROM Showing s
            JOIN s.screen sc
            JOIN sc.cinema c
            JOIN c.city ct
            WHERE (:cityId IS NULL OR ct.cityId = :cityId)
              AND (:cinemaId IS NULL OR c.cinemaId = :cinemaId)
              AND (:date IS NULL OR s.showDate = :date)
              AND (:filmTitle IS NULL OR :filmTitle = '' OR LOWER(s.film.title) LIKE LOWER(CONCAT('%', :filmTitle, '%')))
            ORDER BY s.showDate ASC, s.startTime ASC
            """)
    List<Showing> searchShowings(...);
```

**④ 全项目 Repository 清单：**

| Repository | 实体 Entity |
|------------|-------------|
| [`CityRepository`](../../src/main/java/com/hcbs/repository/CityRepository.java) | City |
| [`CinemaRepository`](../../src/main/java/com/hcbs/repository/CinemaRepository.java) | Cinema |
| [`ScreenRepository`](../../src/main/java/com/hcbs/repository/ScreenRepository.java) | Screen |
| [`SeatRepository`](../../src/main/java/com/hcbs/repository/SeatRepository.java) | Seat |
| [`FilmRepository`](../../src/main/java/com/hcbs/repository/FilmRepository.java) | Film |
| [`ShowingRepository`](../../src/main/java/com/hcbs/repository/ShowingRepository.java) | Showing |
| [`UserRepository`](../../src/main/java/com/hcbs/repository/UserRepository.java) | User |
| [`BookingRepository`](../../src/main/java/com/hcbs/repository/BookingRepository.java) | Booking |
| [`BookingSeatRepository`](../../src/main/java/com/hcbs/repository/BookingSeatRepository.java) | BookingSeat |
| 等 | Actor, FilmActor, PriceRule |

**调用链示例 / Call chain:**  
[`FilmListingService.listCities()`](../../src/main/java/com/hcbs/service/listing/FilmListingService.java) → `cityRepository.findAll()` → `CityOption` → ComboBox

---

## Q6. 【标黄 · 成员 A 必答】用代码解释系统里应用的一个 Vaadin 部件  
## 【Yellow · Member A】Explain one Vaadin component with code

### 推荐答：`ComboBox<CityOption>` — 城市下拉（筛选面板）

**文件：** [`AdditiveShowingFilterPanel.java`](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java)

| | 中文 | English |
|---|------|---------|
| **部件** | Vaadin `ComboBox`：可搜索下拉，支持清空、占位符、值变化监听 | Vaadin `ComboBox`: searchable drop-down with clear, placeholder, listeners |
| **泛型** | `ComboBox<CityOption>` — 每项含 `cityId` + `name` | Generic item type carries id + display name |
| **数据源** | `searchService.listCities()` 从 DB 经 Service 加载 | Items loaded via service from DB |
| **级联** | 选城市 → 重载影院列表 → 清空已选影院 | City change reloads cinemas and clears cinema selection |

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

**`CityOption` 为何能显示名称：**

```3:7:src/main/java/com/hcbs/dto/CityOption.java
public record CityOption(Long cityId, String name) {
    @Override
    public String toString() {
        return name;
    }
}
```

**口述 30 秒 / 30s pitch:**  
ComboBox 绑定城市列表；`addValueChangeListener` 是**事件触发**；换城市时调用 `listCinemas(cityId)` 实现**级联下拉**；提交筛选时 `getFilter()` 只取 `cityId` 写入 DTO，不存显示名。

**同文件其他 Vaadin 部件 / Other components in same file:** `DatePicker`, `TextField`, `Button`, `HorizontalLayout`, `VerticalLayout`, `Span`

**项目中其他 Vaadin 部件（答辩可对比）/ Other components in project:**

| 部件 Component | 文件 File | 用途 Use |
|----------------|-----------|----------|
| `Grid<ShowingRow>` | [`FilmDetailView.java`](../../src/main/java/com/hcbs/web/home/FilmDetailView.java) | 场次表格 + Book 按钮 |
| `SeatMapPicker`（自定义） | [`SeatMapPicker.java`](../../src/main/java/com/hcbs/web/booking/component/SeatMapPicker.java) | 10×10 座位图 |
| `AppLayout` | [`MainLayout.java`](../../src/main/java/com/hcbs/web/shell/MainLayout.java) | 顶栏 + 侧栏壳层 |
| `PasswordField` | [`LoginView.java`](../../src/main/java/com/hcbs/web/auth/LoginView.java) | 登录密码 |

---

## Q7. Vaadin 有什么优点？  
## What are the advantages of Vaadin?

| # | 中文 | English |
|:-:|------|---------|
| 1 | **全 Java 栈**：UI、业务、JPA 同一语言，小组分工清晰（A 写 home 包即可运行） | Full Java stack — UI and backend in one language |
| 2 | **组件丰富**：ComboBox、Grid、DatePicker 等开箱即用 | Rich built-in components |
| 3 | **路由集成**：`@Route` + `navigate` + `QueryParameters`，筛选状态进 URL（[`ShowingFilterQuery`](../../src/main/java/com/hcbs/web/home/ShowingFilterQuery.java)） | Built-in routing; filter state in shareable URLs |
| 4 | **服务端逻辑**：校验、权限在 Service 层，不暴露给浏览器 | Business rules stay on server |
| 5 | **与 Spring 深度集成**：`VaadinWebSecurity`、构造器注入 View | Native Spring Boot integration |

---

## Q8. Spring 在本项目中起什么作用？  
## What role does Spring play?

| 作用 Role | 中文说明 | 代码位置 Code |
|-----------|----------|---------------|
| **IoC / DI** | 自动创建并注入 Service、Repository、View | 任意 View 构造器，如 [`FilmRecommendView`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) |
| **Spring Data JPA** | Repository 接口 → Hibernate → H2 | [`ShowingRepository`](../../src/main/java/com/hcbs/repository/ShowingRepository.java) |
| **Spring Security** | 登录、角色、`@RolesAllowed` | [`SecurityConfig`](../../src/main/java/com/hcbs/config/SecurityConfig.java), [`HcbsUserDetailsService`](../../src/main/java/com/hcbs/security/HcbsUserDetailsService.java) |
| **事务 `@Transactional`** | 注册、订票原子写入 | [`RegistrationService`](../../src/main/java/com/hcbs/service/auth/RegistrationService.java), [`BookingService.createBooking`](../../src/main/java/com/hcbs/service/booking/BookingService.java) |
| **密码加密** | BCrypt 存 `passwordHash` | [`SecurityConfig.passwordEncoder`](../../src/main/java/com/hcbs/config/SecurityConfig.java) |
| **配置** | 数据源、H2、端口 | [`application.properties`](../../src/main/resources/application.properties) |

**Security 配置要点：**

```12:23:src/main/java/com/hcbs/config/SecurityConfig.java
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/images/**").permitAll());
        setLoginView(http, LoginView.class, "/");
        super.configure(http);
    }
}
```

---

# 思路二 · Track 2：功能实现与逻辑 / Functional Implementation

---

## Q1. 【标黄 · 成员 A 必答】用代码解释「场次筛选」功能如何实现  
## 【Yellow · Member A】Explain session filtering with code

### 功能概述 / Overview

**Additive Session Search（叠加筛选）：** 城市、影院、日期、片名四个条件可任意组合；**未填写的字段不参与筛选**。

**English:** Optional city, cinema, date, and title — only non-empty criteria apply.

### 涉及文件 / Files (execution order)

| 顺序 | 文件 | 类型 | 职责 |
|:----:|------|------|------|
| ① | [`AdditiveShowingFilterPanel.java`](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java) | Vaadin 组件 | UI；`getFilter()` / `applyFilter()` |
| ② | [`ShowingListingFilter.java`](../../src/main/java/com/hcbs/dto/ShowingListingFilter.java) | **DTO** | 筛选条件 |
| ③ | [`ShowingFilterQuery.java`](../../src/main/java/com/hcbs/web/home/ShowingFilterQuery.java) | **URL 工具** | DTO ↔ `QueryParameters` |
| ④ | [`FilmRecommendView.java`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) | Vaadin View | 编排：`beforeEnter`, `runSearch`, 渲染 |
| ⑤ | [`HcbsSearchService.java`](../../src/main/java/com/hcbs/service/search/HcbsSearchService.java) | Service | 统一搜索入口 |
| ⑥ | [`FilmListingService.java`](../../src/main/java/com/hcbs/service/listing/FilmListingService.java) | Service | 查场次 |
| ⑦ | [`ShowingRepository.searchShowings`](../../src/main/java/com/hcbs/repository/ShowingRepository.java) | Repository | JPQL 叠加条件 |
| ⑧ | [`FilmCatalogService.java`](../../src/main/java/com/hcbs/service/catalog/FilmCatalogService.java) | Service | `FilmCardDto` 列表 |

### 流程 A：点击 Search（演示主路径）

```
UI 改条件 → Search click → getFilter() → toQueryParameters() → navigate
→ beforeEnter → applyFilter + displayFilteredFilms → 海报网格更新
```

**① 构造时挂面板 + 回调：**

```52:52:src/main/java/com/hcbs/web/home/FilmRecommendView.java
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showAllFilms);
```

**② UI → DTO：**

```107:112:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
    public ShowingListingFilter getFilter() {
        Long cityId = city.getValue() == null ? null : city.getValue().cityId();
        Long cinemaId = cinema.getValue() == null ? null : cinema.getValue().cinemaId();
        return ShowingListingFilter.of(cityId, cinemaId, date.getValue(), filmTitle.getValue());
    }
```

**③ DTO → URL（可分享）：**

```93:98:src/main/java/com/hcbs/web/home/FilmRecommendView.java
    private void runSearch() {
        getUI().ifPresent(ui -> ui.navigate(
                FilmRecommendView.class,
                ShowingFilterQuery.toQueryParameters(filterPanel.getFilter())));
    }
```

URL 参数键 / Query keys：`city`, `cinema`, `date`, `title` — 见 [`ShowingFilterQuery`](../../src/main/java/com/hcbs/web/home/ShowingFilterQuery.java)

**④ 进页统一查库（Search 不直接查库！）：**

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

**⑤ Service：场次 → 去重 filmId → 卡片：**

```52:58:src/main/java/com/hcbs/service/search/HcbsSearchService.java
    public List<FilmCardDto> searchFilmsByShowings(ShowingListingFilter filter) {
        List<Long> filmIds = filmListingService.searchUpcoming(filter).stream()
                .map(ShowingRow::filmId)
                .distinct()
                .toList();
        return filmCatalogService.listFilmCards(filmIds);
    }
```

**⑥ DB 层 JPQL（空参数 = 忽略该条件）：** 见 Q5 中 `ShowingRepository.searchShowings`

### 流程 B：带 query 打开链接 / Refresh

直接 `beforeEnter` → `applyFilter(fromUrl)` → `displayFilteredFilms`

### 流程 C：Clear

[`clearFilters()`](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java) → `showAllFilms()` → 空 URL → `displayAllFilms()`

### 与详情页联动 / Detail page

[`FilmDetailView`](../../src/main/java/com/hcbs/web/home/FilmDetailView.java) 读取同一套 query，场次 Grid 按筛选过滤；Book 按钮带 `showingId` 跳转订票。

---

## Q2. 注册时如何校验手机号 / 邮箱？  
## How are phone number and email validated on registration?

### 涉及文件 / Files

| 层 | 文件 |
|----|------|
| UI | [`RegisterView.java`](../../src/main/java/com/hcbs/web/auth/RegisterView.java) |
| DTO | [`RegistrationRequest.java`](../../src/main/java/com/hcbs/dto/RegistrationRequest.java) |
| 校验 + 入库 | [`RegistrationService.java`](../../src/main/java/com/hcbs/service/auth/RegistrationService.java) |
| 手机工具 | [`PhoneNumbers.java`](../../src/main/java/com/hcbs/util/PhoneNumbers.java) |
| 持久化 | [`UserRepository.java`](../../src/main/java/com/hcbs/repository/UserRepository.java) |

### 流程 / Flow

1. 用户填表 → 点 **Create account** → [`RegisterView.submit()`](../../src/main/java/com/hcbs/web/auth/RegisterView.java) 组装 `RegistrationRequest`
2. 调用 `registrationService.registerCustomer(request)`
3. `validate(request)` 抛 `IllegalArgumentException` → UI `Notification.show(message)`
4. 通过则 BCrypt 加密密码 → `userRepository.save(user)` → 跳转 `login?registered`

### 手机号 / Phone

**规范化：** [`PhoneNumbers.normalize`](../../src/main/java/com/hcbs/util/PhoneNumbers.java) — trim，去掉空格和 `-`

**格式校验：**

```7:25:src/main/java/com/hcbs/util/PhoneNumbers.java
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9][0-9\\s-]{6,18}$");
    // ...
    public static boolean isValid(String phone) {
        String normalized = normalize(phone);
        return normalized != null && PHONE_PATTERN.matcher(normalized).matches();
    }
```

**业务规则（RegistrationService）：**

```57:66:src/main/java/com/hcbs/service/auth/RegistrationService.java
        String phone = PhoneNumbers.normalize(request.phone());
        if (phone == null) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (!PhoneNumbers.isValid(phone)) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
```

**登录也用同一手机号：** [`LoginView.submitLogin()`](../../src/main/java/com/hcbs/web/auth/LoginView.java) → `PhoneNumbers.normalize` → `HttpServletRequest.login(phone, password)` → [`HcbsUserDetailsService.loadUserByUsername`](../../src/main/java/com/hcbs/security/HcbsUserDetailsService.java) 按 phone 查用户。

### 邮箱 / Email（可选 optional）

```68:76:src/main/java/com/hcbs/service/auth/RegistrationService.java
        String email = User.normalizeEmail(request.email());
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Email format is invalid");
            }
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Email is already registered");
            }
        }
```

正则 / Pattern: `^[^@\s]+@[^@\s]+\.[^@\s]+$`；存库前 `trim().toLowerCase()` — [`User.normalizeEmail`](../../src/main/java/com/hcbs/model/User.java)

### 其他校验 / Other rules

| 字段 Field | 规则 Rule |
|------------|-----------|
| username | 正则 `^[a-zA-Z0-9._-]{3,32}$`，唯一 |
| password | ≥ 8 字符，与 confirm 一致 |
| fullName | 必填，≤ 100 字符 |
| role | 固定 `CUSTOMER`（员工由管理员创建） |

---

## Q3. 选座是单独页面还是弹窗？优缺点？  
## Seat selection: separate page or popup? Pros and cons?

### 本项目答案 / Our answer：**单独页面 Separate route page**

**路由 / Route：** [`BookingView`](../../src/main/java/com/hcbs/web/booking/BookingView.java) — `@Route(value = "booking", layout = MainLayout.class)`

**不是弹窗 / Not a dialog：** 座位图 [`SeatMapPicker`](../../src/main/java/com/hcbs/web/booking/component/SeatMapPicker.java) 嵌入 BookingView 页面内，非 `Dialog` 组件。

### 进入方式 / How users arrive

1. 首页筛选 → 详情 [`FilmDetailView`](../../src/main/java/com/hcbs/web/home/FilmDetailView.java) → Grid 点 **Book**
2. URL：`/booking?showingId=123`（可保留筛选 query）

```117:124:src/main/java/com/hcbs/web/home/FilmDetailView.java
        showings.addComponentColumn(row -> {
            Button book = new Button("Book");
            book.addClickListener(e -> book.getUI().ifPresent(ui -> ui.navigate(
                    BookingView.class,
                    ShowingFilterQuery.withShowingId(filter, row.showingId()))));
            return book;
        })
```

### 选座交互 / Seat interaction

```174:199:src/main/java/com/hcbs/web/booking/component/SeatMapPicker.java
    private Button seatButton(SeatMapSeat seat) {
        Button button = new Button(label);
        if (seat.available()) {
            button.addClickListener(e -> toggleSeat(seat.seatId(), button));
        }
        return button;
    }

    private void toggleSeat(Long seatId, Button button) {
        if (selectedIds.contains(seatId)) {
            selectedIds.remove(seatId);
        } else {
            selectedIds.add(seatId);
        }
        applySeatStyle(button, seat);
        fireSelectionChanged();
    }
```

### 优缺点 / Pros & cons

| | 单独页 Separate page ✅ 本项目 | 弹窗 Dialog |
|---|-------------------------------|-------------|
| **优点 Pros** | URL 可带 `showingId`，可书签/刷新；流程清晰；员工柜台与客户自助共用一页 | 少跳转，上下文不离开详情页 |
| **缺点 Cons** | 多一次路由跳转 | 状态难分享；大屏座位图空间受限；浏览器后退行为复杂 |

---

## Q4. 管理导航栏的是哪个文件？  
## Which file manages the navigation bar?

**主文件 / Main file：** [`MainLayout.java`](../../src/main/java/com/hcbs/web/shell/MainLayout.java)

继承 Vaadin `AppLayout`，提供**顶栏 navbar + 左侧 drawer + 右侧内容槽**。

| 方法 Method | 行号约 Lines | 作用 Purpose |
|-------------|--------------|--------------|
| 构造器 | 58–99 | 品牌区、顶栏、抽屉容器 |
| [`refreshHeader()`](../../src/main/java/com/hcbs/web/shell/MainLayout.java) | 115–134 | 顶栏：Account / 用户名 / Sign out |
| [`refreshDrawer()`](../../src/main/java/com/hcbs/web/shell/MainLayout.java) | 141–163 | **侧栏菜单（按角色）** |
| [`afterNavigation()`](../../src/main/java/com/hcbs/web/shell/MainLayout.java) | 102–105 | 每次路由后刷新壳层 |
| [`navLink()`](../../src/main/java/com/hcbs/web/shell/MainLayout.java) | 190–201 | `RouterLink` 绑定 `@Route` View |

**样式 / Styles：** 类名如 `topbar`, `nav-card` 在 [`frontend/themes/hcbs/styles.css`](../../src/main/frontend/themes/hcbs/styles.css)（若答辩问到 CSS）

**不使用 MainLayout 的页：** [`LoginView`](../../src/main/java/com/hcbs/web/auth/LoginView.java)、[`RegisterView`](../../src/main/java/com/hcbs/web/auth/RegisterView.java) — 全屏独立认证页

**全局壳配置 / App shell：** [`AppShell.java`](../../src/main/java/com/hcbs/web/shell/AppShell.java) — 主题、PWA 元数据

---

## Q5. Admin 和 Customer 是否分开？看到不同页面吗？  
## Are Admin and Customer separate? Different views?

### 账号层面 / Accounts

**同一张表 `app_user`，用 `UserRole` 区分：**

```3:7:src/main/java/com/hcbs/model/UserRole.java
public enum UserRole {
    CUSTOMER,
    BOOKING_STAFF,
    ADMIN;
```

- **Customer**：[`RegisterView`](../../src/main/java/com/hcbs/web/auth/RegisterView.java) 自助注册
- **Staff / Admin**：种子数据或 [`AdminDataView`](../../src/main/java/com/hcbs/web/admin/AdminDataView.java) 管理（非公开注册）

### 菜单不同 / Different menus — `MainLayout.refreshDrawer()`

```141:162:src/main/java/com/hcbs/web/shell/MainLayout.java
    private void refreshDrawer() {
        drawerContent.add(navLink("Home", FilmRecommendView.class, ...));
        if (... isEmployee()) {
            drawerContent.add(navLink("Book tickets", BookingView.class, ...));
        }
        if (... isAuthenticated()) {
            if (user.getRole().isCustomer()) {
                drawerContent.add(navLink("My bookings", MyBookingsView.class, ...));
            } else {
                drawerContent.add(navLink("Cancellation", CancellationView.class, ...));
                if (user.getRole().canAccessAdminTools()) {
                    drawerContent.add(navLink("Data admin", AdminDataView.class, ...));
                }
            }
        }
    }
```

| 角色 Role | 侧栏可见 Visible in drawer | 页面权限 Page access |
|-----------|---------------------------|----------------------|
| 未登录 Guest | Home | 首页/详情 `@AnonymousAllowed` |
| CUSTOMER | Home, My bookings | 自己的订单 |
| BOOKING_STAFF | Home, Book tickets, Cancellation | 柜台订票、退票 |
| ADMIN | 上述 + Data admin | 上述 + 数据管理 |

### 页面级权限示例 / Route-level security

```24:27:src/main/java/com/hcbs/web/cancellation/CancellationView.java
@Route(value = "cancellation", layout = MainLayout.class)
@RolesAllowed({"BOOKING_STAFF", "ADMIN"})
public class CancellationView extends VerticalLayout {
```

**同一 BookingView，UI 因角色不同：** 员工见「Customer phone」ComboBox；客户自助隐藏 — [`BookingView.buildWorkspace()`](../../src/main/java/com/hcbs/web/booking/BookingView.java) 第 92–102 行

---

## Q6. 某功能的文件结构、流程、触发事件（Login / Booking / 浏览）  
## File structure, process flow, and trigger events

### 6A. 浏览 + 筛选（成员 A）Browse + Filter

| 步骤 | 文件 | 触发事件 Trigger |
|------|------|------------------|
| 打开 `/` | [`FilmRecommendView`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) | 路由 → **`beforeEnter`** |
| 显示全部 | `displayAllFilms()` | URL 无 criteria |
| 改筛选条件 | [`AdditiveShowingFilterPanel`](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java) | **`ValueChangeListener`** |
| 搜索 | `runSearch()` | Search **click** / 片名 **Enter** |
| 查库渲染 | `displayFilteredFilms()` | `beforeEnter` 内 |
| 点海报 | [`FilmDetailView`](../../src/main/java/com/hcbs/web/home/FilmDetailView.java) | **`setParameter`** / 路由 |

### 6B. 登录 Login

| 步骤 | 文件 | 触发事件 |
|------|------|----------|
| 打开 `/login` | [`LoginView`](../../src/main/java/com/hcbs/web/auth/LoginView.java) | 路由 |
| 读 redirect | `beforeEnter` | URL `?redirect=booking?showingId=…` |
| 提交 | `submitLogin()` | **Sign in click** |
| 认证 | `HttpServletRequest.login()` | Servlet 容器 |
| 查用户 | [`HcbsUserDetailsService`](../../src/main/java/com/hcbs/security/HcbsUserDetailsService.java) | Spring Security |
| 跳转 | `ui.navigate(redirect or "")` | 登录成功 |

```101:132:src/main/java/com/hcbs/web/auth/LoginView.java
    private void submitLogin() {
        // ...
        request.getHttpServletRequest().login(PhoneNumbers.normalize(user.trim()), pass);
        request.getHttpServletRequest().changeSessionId();
        getUI().ifPresent(ui -> {
            String redirect = resolveRedirectTarget();
            ui.navigate(redirect != null ? ... : "");
        });
    }
```

### 6C. 订票 Booking

| 步骤 | 文件 | 触发事件 |
|------|------|----------|
| 进入 `/booking` | [`BookingView`](../../src/main/java/com/hcbs/web/booking/BookingView.java) | **`beforeEnter`** |
| 未登录拦截 | `beforeEnter` | `forwardTo(LoginView)` + `redirect` param |
| 构建 UI | `buildWorkspace()` | 首次进入且已登录 |
| 加载场次 | `loadShowing(showingId)` | URL `showingId` 或 ComboBox 选择 |
| 加载座位 | `seatPicker.setSeats(...)` | `BookingService.listSeatMap` |
| 选座 | [`SeatMapPicker.toggleSeat`](../../src/main/java/com/hcbs/web/booking/component/SeatMapPicker.java) | 座位 Button **click** |
| 确认 | `confirm()` | **Confirm booking click** |
| 写库 | [`BookingService.createBooking`](../../src/main/java/com/hcbs/service/booking/BookingService.java) | `@Transactional` |
| 刷新座位图 | `setSeats` again | 成功后 |

```68:87:src/main/java/com/hcbs/web/booking/BookingView.java
    public void beforeEnter(BeforeEnterEvent event) {
        Long preset = ShowingFilterQuery.showingIdFrom(...).orElse(null);
        if (!currentUserService.isAuthenticated()) {
            params.put("redirect", List.of(target));
            event.forwardTo(LoginView.class, new QueryParameters(params));
            return;
        }
        // ...
        loadShowing(preset);
    }
```

```236:265:src/main/java/com/hcbs/web/booking/BookingView.java
    private void confirm() {
        // ...
        BookingReceipt bookingReceipt = bookingService.createBooking(activeShowingId, seatIds, phone);
        receipt.setValue(bookingReceipt.toReceiptText());
        seatPicker.setSeats(bookingService.listSeatMap(activeShowingId));
    }
```

### 6D. 取消订单 Cancellation

| 步骤 | 文件 | 触发事件 |
|------|------|----------|
| 员工退票页 | [`CancellationView`](../../src/main/java/com/hcbs/web/cancellation/CancellationView.java) | `@RolesAllowed` STAFF/ADMIN |
| 客户我的订单 | [`MyBookingsView`](../../src/main/java/com/hcbs/web/cancellation/MyBookingsView.java) | CUSTOMER |
| 业务 | [`CancellationService`](../../src/main/java/com/hcbs/service/cancellation/CancellationService.java) | Cancel **click** |

### 6E. 端到端链路 / End-to-end

```
FilmRecommendView (筛选)
  → FilmDetailView (Grid + Book)
    → LoginView (若未登录)
      → BookingView (SeatMapPicker + Confirm)
        → CancellationView / MyBookingsView (Cancel)
```

---

# 思路三 · Track 3：Java 语法 / Java Syntax

---

## Q1. 某方法的返回类型是什么？  
## What is the return type of a method?

### 示例表 / Examples

| 方法 Method | 文件 | 返回类型 Return type | 中文说明 |
|-------------|------|---------------------|----------|
| `getFilter()` | [`AdditiveShowingFilterPanel`](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java) | `ShowingListingFilter` | 筛选 DTO |
| `searchFilmsByShowings()` | [`HcbsSearchService`](../../src/main/java/com/hcbs/service/search/HcbsSearchService.java) | `List<FilmCardDto>` | 海报卡片列表 |
| `registerCustomer()` | [`RegistrationService`](../../src/main/java/com/hcbs/service/auth/RegistrationService.java) | `User` | 持久化后的用户实体 |
| `createBooking()` | [`BookingService`](../../src/main/java/com/hcbs/service/booking/BookingService.java) | `BookingReceipt` | 订票回执 DTO |
| `toQueryParameters()` | [`ShowingFilterQuery`](../../src/main/java/com/hcbs/web/home/ShowingFilterQuery.java) | `QueryParameters` | Vaadin URL 参数 |
| `beforeEnter()` | [`FilmRecommendView`](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) | `void` | 路由生命周期，无返回值 |
| `loadUserByUsername()` | [`HcbsUserDetailsService`](../../src/main/java/com/hcbs/security/HcbsUserDetailsService.java) | `UserDetails` | Spring Security 用户详情 |
| `listCities()` | [`FilmListingService`](../../src/main/java/com/hcbs/service/listing/FilmListingService.java) | `List<CityOption>` | 城市下拉数据 |

**代码 — `createBooking` 签名：**

```161:162:src/main/java/com/hcbs/service/booking/BookingService.java
    @Transactional
    public BookingReceipt createBooking(Long showingId, List<Long> seatIds, String customerPhone) {
```

---

## Q2. 某方法的参数是什么？  
## What are the parameters of a method?

| 方法 Method | 参数 Parameters | 含义 Meaning |
|-------------|-----------------|--------------|
| `createBooking(showingId, seatIds, customerPhone)` | `Long`, `List<Long>`, `String` | 场次 id、座位 id 列表、顾客手机（员工柜台必填） |
| `searchFilmsByShowings(filter)` | `ShowingListingFilter filter` | 城市/影院/日期/片名条件 |
| `fromQueryParameters(queryParameters)` | `QueryParameters queryParameters` | 浏览器 URL 查询参数 |
| `AdditiveShowingFilterPanel(searchService, onSearch, onClear)` | `HcbsSearchService`, `Runnable`, `Runnable` | 服务 + 搜索/清空回调 |
| `setParameter(event, filmId)` | `BeforeEvent`, `Long` | 详情页 URL 路径参数 `/film/{id}` |
| `loadUserByUsername(loginPhone)` | `String loginPhone` | 登录用手机号（Spring Security username） |
| `registerCustomer(request)` | `RegistrationRequest request` | 注册表单 DTO |

**`ShowingListingFilter` record 组件 / record components:**

```9:9:src/main/java/com/hcbs/dto/ShowingListingFilter.java
public record ShowingListingFilter(Long cityId, Long cinemaId, LocalDate date, String filmTitle) {
```

---

## Q3. Lambda 表达式是什么？代码里哪里有？  
## What is a lambda expression? Where is it used?

### 定义 / Definition

**中文：** Lambda 是**匿名函数**的简写，用于实现只有一个抽象方法的**函数式接口**（如 `Runnable`、`ValueChangeListener`、`Consumer`）。

**English:** A lambda is shorthand for an anonymous function passed to a functional interface (single abstract method).

语法 / Syntax: `(parameters) -> { body }` 或 `(parameters) -> expression`

### 项目中的例子 / Examples in this project

**① 筛选 — ComboBox 值变化（成员 A）**

```53:58:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
        city.addValueChangeListener(event -> {
            Long cityId = event.getValue() == null ? null : event.getValue().cityId();
            cinema.setItems(searchService.listCinemas(cityId));
            cinema.clear();
            refreshActiveFilters();
        });
```

**② 方法引用 — 传回调给面板（成员 A）**

```52:52:src/main/java/com/hcbs/web/home/FilmRecommendView.java
        this.filterPanel = new AdditiveShowingFilterPanel(searchService, this::runSearch, this::showAllFilms);
```

`this::runSearch` ≡ `() -> this.runSearch()`

**③ 订票 — Confirm 按钮**

```129:129:src/main/java/com/hcbs/web/booking/BookingView.java
        Button confirm = new Button("Confirm booking", e -> confirm());
```

**④ 导航 — Account 按钮**

```117:117:src/main/java/com/hcbs/web/shell/MainLayout.java
        Button account = new Button("Account", event -> openAccountCenter());
```

**⑤ 座位图 — 场次 chip 点击**

```78:81:src/main/java/com/hcbs/web/booking/component/SeatMapPicker.java
            chip.addClickListener(e -> {
                if (onShowtimeSelected != null) {
                    onShowtimeSelected.accept(option.showingId());
                }
            });
```

**⑥ Stream + Lambda — Grid 列、筛选回填**

```114:115:src/main/java/com/hcbs/web/home/FilmDetailView.java
        showings.addColumn(row -> row.showDate().format(DATE_FORMAT)).setHeader("Date")
```

```140:143:src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java
            city.setValue(searchService.listCities().stream()
                    .filter(option -> option.cityId().equals(filter.cityId()))
                    .findFirst()
                    .orElse(null));
```

**⑦ 注册 — 提交按钮**

```72:72:src/main/java/com/hcbs/web/auth/RegisterView.java
        Button submit = new Button("Create account", event -> submit());
```

---

# 附录 · Appendix

## A. 成员 A 3 分钟话术（中英） / Member A 3-minute script

| # | 中文 | English |
|:-:|------|---------|
| 1 | 我负责首页和筛选面板。 | I own the home page and filter panel. |
| 2 | Vaadin 部件：`ComboBox` 城市下拉，级联影院。 | Vaadin: `ComboBox` for city with cascading cinemas. |
| 3 | 筛选：四条件叠加；Search → DTO → URL → `beforeEnter` → 查场次 → `FilmCardDto` 海报。 | Filter: additive criteria; Search → DTO → URL → `beforeEnter` → showings → poster cards. |
| 4 | 演示：选条件 → Search → 复制 URL → Clear。 | Demo: filter → Search → copy URL → Clear. |

## B. 全项目演示顺序 / Full demo order（3–5 分钟）

1. **`/`** — 筛选 + 海报（A）
2. **点影片** — `FilmDetailView` Grid
3. **Book** — 登录（若需要）→ `BookingView` 选座 Confirm
4. **侧栏 Cancellation** — 员工搜手机号退票
5. **提 MainLayout** — 角色菜单不同

## C. 文档索引 / Doc index

| 模块 | README |
|------|--------|
| Home | [`home/README.md`](../../src/main/java/com/hcbs/web/home/README.md) |
| Booking | [`booking/README.md`](../../src/main/java/com/hcbs/web/booking/README.md) |
| Auth | [`auth/README.md`](../../src/main/java/com/hcbs/web/auth/README.md) |
| Shell | [`shell/README.md`](../../src/main/java/com/hcbs/web/shell/README.md) |
| 答辩速查 | [`答辩指南.md`](../../src/main/java/com/hcbs/web/答辩指南.md) |
| A 阅读顺序 | [`MEMBER_A_影片浏览.md`](./MEMBER_A_影片浏览.md) |
