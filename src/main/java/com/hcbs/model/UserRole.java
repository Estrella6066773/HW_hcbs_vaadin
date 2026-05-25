package com.hcbs.model;

public enum UserRole {
    CUSTOMER,
    BOOKING_STAFF,
    ADMIN,
    MANAGER;

    public boolean isEmployee() {
        return this != CUSTOMER;
    }

    public boolean isCustomer() {
        return this == CUSTOMER;
    }

    public boolean canAccessAdminTools() {
        return this == ADMIN || this == MANAGER;
    }
}
