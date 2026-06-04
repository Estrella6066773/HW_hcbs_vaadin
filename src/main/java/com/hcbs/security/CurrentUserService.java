package com.hcbs.security;

import com.hcbs.model.User;
import com.hcbs.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 当前登录用户查询服务（成员 C · 安全模块）。
 * <p>
 * 从 {@link SecurityContextHolder} 读取用户标识（规范化手机号），
 * 再映射为领域对象 {@link User}。供 {@link com.hcbs.web.shell.MainLayout} 侧边栏、
 * {@link com.hcbs.service.cancellation.CancellationService} 权限判断及各 View 使用。
 * <p>
 * 切勿在 UI 层缓存 User 对象：登出后必须通过本服务实时读取会话状态。
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 判断是否已建立有效的 Spring Security 会话（排除 anonymousUser）。
     * @return 是否已登录
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 如果已登录，则按 principal（手机号）查询数据库返回用户；未登录则返回 empty。
     * @return 当前登录用户的 Optional 包装
     */
    public Optional<User> findCurrentUser() {
        if (!isAuthenticated()) {
            return Optional.empty();
        }
        return userRepository.findByPhone(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * 获取当前登录用户，未登录时抛出 {@link AccessDeniedException}。
     * 供必须登录的业务方法使用。
     * @return 当前登录用户
     * @throws AccessDeniedException 如果用户未登录
     */
    public User requireCurrentUser() {
        return findCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Not signed in"));
    }

    /**
     * 判断当前用户是否为员工角色。
     * @return 如果是员工则返回 true，否则返回 false
     */
    public boolean isEmployee() {
        return findCurrentUser().map(user -> user.getRole().isEmployee()).orElse(false);
    }

    /**
     * 判断当前用户是否为客户角色。
     * @return 如果是客户则返回 true，否则返回 false
     */
    public boolean isCustomer() {
        return findCurrentUser().map(user -> user.getRole().isCustomer()).orElse(false);
    }
}
