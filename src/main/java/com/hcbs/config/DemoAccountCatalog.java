package com.hcbs.config;

import com.hcbs.model.UserRole;

import java.util.List;

/**
 * Demo accounts seeded on empty database startup. Password for all: {@link #DEMO_PASSWORD}.
 */
public final class DemoAccountCatalog {

    public static final String DEMO_PASSWORD = "demo";

    private DemoAccountCatalog() {
    }

    public static List<DemoAccount> all() {
        return List.of(
                // Customers
                new DemoAccount("alice", "alice@demo.hcbs", "Alice Chen", UserRole.CUSTOMER, "客户"),
                new DemoAccount("bob", "bob@demo.hcbs", "Bob Walker", UserRole.CUSTOMER, "客户"),
                new DemoAccount("carol", "carol@demo.hcbs", "Carol Murphy", UserRole.CUSTOMER, "客户"),
                // Booking staff
                new DemoAccount("staff", "staff@demo.hcbs", "Sam Staff", UserRole.BOOKING_STAFF, "订票员"),
                new DemoAccount("desk01", "desk01@demo.hcbs", "Emma Desk", UserRole.BOOKING_STAFF, "订票员"),
                new DemoAccount("desk02", "desk02@demo.hcbs", "Liam Desk", UserRole.BOOKING_STAFF, "订票员"),
                // Administrators
                new DemoAccount("admin", "admin@demo.hcbs", "Ava Admin", UserRole.ADMIN, "管理员"),
                new DemoAccount("admin01", "admin01@demo.hcbs", "Olivia Admin", UserRole.ADMIN, "管理员"),
                new DemoAccount("admin02", "admin02@demo.hcbs", "Noah Admin", UserRole.ADMIN, "管理员"),
                // Managers
                new DemoAccount("manager", "manager@demo.hcbs", "Mia Manager", UserRole.MANAGER, "管理员"),
                new DemoAccount("mgr01", "mgr01@demo.hcbs", "Grace Manager", UserRole.MANAGER, "管理员"),
                new DemoAccount("mgr02", "mgr02@demo.hcbs", "James Manager", UserRole.MANAGER, "管理员")
        );
    }

    public record DemoAccount(
            String username,
            String email,
            String fullName,
            UserRole role,
            String roleLabel
    ) {
    }
}
