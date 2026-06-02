# shell — 全局壳层

答辩高频问题：**「管理导航栏的文件是哪个？」** → 就是本文件夹的 `MainLayout.java`。

## 文件清单

| 文件 | 作用 |
|------|------|
| `AppShell.java` | 注册 Vaadin 主题名 `hcbs` |
| `MainLayout.java` | 顶栏 + 侧栏菜单，所有带 `@Route(..., layout = MainLayout.class)` 的页面共用 |

---

## AppShell.java

```java
@Theme("hcbs")
public class AppShell implements AppShellConfigurator { }
```

- 只有一行有效配置：`@Theme("hcbs")` 把应用绑定到 `frontend/themes/hcbs/styles.css`。
- 测试类 `UiThemeTest` 会断言主题名为 `hcbs`。
- **答辩可说：** Vaadin 通过 AppShell 加载自定义 CSS，不需要手写 HTML 模板。

---

## MainLayout.java

继承 `AppLayout`（Vaadin 内置的应用布局：上方导航栏 + 左侧抽屉）。

### 依赖注入

| 服务 | 用途 |
|------|------|
| `CurrentUserService` | 判断当前是否登录、角色是谁 |
| `AuthUiService` | 登出操作 |

### 页面结构

```text
┌─────────────────────────────────────────────┐
│ topbar: [≡] Horizon Cinemas    [Account][登出] │  ← addToNavbar
├──────────┬──────────────────────────────────┤
│  Menu    │                                  │
│  Home    │   子页面内容区                    │  ← addToDrawer + @Route 内容
│  Book…   │   (FilmRecommendView 等)         │
└──────────┴──────────────────────────────────┘
```

### 关键方法

#### 1. 构造器 — 搭建顶栏

- `DrawerToggle`：汉堡按钮，展开/收起侧栏。
- `brand-lockup`：品牌区（HC 标记 + 标题 + 副标题）。
- `headerActions`：右侧 Account / 用户名 / Sign out。

#### 2. `afterNavigation()` — 路由切换后刷新

每次用户跳转到新页面，Vaadin 会调用此方法 → 执行 `refreshChrome()`，保证登录状态变化后侧栏立刻更新。

#### 3. `refreshDrawer()` — 侧栏菜单（答辩重点）

```java
drawerContent.add(navLink("Home", FilmRecommendView.class, "..."));

if (已登录 && 是员工) {
    drawerContent.add(navLink("Book tickets", BookingView.class, "..."));
}

if (已登录) {
    if (是客户) {
        drawerContent.add(navLink("My bookings", MyBookingsView.class, "..."));
    } else {
        drawerContent.add(navLink("Cancellation", CancellationView.class, "..."));
        if (是 ADMIN) {
            drawerContent.add(navLink("Data admin", AdminDataView.class, "..."));
        }
    }
}
```

**答辩话术：** 不同角色看到不同菜单；客户走自助（My bookings），员工走柜台（Book + Cancellation），管理员额外有 Data admin。

#### 4. `navLink()` — 生成侧栏卡片

使用 Vaadin 的 `RouterLink`：
- `link.setRoute(SomeView.class)` 绑定路由
- 添加 CSS class `nav-card`、`nav-title`、`nav-caption`

#### 5. `openAccountCenter()` — Account 按钮

- 已登录 → 跳转 `AccountCenterView`
- 未登录 → 跳转 `LoginView`

### 使用的 Vaadin 组件

| 组件 | 在哪用 |
|------|--------|
| `AppLayout` | 整体布局基类 |
| `DrawerToggle` | 侧栏开关 |
| `RouterLink` | 侧栏导航链接 |
| `Button` | Account、Sign out |
| `HorizontalLayout` / `VerticalLayout` | 顶栏、抽屉排版 |

### 与其他板块的交互

- **home / booking / cancellation / admin** 的 View 都在 `@Route` 上写 `layout = MainLayout.class`，内容渲染在 MainLayout 右侧主区域。
- **auth** 的 LoginView、RegisterView **没有** MainLayout，全屏独立页。
