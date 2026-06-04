package com.hcbs.config;

import com.hcbs.model.UserRole;

import java.util.List;

/**
 * 演示账号目录（成员 C · 安全/答辩）。
 * <p>
 * 空库启动时由 {@link com.hcbs.config.HcbsTestDataSeeder} 写入；全部密码为 {@link #DEMO_PASSWORD}。
 * 每类角色 3 个账号，供 {@link com.hcbs.web.auth.LoginView#buildTestAccountPanel} 展示，
 * 答辩时演示 {@link com.hcbs.web.shell.MainLayout} 四种侧栏状态。
 */
public final class DemoAccountCatalog {

    /** 种子用户与 LoginView 页脚提示共用 */
    public static final String DEMO_PASSWORD = "demo";

    private DemoAccountCatalog() {
    }

    public static List<DemoAccount> all() {
        return List.of(
                // CUSTOMER ×3 — 侧栏：Home + My bookings
                new DemoAccount("alice", "alice@demo.hcbs", "Alice Chen", "13800138001", UserRole.CUSTOMER, "Customer"),
                new DemoAccount("bob", "bob@demo.hcbs", "Bob Walker", "13800138002", UserRole.CUSTOMER, "Customer"),
                new DemoAccount("carol", "carol@demo.hcbs", "Carol Murphy", "13800138003", UserRole.CUSTOMER, "Customer"),
                // BOOKING_STAFF ×3 — 侧栏：+ Book tickets + Cancellation
                new DemoAccount("staff", "staff@demo.hcbs", "Sam Staff", "13800238001", UserRole.BOOKING_STAFF, "Booking staff"),
                new DemoAccount("desk01", "desk01@demo.hcbs", "Emma Desk", "13800238002", UserRole.BOOKING_STAFF, "Booking staff"),
                new DemoAccount("desk02", "desk02@demo.hcbs", "Liam Desk", "13800238003", UserRole.BOOKING_STAFF, "Booking staff"),
                // ADMIN ×3 — 侧栏：再 + Data admin
                new DemoAccount("admin", "admin@demo.hcbs", "Ava Admin", "13800338001", UserRole.ADMIN, "Administrator"),
                new DemoAccount("admin01", "admin01@demo.hcbs", "Olivia Admin", "13800338002", UserRole.ADMIN, "Administrator"),
                new DemoAccount("admin02", "admin02@demo.hcbs", "Noah Admin", "13800338003", UserRole.ADMIN, "Administrator")
        );
    }

    /** roleLabel 用于 LoginView 演示面板分组标题 */
    public record DemoAccount(
            String username,
            String email,
            String fullName,
            String phone,
            UserRole role,
            String roleLabel
    ) {
    }
}
