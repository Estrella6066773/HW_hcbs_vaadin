# 角色贡献说明（快速上手）

本目录为四人分工各备一份**独立说明**。总表与答辩安排见 [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md)。

**分工原则：** 按**功能纵向主责**答辩（每人讲一块完整用户故事 + 自己模块的测试）；开发合并时仍遵守 [ARCHITECTURE.md](../ARCHITECTURE.md) 分层依赖。

| 成员 | 答辩主责 | 文档 | 一句话 |
| --- | --- | --- | --- |
| **A** | Film Listing + 场馆/价格 | [MEMBER_A_持久化与数据.md](MEMBER_A_持久化与数据.md) | 列表、筛选、详情、价目与场次数据 |
| **B** | Booking / 订票 | [MEMBER_B_应用服务.md](MEMBER_B_应用服务.md) | 订票规则、座位图、收据 |
| **C** | Cancellation + My Bookings | [MEMBER_C_Web界面.md](MEMBER_C_Web界面.md) | 取消规则、员工/客户订单页 |
| **D** | Login / Register / Role / Nav + Admin | [MEMBER_D_测试与交付.md](MEMBER_D_测试与交付.md) | 登录注册、角色导航、管理页、用例汇总与提交 |

**阅读顺序：** 本角色文档 → [ARCHITECTURE.md](../ARCHITECTURE.md) → [README_CN.md](../README_CN.md) → [CONTRIBUTION_MATRIX.md](../CONTRIBUTION_MATRIX.md) §6（答辩底线与演示顺序）。

**组内目标：** 每人总体贡献约 **25%**；修改他人功能主责范围内的代码须走 Review。

**全组 1 页小抄（答辩前必背）：** Vaadin/Spring Boot 各一句；自己功能的 `View→Service→Repository→Entity`；`@Entity`/`@Table`/`@Column`；Repository 作用；一个 Vaadin 组件例子。详见贡献矩阵 §6.1。
