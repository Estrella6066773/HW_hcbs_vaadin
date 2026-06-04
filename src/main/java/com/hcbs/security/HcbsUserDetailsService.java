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
 * 登录认证：按手机号加载用户（成员 C · 安全模块）。
 * <p>
 * Spring Security 的 principal 为规范化后的 {@link com.hcbs.model.User#getPhone()}，
 * 与 {@link com.hcbs.web.auth.LoginView} 中 {@code HttpServletRequest.login} 传入的用户名一致。
 * 非 {@link com.hcbs.model.UserStatus#ACTIVE} 账号拒绝登录。
 */
@Service
public class HcbsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public HcbsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginPhone) throws UsernameNotFoundException {
        // loginPhone 即登录表单中的手机号，经 PhoneNumbers 规范化后查库
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
                .username(user.getPhone())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
