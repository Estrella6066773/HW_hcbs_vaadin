package com.hcbs.util;

import java.util.regex.Pattern;

/**
 * 手机号规范化与校验（成员 C · 工具类）。
 * <p>
 * 登录名、注册、取消检索、{@link com.hcbs.security.HcbsUserDetailsService} 共用：
 * 去掉空格与连字符后做格式校验，保证全链路同一标识。
 */
public final class PhoneNumbers {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9][0-9\\s-]{6,18}$");

    private PhoneNumbers() {
    }

    /** 去首尾空白并移除空格、连字符；空输入返回 null */
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

    /** 规范化后匹配国际/本地数字格式 */
    public static boolean isValid(String phone) {
        String normalized = normalize(phone);
        return normalized != null && PHONE_PATTERN.matcher(normalized).matches();
    }
}
