# 成员 C — Web 界面与主题

**角色：** Web 界面与前端主题  
**组内目标：** 约 25%（Exercise 2 组内约 28%）  
**负责目录：** `src/main/java/com/hcbs/web/**`、`frontend/themes/hcbs/**`  
**合并前须请谁 Review：** B（Service API）、D（UI 测试与用例反馈）

---

## 1. 你在项目中的位置

```text
浏览器 → Vaadin View (你) → Service (B) → DTO
                ↓
         styles.css 主题
```

你只负责**展示与交互**：绑定组件、路由、布局、样式。**不算价、不查库、不判断能否取消**——这些一律调用 B 的 Service。

---

## 2. 核心交付清单

| 编号 | 工作项 | 主要文件 | 路由 |
| --- | --- | --- | --- |
| C1 | 影片列表 / 主页 | `FilmRecommendView` | `/` |
| C2 | 订票页 | `BookingView` | `/booking` |
| C3 | 取消页 | `CancellationView` | `/cancellation` |
| C4 | 布局与导航 | `MainLayout`、`AppShell` | 全局 |
| C5 | 主题样式 | `frontend/themes/hcbs/styles.css` | — |
| C6 | 功能说明（界面流程） | 与 [README_CN.md](../README_CN.md) 一致 | — |
| C7 | 联调与文案 | 错误提示、`Notification` | — |
| C8 | 影片封面展示 | `FilmPoster` 组件、`styles.css` 中 `.film-poster-missing` | — |

> 说明：早期矩阵中的 `FilmListingView` 已合并进主页 `FilmRecommendView`；列表能力由 `AdditiveShowingFilterPanel` + 搜索服务完成。

---

## 3. 路由与页面对照表

| 路由 | 类 | 布局 | 主要注入 | 说明 |
| --- | --- | --- | --- | --- |
| `/` | `FilmRecommendView` | `MainLayout` | `HcbsSearchService`、`PosterResourceService` 等 | 默认海报墙；搜索后显示统计与场次表 |
| `/film/:id` | `FilmDetailView` | `MainLayout` | `HcbsSearchService`、`PosterResourceService` | 简介、演员、场次列表 |
| `/booking` | `BookingView` | `MainLayout` | **`BookingService`** | 场次、座位图、收据 |
| `/cancellation` | `CancellationView` | `MainLayout` | **`CancellationService`** | 参考号查询与取消 |
| `/my-bookings` | `MyBookingsView` | `MainLayout` | `CancellationService` | 客户订单列表 |
| `/login` | `LoginView` | 无侧栏 | Spring Security | 演示账号列表 |
| `/register` | `RegisterView` | 无 | `RegistrationService` | 客户注册 |
| `/logout` | `LogoutView` | — | — | 登出 |
| `/account` | `AccountCenterView` | `MainLayout` | 用户相关 | 账户中心 |
| `/admin` | `AdminDataView` | `MainLayout` | `AdminCatalogService` | 员工数据管理（扩展） |

**Shell：** `AppShell.java` 注册主题名 `hcbs`；`@Theme` 与 `styles.css` 关联。

---

## 4. 共享组件（`web/component`）

| 组件 | 用途 | 使用方 |
| --- | --- | --- |
| `FilmPoster` | 按 DB 路径显示本地 JPEG；文件缺失时显示「缺失」 | `FilmRecommendView`、`FilmDetailView` |
| `AdditiveShowingFilterPanel` | 城市/影院/日期/片名叠加筛选 | `FilmRecommendView` |
| `SeatMapPicker` | 座位图勾选、区域切换 | `BookingView` |
| `PageHero` | 页头标题区 | 多页 |
| `BackToHomeAction` | 返回主页按钮 | 详情、订票等 |

改筛选 UI → 确认仍产出 `ShowingListingFilter` 或 B 约定的参数；改座位图 → 与 `BookingService.loadSeatMap` 返回的 `SeatMapSeat` 字段一致。

### 4.1 影片封面（`FilmPoster`）

1. 注入 `PosterResourceService`（与 `HcbsSearchService` 一起在 View 构造器中）。  
2. `FilmCardDto.posterUrl()` / `FilmDetailDto.posterUrl()` 来自数据库，**不要在 View 里拼路径或生成图**。  
3. `new FilmPoster(posterResources, url, altText[, classNames...])`：有文件则 `<img>`，否则灰色虚线框 + **「缺失」**。  
4. 样式：`.film-poster-slot`、`.film-poster-image`、`.film-poster-missing`（见 `styles.css`）。  
5. 演示缺失：种子影片 **Solitude** 的路径为 `/images/posters/solitude.jpg`，仓库未附带该文件。

---

## 5. 三个核心页面流程（答辩演示用）

### 5.1 主页 `FilmRecommendView`（TC_001、TC_002）

1. 初始：影片卡片/海报。  
2. 用户设置筛选 → 调用搜索 → 展示 `ShowingRow` 表格（片名、影院、时间、余座等）。  
3. 可跳转详情 `/film/{id}` 或带参数进订票 `/booking?showingId=...`。

**你只负责：** 表单、表格列、`Grid`/`ComboBox` 绑定；**不负责** JPQL 与余座 SQL。

### 5.2 订票 `BookingView`（TC_003、TC_004）

1. 选场次（`ShowingOption`）→ 加载 `BookingShowingContext`、座位图。  
2. 员工角色：显示客户手机 `ComboBox`（`searchCustomerPhones`）。  
3. 勾选座位 → `BookingService.createBooking` → `BookingReceipt` 填入 `TextArea`。  
4. 异常：`catch` 后 `Notification.show(e.getMessage())`，不解析业务细节。

**关键依赖：** 仅 `BookingService` + `CurrentUserService`（判断员工/客户 UI），**禁止** `import com.hcbs.repository`。

### 5.3 取消 `CancellationView`（TC_008–TC_011）

1. 输入参考号 → `findBookingSummary`。  
2. 展示是否可取消、50% 手续费说明（文案来自 Service 状态或你根据 `canCancel` 显示）。  
3. 确认 → `cancelBooking` → 刷新摘要。

---

## 6. `MainLayout` 与导航

- **顶栏：** 品牌、登录状态、`AuthUiService` 按钮。  
- **侧栏：** 按角色显示链接（主页、订票、取消、我的订单、管理、登录/登出）。  
- **`afterNavigation`：** 刷新侧栏与副标题（`resolveSubtitle()`）。

改导航项时同步 [README_CN.md](../README_CN.md) 中的路由表，并知会 D（若有 UI 测试）。

---

## 7. 主题 `frontend/themes/hcbs/styles.css`

| 区域 | 典型 class | 说明 |
| --- | --- | --- |
| 壳层 | `.hcbs-shell`、`.topbar`、`.brand-lockup` | 整体布局 |
| 主页 | 海报墙、统计卡片、场次表 | 与 `FilmRecommendView` 的 class 对应 |
| 订票 | 座位图、收据区 | `SeatMapPicker` 相关样式 |
| 表单 | Vaadin Lumo 变量覆盖 | 保持课程演示一致的风格 |

**测试：** `UiThemeTest` 断言主题名为 `hcbs`（D 维护，你改主题后须保证通过）。

修改 Vaadin 版本或主题后执行：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" vaadin:prepare-frontend
```

---

## 8. 依赖规则（违反即 Request Changes）

1. **允许：** `com.hcbs.service.*`、`com.hcbs.dto.*`、`com.hcbs.security.*`（当前用户）、`com.hcbs.model.SeatArea`（订票 UI 枚举展示）。  
2. **禁止：** `com.hcbs.repository.*`、在 View 里 `new` JPA 实体并保存。  
3. **禁止：** 在 View 写 `totalCost * 0.5`、7 日日期判断等——一律问 B 要结果。

---

## 9. 协作边界

| 同事 | 你需要什么 |
| --- | --- |
| **B** | 稳定 Service 方法签名；异常信息对用户友好；新字段先出现在 DTO |
| **A** | 一般不直接联系；表结构问题通过 B |
| **D** | 按 TC 做手工测试；`UiThemeTest` 失败时你查主题名与路径 |

**答辩时建议操作：** 由你启动浏览器（或组内约定你操作），依次演示 `/` → `/booking` → `/cancellation`。

---

## 10. 本地运行

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

控制台会打印实际端口（8080 或 fallback）。演示账号见 `LoginView` / [README_CN.md](../README_CN.md) §演示数据。

---

## 11. 答辩 3–5 分钟提纲

1. **三个核心 View 的用户路径**（列表 → 订 → 取消）。  
2. **为何只注入 Service**（分层、可测、实体不泄漏）。  
3. **`MainLayout` + `styles.css`** 如何统一品牌（可开开发者工具指一个 class）。  
4. **现场演示** 一条完整订票 + 一条取消（用种子参考号 `HCBS-SEED001` 等）。  
5. 已知局限：不做支付、邮件（课程范围外）。

---

## 12. 相关文档

| 文档 | 用途 |
| --- | --- |
| [README_CN.md](../README_CN.md) §使用流程 | 与界面一致的步骤描述 |
| [MEMBER_B_应用服务.md](MEMBER_B_应用服务.md) | API 与 DTO |
| [ARCHITECTURE.md](../ARCHITECTURE.md) | Web 层依赖表 |
| [TEST_CASES.md](../TEST_CASES.md) | 手工 TC_001–TC_011 |

---

## 13. 自检清单

- [ ] 三个核心路由可访问且无控制台报错  
- [ ] View 中无 `repository`、无实体持久化  
- [ ] 筛选、订票、取消的错误提示来自 Service 异常信息  
- [ ] 改 `styles.css` 后 `UiThemeTest` 通过  
- [ ] README 中界面流程与实机一致
