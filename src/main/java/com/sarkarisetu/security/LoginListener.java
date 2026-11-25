package com.sarkarisetu.security;

import com.sarkarisetu.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/** Records the last login time and counts wrong passwords. */
@Component
public class LoginListener {
    private static final Logger log = LoggerFactory.getLogger("LOGIN");
    private final UserRepository users;
    private final LoginAttempts attempts;

    public LoginListener(UserRepository users, LoginAttempts attempts) { this.users = users; this.attempts = attempts; }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        String name = e.getAuthentication().getName();
        attempts.ok(name);
        log.info("login ok: {}", name);
        users.findByEmailIgnoreCase(name).ifPresent(u -> { u.setLastLogin(LocalDateTime.now()); users.save(u); });
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent e) {
        String name = String.valueOf(e.getAuthentication().getName());
        attempts.fail(name);
        log.warn("login failed: {}", name);
    }
}
