# auth — 认证板块

登录、注册、账户中心。**不使用 MainLayout**（无侧栏，全屏独立页）。

## 文件清单

| 文件 | 路由 | 布局 | 说明 |
|------|------|------|------|
| `LoginView.java` | `/login` | 无 | 登录 |
| `RegisterView.java` | `/register` | 无 | 客户注册 |
| `LogoutView.java` | `/logout` | 无 | 登出（自动跳转） |
| `AccountCenterView.java` | `/account` | MainLayout | 账户入口 |

## 依赖

| 类型 | 名称 |
|------|------|
| Service | `RegistrationService`（注册） |
| Service | `CurrentUserService`、`AuthUiService` |
| Config | `DemoAccountCatalog`（演示账号） |
| Security | Spring Security + `SecurityConfig` |

---

## LoginView.java — 答辩重点

### 路由

```java
@Route("login")
@AnonymousAllowed
public class LoginView implements BeforeEnterObserver
```

`SecurityConfig` 中：`setLoginView(http, LoginView.class, "/")` 把此页注册为 Spring Security 登录页。

### 页面组件

| 组件 | 字段 |
|------|------|
| `TextField` | phone（手机号登录） |
| `PasswordField` | password |
| `Button` | Sign in |
| `Anchor` | 跳转 register |
| `BackToHomeAction` | 回主页 |
| 演示账号列表 | 从 `DemoAccountCatalog.all()` 生成 |

### 登录流程 `submitLogin()`（答辩必讲）

```text
1. 校验非空
2. VaadinServletRequest.getHttpServletRequest().login(phone, password)
   └── Spring Security 验证（HcbsUserDetailsService 查库）
3. changeSessionId() 防会话固定攻击
4. navigate(redirect 或 首页)
```

**关键代码：**

```java
request.getHttpServletRequest().login(PhoneNumbers.normalize(user.trim()), pass);
request.getHttpServletRequest().changeSessionId();
getUI().ifPresent(ui -> ui.navigate(redirect != null ? redirect : ""));
```

失败 → `ServletException` → 显示 "Incorrect phone number or password"。

### redirect 机制 `beforeEnter()`

```java
// URL: /login?redirect=booking?showingId=456
pendingRedirect = queryParams.get("redirect")
```

来自 `BookingView` 未登录时的 `forwardTo(LoginView, redirect=booking...)`。  
登录成功后跳回订票页。

### 演示账号

密码统一：`demo`（`DemoAccountCatalog.DEMO_PASSWORD`）  
手机号按角色分组显示在页面上。

---

## RegisterView.java

### 路由

```java
@Route("register")
@AnonymousAllowed
```

### 表单字段

Username、Phone、Full name、Email（可选）、Password、Confirm password

### 注册流程 `submit()`

```java
RegistrationRequest request = new RegistrationRequest(...);
registrationService.registerCustomer(request);
ui.navigate("login?registered");
```

- 校验（手机格式、密码长度等）在 **RegistrationService**
- 失败 → `Notification.show(ex.getMessage())`

### 限制

页面文案说明：**仅客户可自助注册**，员工账号由管理员创建。

---

## LogoutView.java

```java
@Route("logout")
public class LogoutView implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        authUiService.signOut();
    }
}
```

进入 `/logout` 即执行登出，无可见 UI。

---

## AccountCenterView.java

### 路由

```java
@Route(value = "account", layout = MainLayout.class)
@AnonymousAllowed   // 未登录也能进，显示 guest 面板
```

### 两种面板

#### guestPanel（未登录）

Sign in → `LoginView`  
Register → `RegisterView`

#### signedInPanel（已登录）

显示用户名，按角色给快捷按钮：

| 角色 | 按钮 |
|------|------|
| CUSTOMER | My bookings、Book tickets |
| STAFF | Booking desk、Cancellation desk |
| ADMIN | 上述 + Data admin |

Sign out → `authUiService.signOut()`

### 与 MainLayout 的关系

顶栏 **Account** 按钮：
- 未登录 → LoginView
- 已登录 → AccountCenterView

---

## 答辩：Login 实现话术

> 登录页是 Vaadin View，不自己查 UserRepository。用户点 Sign in 后，我们调用 Servlet 标准 API HttpServletRequest.login，交给 Spring Security 和 HcbsUserDetailsService 验证。成功后 changeSessionId 并 navigate 到 redirect 参数指定的页面，比如从订票页被拦下来登录后会回到 booking。这是 Vaadin 与 Spring Security 集成的标准做法。

---

## 与其他板块的交互

```text
booking/BookingView (未登录)
  ──forwardTo──→ auth/LoginView (?redirect=booking)
  ←──login 成功──

shell/MainLayout
  ──Account 按钮──→ auth/AccountCenterView 或 LoginView
  ──Sign out──→ auth/AuthUiService.signOut()

config/SecurityConfig
  ──setLoginView──→ auth/LoginView
```

---

## 使用的 Vaadin 组件汇总

| 组件 | 文件 |
|------|------|
| `TextField` / `PasswordField` | Login、Register |
| `EmailField` | Register |
| `FormLayout` | Register |
| `Button` | 各页 |
| `Anchor` | 页间链接 |
| `Notification` | 注册成功、错误 |
