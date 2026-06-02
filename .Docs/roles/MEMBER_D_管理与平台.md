# 成员 D — 模块 4：数据管理 · 平台基础

**路由：** `/admin`  
**组内目标：** 约 25%（提交协调 + 平台层）  
**完整文件列表：** [四人分工.md §成员 D](../四人分工.md#成员-d--模块-4数据管理--平台基础)

---

## 1. 模块职责

负责「Admin 后台 + 应用能跑起来」**端到端**：

```text
AdminDataView → AdminCatalogService → Film/Showing/User …
DataLoader / HcbsTestDataSeeder → 演示数据库
HcbsPortAllocator / StartupAccessLogger → 端口与启动体验
```

---

## 2. 核心文件

| 类型 | 文件 |
| --- | --- |
| Service | `AdminCatalogService` |
| View | `AdminDataView`（约 450 行，本模块最大页面） |
| 种子 | `DataLoader`, `HcbsTestDataSeeder` |
| 平台 | `HcbsPortAllocator`, `StartupAccessLogger`, `HcbsDatabaseLockFailureAnalyzer` |
| Test | `AdminCatalogServiceTest`, `DataLoaderTest`, `HcbsPortAllocatorTest`, `UiThemeTest` |

---

## 3. 额外任务

- 编写 zip 内 **HOW_TO_RUN.txt**  
- 汇总 **[CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md)** 并签字  
- 协调 **Group_No.zip** 与答辩计时  

---

## 4. 答辩

**开场（~2 min）：** 架构一句 + 控制台启动横幅 + 种子数据作用  

**收尾（~3 min）：** Admin 登录 → 周排片 → 添加/编辑场次 → 禁用用户  

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test -Dtest=AdminCatalogServiceTest,DataLoaderTest,HcbsPortAllocatorTest,UiThemeTest
```

---

## 5. 协作边界

| 同事 | 交界 |
| --- | --- |
| **A** | Admin 操作 Film/Showing；A 展示列表 |
| **C** | Admin 启停 User；演示账号在 `DemoAccountCatalog`（C 维护，D 种子引用） |
| **全员** | 删 `./data/` 重置库 — 见 [DEV_TROUBLESHOOTING.md](../DEV_TROUBLESHOOTING.md) |

---

## 6. 已知 limitation（答辩可主动说）

- 无独立 Manager 视图  
- 无 Admin 报表  

---

## 7. 自检

- [ ] 能演示 Admin 排片 CRUD  
- [ ] 能说明种子数据与 Case Study 对齐  
- [ ] 4 个平台测试能讲 1–2 个  

---

## 8. 相关文档

- [admin/README.md](../../src/main/java/com/hcbs/web/admin/README.md)  
- [TEST_DATABASE.md](../TEST_DATABASE.md)
