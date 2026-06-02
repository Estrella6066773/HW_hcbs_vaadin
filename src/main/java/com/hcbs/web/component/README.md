# component — 跨板块共享组件

本文件夹放**多个菜单板块都会用到**的组件。板块专用组件放在各子包的 `component/` 下（如 `home/component`、`booking/component`）。

## 文件清单

| 文件 | 说明 |
|------|------|
| `PageHero.java` | 页头标题区（eyebrow + 标题 + 描述） |

---

## PageHero.java

### 类定义

```java
public class PageHero extends Div
```

继承 Vaadin 的 `Div`（普通 HTML div 容器）。

### 构造参数

```java
new PageHero(String eyebrow, String heading, String copy)
```

| 参数 | 示例 | 渲染 |
|------|------|------|
| eyebrow | "Films" | 小标签 `<span class="eyebrow">` |
| heading | "Home" | 大标题 `<h2 class="page-hero-heading">` |
| copy | "Browse films..." | 描述 `<p class="page-hero-copy">` |

### 使用位置

| 板块 | View | eyebrow 示例 |
|------|------|-------------|
| home | FilmRecommendView | "Films" |
| booking | BookingView | "Employee desk" / "Self-service" |
| cancellation | CancellationView | "Refund control" |
| auth | LoginView, RegisterView | "Sign in" / "Customer" |

### 为什么抽成共享组件？

- 各页页头结构一致，避免重复 new Span/H2/Paragraph
- CSS class 统一（`page-hero`），改样式一处生效
- 答辩时可说：**复用组件减少重复、保证 UI 一致**

### 样式

在 `frontend/themes/hcbs/styles.css` 搜索：
- `.page-hero`
- `.eyebrow`
- `.page-hero-heading`
- `.page-hero-copy`

---

## 组件分包原则

```text
web/component/          ← 2+ 个板块共用（PageHero）
web/home/component/     ← 仅 home/auth 用（FilmPoster、FilterPanel）
web/booking/component/  ← 仅 booking 用（SeatMapPicker）
```

答辩时说明分包逻辑：**按菜单板块组织 View，组件跟板块走或提升到共享层**。

---

## 答辩：讲 Vaadin 组件的备选

若老师指定要讲「内置组件」而非自定义组件，可改讲：

| 组件 | 文件 | 说明 |
|------|------|------|
| `Grid` | FilmDetailView、CancellationView | 数据表格 |
| `ComboBox` | BookingView、FilterPanel | 下拉选择 |
| `Button` | 各处 | 点击事件 addClickListener |
| `Notification` | BookingView | 轻提示 |

若允许讲**自定义组件**，优先 **SeatMapPicker**（见 booking/component/README.md）。
