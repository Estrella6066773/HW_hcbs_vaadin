package com.hcbs.security;

import com.hcbs.model.User;
import com.hcbs.model.UserStatus;
import com.hcbs.repository.UserRepository;
import com.hcbs.util.PhoneNumbers;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class HcbsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public HcbsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
                .username(user.getPhone())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
