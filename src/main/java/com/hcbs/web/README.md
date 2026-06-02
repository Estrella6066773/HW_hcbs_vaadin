# Web 界面层总览

本目录是 **Vaadin Flow** 界面代码，**按侧栏菜单分包**。  
**四人模块分工**见 [.Docs/四人分工.md](../../../../../.Docs/四人分工.md)（仓库根目录 `.Docs/`）。

## 目录与模块归属

| 文件夹 | 菜单 | 路由 | 模块主责 |
|--------|------|------|----------|
| [home/](home/README.md) | Home | `/`、`/film/:id` | **A** |
| [booking/](booking/README.md) | Book tickets | `/booking` | **B** |
| [auth/](auth/README.md) | — | `/login`、`/register` 等 | **C** |
| [cancellation/](cancellation/README.md) | Cancellation / My bookings | `/cancellation`、`/my-bookings` | **C** |
| [shell/](shell/README.md) | 全局 | — | **C** |
| [admin/](admin/README.md) | Data admin | `/admin` | **D** |
| [component/](component/README.md) | — | — | **C**（如 `PageHero`） |

## 架构分层

```text
浏览器
  ↓
Vaadin View（本目录）
  ↓
Service（com.hcbs.service.*）
  ↓
DTO（com.hcbs.dto.*）
```

**禁止：** 在 View 里 `import com.hcbs.repository.*`、在界面层写业务规则。

## 完整用户路径（答辩演示）

```text
/ 主页 (FilmRecommendView)           ← A
  → /film/123 详情 (FilmDetailView)  ← A
  → /booking?showingId=456           ← B
  → 未登录则 /login?redirect=…       ← C
/cancellation 或 /my-bookings        ← C
/admin                               ← D
```

## 角色与侧栏可见性

| 菜单项 | 未登录 | CUSTOMER | STAFF | ADMIN |
|--------|--------|----------|-------|-------|
| Home | ✓ | ✓ | ✓ | ✓ |
| Book tickets | — | — | ✓ | ✓ |
| My bookings | — | ✓ | — | — |
| Cancellation | — | — | ✓ | ✓ |
| Data admin | — | — | — | ✓ |

逻辑在 [shell/MainLayout.java](shell/MainLayout.java) 的 `refreshDrawer()`（**C** 主责）。

## 样式

主题 CSS：[frontend/themes/hcbs/styles.css](../../../../../frontend/themes/hcbs/styles.css)

## 更多文档

- [答辩指南.md](答辩指南.md)
- [.Docs/ARCHITECTURE.md](../../../../../.Docs/ARCHITECTURE.md)
- 各子包 `README.md`
