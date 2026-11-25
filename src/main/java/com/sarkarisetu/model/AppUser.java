package com.sarkarisetu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class AppUser {
    public enum Role { EDITOR, SUPER_ADMIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private Role role = Role.EDITOR;
    private boolean enabled = true;
    private LocalDateTime lastLogin;

    public String getInitials() {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        String s = p[0].substring(0, 1) + (p.length > 1 ? p[p.length - 1].substring(0, 1) : "");
        return s.toUpperCase();
    }
    public String getLastLoginText() { return lastLogin == null ? "Never" : lastLogin.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, HH:mm")); }
    public boolean isSuperAdmin() { return role == Role.SUPER_ADMIN; }
    public String getRoleLabel() { return role == Role.SUPER_ADMIN ? "Super Admin" : "Editor"; }

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String v) { passwordHash = v; }
    public Role getRole() { return role; } public void setRole(Role v) { role = v; }
    public boolean isEnabled() { return enabled; } public void setEnabled(boolean v) { enabled = v; }
    public LocalDateTime getLastLogin() { return lastLogin; } public void setLastLogin(LocalDateTime v) { lastLogin = v; }
}
