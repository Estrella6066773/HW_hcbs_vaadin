package com.hcbs.security;

import com.hcbs.model.User;
import com.hcbs.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 当前登录用户查询（成员 C · 安全模块）。
 * <p>
 * 从 {@link org.springframework.security.core.context.SecurityContextHolder} 读取 principal（手机号），
 * 再映射为领域对象 {@link User}。供 {@link com.hcbs.web.shell.MainLayout} 侧栏、
 * 取消模块权限判断及各 View 使用。
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public Optional<User> findCurrentUser() {
        if (!isAuthenticated()) {
            return Optional.empty();
        }
        return userRepository.findByPhone(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public User requireCurrentUser() {
        return findCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Not signed in"));
    }

    public boolean isEmployee() {
        return findCurrentUser().map(user -> user.getRole().isEmployee()).orElse(false);
    }

    public boolean isCustomer() {
        return findCurrentUser().map(user -> user.getRole().isCustomer()).orElse(false);
    }
}
