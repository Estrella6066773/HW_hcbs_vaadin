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
                new DemoAccount("alice", "alice@demo.hcbs", "Alice Chen", "13800138001", UserRole.CUSTOMER, "Customer"),
                new DemoAccount("bob", "bob@demo.hcbs", "Bob Walker", "13800138002", UserRole.CUSTOMER, "Customer"),
                new DemoAccount("carol", "carol@demo.hcbs", "Carol Murphy", "13800138003", UserRole.CUSTOMER, "Customer"),
                // Booking staff
                new DemoAccount("staff", "staff@demo.hcbs", "Sam Staff", "13800238001", UserRole.BOOKING_STAFF, "Booking staff"),
                new DemoAccount("desk01", "desk01@demo.hcbs", "Emma Desk", "13800238002", UserRole.BOOKING_STAFF, "Booking staff"),
                new DemoAccount("desk02", "desk02@demo.hcbs", "Liam Desk", "13800238003", UserRole.BOOKING_STAFF, "Booking staff"),
                // Administrators
                new DemoAccount("admin", "admin@demo.hcbs", "Ava Admin", "13800338001", UserRole.ADMIN, "Administrator"),
                new DemoAccount("admin01", "admin01@demo.hcbs", "Olivia Admin", "13800338002", UserRole.ADMIN, "Administrator"),
                new DemoAccount("admin02", "admin02@demo.hcbs", "Noah Admin", "13800338003", UserRole.ADMIN, "Administrator")
        );
    }

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
