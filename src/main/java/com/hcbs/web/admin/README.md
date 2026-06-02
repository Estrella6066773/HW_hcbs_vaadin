# admin — Data admin 板块

对应侧栏 **Data admin**：Manage films and accounts。仅 **ADMIN** 角色可见。

## 文件清单

| 文件 | 路由 | 说明 |
|------|------|------|
| `AdminDataView.java` | `/admin` | 场次排片 + 用户账户管理 |

## 依赖

| Service | 用途 |
|---------|------|
| `AdminCatalogService` | 影片、场次 CRUD；用户启用/禁用 |

---

## AdminDataView.java

### 路由与安全

```java
@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
```

### 页面两大块

```text
PageHero ("Data admin")
├── filmsPanel — 场次管理
│     ├── scheduleDate DatePicker
│     ├── cinemaFilter ComboBox
│     ├── 周排片可视化 (scheduleHost)
│     └── Add / Edit / Delete showing 按钮
└── usersPanel — 账户管理
      ├── customerPhoneSearch TextField
      └── userGrid + Enable/Disable 按钮
```

---

## 场次管理

### 周排片视图 `refreshSchedule()`

1. 以 `scheduleDate` 所在周（周一到周日）为范围
2. 按选中 `cinemaFilter` 过滤影院
3. 每个 Screen 一行，每天一列，Showing 显示为色块

色块点击 → `selectedShowing = showing` → 高亮 → 可 Edit/Delete。

### 添加/编辑 Dialog `openShowingDialog()`

| 字段 | 组件 |
|------|------|
| Film | ComboBox\<FilmAdminRow\> |
| Cinema and screen | ComboBox\<Screen\> |
| Date | DatePicker |
| Start time | TimePicker |

保存调用：
- 新增：`adminCatalogService.addShowing(...)`
- 编辑：`adminCatalogService.updateShowing(...)`

### 删除 `deleteShowing()`

```java
adminCatalogService.deleteShowing(showing.showingId());
refreshSchedule();
```

### 排片块定位（CSS 百分比）

`scheduleTopPercent` / `scheduleHeightPercent` 根据 09:00–23:00 时间轴计算色块的 top 和 height，纯 UI 布局逻辑。

---

## 账户管理

### userGrid 列

Username、Phone、Email、Name、Role、Status、Registered、Action（Enable/Disable）

### 搜索

```java
customerPhoneSearch → adminCatalogService.listUsersByCustomerPhone(value)
```

### 切换状态 `toggleUser()`

```java
UserStatus next = active ? DISABLED : ACTIVE;
adminCatalogService.setUserStatus(userId, next);
```

---

## 使用的 Vaadin 组件

| 组件 | 用途 |
|------|------|
| `DatePicker` | 选排片周 |
| `ComboBox<Cinema>` | 筛选影院 |
| `Dialog` | 添加/编辑场次表单 |
| `TimePicker` | 开场时间 |
| `Grid<User>` | 用户列表 |
| `TextField` | 搜客户手机 |

---

## 答辩说明

Admin 是扩展功能，核心答辩路径仍是 **Home → Booking → Cancellation**。若被问到：

> Data admin 是 ADMIN 专属页，用 AdminCatalogService 管理场次和用户状态。View 只负责 Dialog 表单和 Grid 展示，排片冲突、删除约束等由 Service 校验，失败时 Notification 显示异常信息。

---

## 与其他板块的交互

```text
shell/MainLayout
  └── ADMIN 角色 → 侧栏 Data admin

auth/AccountCenterView
  └── ADMIN → 快捷跳转 AdminDataView

home / booking
  └── Admin 维护的场次数据 → 搜索和订票可见
```
