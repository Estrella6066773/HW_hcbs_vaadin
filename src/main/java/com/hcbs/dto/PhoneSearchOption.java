package com.hcbs.dto;

public record PhoneSearchOption(String phone, String label) {

    public static PhoneSearchOption forCustomer(String phone, String fullName) {
        return new PhoneSearchOption(phone, phone + " · " + fullName);
    }
}
