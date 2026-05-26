package com.hcbs.security;

import com.hcbs.model.User;
import com.hcbs.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
