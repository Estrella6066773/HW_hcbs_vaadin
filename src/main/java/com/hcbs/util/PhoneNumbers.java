package com.hcbs.util;

import java.util.regex.Pattern;

public final class PhoneNumbers {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9][0-9\\s-]{6,18}$");

    private PhoneNumbers() {
    }

    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replaceAll("[\\s-]", "");
    }

    public static boolean isValid(String phone) {
        String normalized = normalize(phone);
        return normalized != null && PHONE_PATTERN.matcher(normalized).matches();
    }
}
