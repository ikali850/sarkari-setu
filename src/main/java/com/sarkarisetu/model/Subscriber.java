package com.sarkarisetu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Subscriber {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getCreatedText() { return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")); }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
