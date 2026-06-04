package com.hcbs.service.auth;

import com.hcbs.dto.RegistrationRequest;
import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.model.UserStatus;
import com.hcbs.repository.UserRepository;
import com.hcbs.util.PhoneNumbers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * 客户注册业务（成员 C · 账户模块）。
 * <p>
 * 仅创建 {@link UserRole#CUSTOMER} 且状态为 {@link UserStatus#ACTIVE} 的用户；
 * 密码经 {@link PasswordEncoder} 哈希后存入数据库。手机号经 {@link PhoneNumbers} 规范化，
 * 与登录、{@link com.hcbs.security.HcbsUserDetailsService} 使用同一套格式。
 * <p>
 * 自动化测试：{@link com.hcbs.service.auth.RegistrationServiceTest}；注册成功路径为手工演示。
 */
@Service
public class RegistrationService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,32}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 校验通过后持久化新客户；角色固定为 CUSTOMER，不可通过本 API 注册员工账号。
     */
    @Transactional
    public User registerCustomer(RegistrationRequest request) {
        validate(request);

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(User.normalizeEmail(request.email()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setPhone(PhoneNumbers.normalize(request.phone())); // 登录 principal
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    /**
     * 用户名、手机、邮箱唯一性及密码一致性校验；校验失败时抛出 {@link IllegalArgumentException} 供 UI 展示。
     */
    public void validate(RegistrationRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        String username = request.username().trim();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3–32 characters and use letters, numbers, dots, underscores, or hyphens");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String phone = PhoneNumbers.normalize(request.phone());
        if (phone == null) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (!PhoneNumbers.isValid(phone)) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone number is already registered");
        }

        String email = User.normalizeEmail(request.email());
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Email format is invalid");
            }
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Email is already registered");
            }
        }

        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.fullName().trim().length() > 100) {
            throw new IllegalArgumentException("Full name must be at most 100 characters");
        }

        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}
