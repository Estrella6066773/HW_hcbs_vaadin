# 开发环境问题处理（非正式版）

本仓库当前为**课程/开发构建**，不是对外发布的正式版。本地 H2 文件库 `./data/` 仅用于开发，可随时清空重建。

---

## 核心原则（请务必遵守）

当异常明显与**数据格式、种子结构或本地库文件**有关时（而不是业务代码逻辑写错），例如：

| 典型报错或现象 | 常见原因 |
| --- | --- |
| `Database may be already in use` / `Locked by another computer`（H2 **90020**） | 陈旧 `hcbs.lock.db`、多实例占库、OneDrive 同步了锁文件 |
| `The file is locked: .../hcbs.mv.db` | 未退出的 Java 进程、DevTools 热重启残留 |
| 枚举/列与实体不一致、旧座位编号（`L*`/`U*`）仍存在 | 改过 `SeatArea`、座位网格或种子规则后**未删旧库** |
| 启动后座位数量、票价规则与文档不符 | 本地 `hcbs.mv.db` 仍是旧版本灌入的数据 |

**处理方式：**

1. **停止**所有 HCBS / `java` 进程（IDE 停止运行、结束多余 `java.exe`）。
2. **删除**本地数据文件（不要为此去改业务代码迁就旧库）：
   - `./data/hcbs.lock.db`
   - `./data/hcbs.mv.db`
   - 如有 `./data/hcbs.trace.db` 一并删除  
   - 或直接删除整个 `./data/` 目录
3. **重新启动**应用（`spring-boot:run` 或 IDE）。  
   - `spring.jpa.hibernate.ddl-auto=create` 会建表  
   - `HcbsTestDataSeeder` 会按**当前代码**重新灌入测试数据  

**不要做的事：** 在「非正式版」阶段，不要为了兼容本机残留的旧 `hcbs.mv.db` 去改实体、种子或订票逻辑；应删库后让种子与代码一致。

正式版上线前应改用独立数据库服务（如 PostgreSQL），并制定迁移脚本，而不是继续依赖删除 `./data/`。

---

## 推荐命令（Windows PowerShell）

```powershell
# 在项目根目录执行；先确保应用已停止
Remove-Item ".\data\hcbs.lock.db" -Force -ErrorAction SilentlyContinue
Remove-Item ".\data\hcbs.mv.db" -Force -ErrorAction SilentlyContinue
Remove-Item ".\data\hcbs.trace.db" -Force -ErrorAction SilentlyContinue
```

然后重新运行：

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

---

## 相关说明位置

| 位置 | 内容 |
| --- | --- |
| [TEST_DATABASE.md](TEST_DATABASE.md) §8 | 重置 H2、控制台 JDBC |
| [README.md](../README.md) · [README_CN.md](README_CN.md) | 运行前文件锁提示 |
| `application.properties` | 数据源路径旁的开发备注 |
| `HcbsDatabaseLockFailureAnalyzer` | 启动失败时的控制台说明（90020 等） |

修改座位网格、种子或票价规则后，请同步更新 `HcbsTestDataSeeder` 与 [TEST_DATABASE.md](TEST_DATABASE.md)，并**删库重启**验证。
