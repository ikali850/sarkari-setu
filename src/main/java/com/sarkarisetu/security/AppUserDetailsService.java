package com.sarkarisetu.security;

import com.sarkarisetu.model.AppUser;
import com.sarkarisetu.repo.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    private final LoginAttempts attempts;

    public AppUserDetailsService(UserRepository users, LoginAttempts attempts) { this.users = users; this.attempts = attempts; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser u = users.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return User.withUsername(u.getEmail()).password(u.getPasswordHash())
                .roles(u.getRole().name()).disabled(!u.isEnabled()).accountLocked(attempts.isBlocked(u.getEmail())).build();
    }
}
