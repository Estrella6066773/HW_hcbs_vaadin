package com.hcbs.security;

import com.hcbs.model.User;
import com.hcbs.model.UserStatus;
import com.hcbs.repository.UserRepository;
import com.hcbs.util.PhoneNumbers;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 登录认证服务：按手机号加载用户（成员 C · 安全模块）。
 * <p>
 * Spring Security 的 principal 为规范化后的 {@link User#getPhone()}，
 * 与 {@link com.hcbs.web.auth.LoginView} 中 {@code HttpServletRequest.login} 传入的用户名保持一致。
 * 非 {@link UserStatus#ACTIVE} 状态的账号拒绝登录（管理员在后台停用账户时生效）。
 */
@Service
public class HcbsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public HcbsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据登录手机号加载用户详情。
     * <p>
     * {@code loginPhone} 即登录表单中的手机号；通过 {@link PhoneNumbers#normalize} 规范化后查询数据库。
     * 返回的 {@link UserDetails#getUsername()} = User.phone，供 {@link CurrentUserService} 反查用户实体。
     *
     * @param loginPhone 登录手机号
     * @return 用户详情
     * @throws UsernameNotFoundException 如果用户不存在或账号未激活
     */
    @Override
    public UserDetails loadUserByUsername(String loginPhone) throws UsernameNotFoundException {
        String phone = PhoneNumbers.normalize(loginPhone);
        if (phone == null) {
            throw new UsernameNotFoundException("Unknown user");
        }
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + phone));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("Account disabled: " + phone);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhone()) // principal 使用手机号，而非 username 字段
                .password(user.getPasswordHash()) // BCrypt 加密，与 SecurityConfig#passwordEncoder 一致
                .roles(user.getRole().name()) // → ROLE_CUSTOMER / ROLE_BOOKING_STAFF / ROLE_ADMIN
                .build();
    }
}
