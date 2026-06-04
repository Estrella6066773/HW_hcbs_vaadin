package com.hcbs.util;

import java.util.regex.Pattern;

/**
 * 手机号规范化与校验工具类（成员 C · 工具类）。
 * <p>
 * 全链路共用，保证「登录名 = 注册手机 = 取消检索 = Security principal」为同一字符串：
 * <ul>
 *   <li>{@link com.hcbs.web.auth.LoginView#submitLogin}</li>
 *   <li>{@link com.hcbs.service.auth.RegistrationService#registerCustomer}</li>
 *   <li>{@link com.hcbs.security.HcbsUserDetailsService#loadUserByUsername}</li>
 *   <li>{@link com.hcbs.service.cancellation.CancellationService} 按手机查单</li>
 * </ul>
 */
public final class PhoneNumbers {

    /** 允许 + 开头及 7–19 位数字（支持输入时的空格或连字符，规范化后匹配） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9][0-9\\s-]{6,18}$");

    private PhoneNumbers() {
    }

    /**
     * 去除首尾空白并移除空格、连字符；空输入返回 null。
     * 示例：{@code "138-0013-8001"} → {@code "13800138001"}
     * @param phone 原始手机号
     * @return 规范化后的手机号，或 null（如果输入为空）
     */
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

    /**
     * 对原始或已规范化字符串校验格式。
     * @param phone 手机号
     * @return 如果格式有效则返回 true，否则返回 false
     */
    public static boolean isValid(String phone) {
        String normalized = normalize(phone);
        return normalized != null && PHONE_PATTERN.matcher(normalized).matches();
    }
}
