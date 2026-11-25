package com.sarkarisetu.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers("/admin/login", "/css/**", "/js/**", "/img/**").permitAll()
                // Super Admin only
                .requestMatchers("/admin/users/**", "/admin/sections/**", "/admin/pages/**", "/admin/tools/**",
                        "/admin/trending/**", "/admin/subscribers/**", "/admin/logs/**", "/admin/media/delete", "/admin/settings").hasRole("SUPER_ADMIN")
                // Editors and Super Admins
                .requestMatchers("/admin/**").hasAnyRole("EDITOR", "SUPER_ADMIN")
                .anyRequest().permitAll())
            .formLogin(f -> f.loginPage("/admin/login").loginProcessingUrl("/admin/login")
                    .defaultSuccessUrl("/admin", true).failureUrl("/admin/login?error"))
            .logout(l -> l.logoutUrl("/admin/logout").logoutSuccessUrl("/admin/login?out"))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
