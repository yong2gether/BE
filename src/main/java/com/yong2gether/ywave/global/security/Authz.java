package com.yong2gether.ywave.global.security;

import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class Authz {

    private final UserRepository userRepository;


    public boolean isSelfOrAdmin(Long userId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) return true;


        String email = auth.getName();
        if (email == null) return false;

        return userRepository.findByEmail(email)
                .map(u -> u.getId().equals(userId))
                .orElse(false);
    }
}
